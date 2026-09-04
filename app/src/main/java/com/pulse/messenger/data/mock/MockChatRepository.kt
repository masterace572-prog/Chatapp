package com.pulse.messenger.data.mock

import com.pulse.messenger.domain.model.Chat
import com.pulse.messenger.domain.model.ChatKind
import com.pulse.messenger.domain.model.ChatSummary
import com.pulse.messenger.domain.model.Message
import com.pulse.messenger.domain.model.MessageContent
import com.pulse.messenger.domain.model.MessageStatus
import com.pulse.messenger.domain.model.User
import com.pulse.messenger.domain.repository.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Mock conversations (PRD §5 + §9). Singleton state store so lists and the
 * conversation screen stay consistent across Chats / Archived / Search / Chat.
 *
 * M4a behaviors on top of the M3 store:
 *  - live message streams (send/reply/status transitions emit everywhere);
 *  - send pipeline Sending → Sent (300–800ms) → Delivered (+0.5–1.5s) → Read
 *    (+1–3s, only when the chat is "online"); ~5% of sends fail so the retry
 *    UI is exercisable - retries always succeed;
 *  - auto-replies (PRD §9 + D2): the ~40% of chats flagged in [autoReplyChats]
 *    reply 1.5–4s after a successful send, showing the sender typing 1–2s
 *    first; the Pulse Assistant chat always replies (deterministic demos);
 *  - unread counts bump for incoming messages only when the chat is not on
 *    screen (see [setActiveConversation]); opening a chat clears the badge;
 *  - typing simulation on [SeedData.typingChatId] keeps lists alive;
 *  - drafts mutate (S19 prefix) via [setDraft].
 */
@Singleton
class MockChatRepository @Inject constructor(
    @com.pulse.messenger.di.ApplicationScope private val scope: CoroutineScope,
) : ChatRepository {

    private data class ChatState(
        var chat: Chat,
        var unread: Int,
        var draft: String?,
    )

    /** Live per-chat state: chat + unread counter + mutable draft. */
    private val _chatState = MutableStateFlow(buildInitialChats())

    /** Live message store: chatId -> messages, newest last. Single source. */
    private val _messages = MutableStateFlow(
        SeedData.messagesByChatId.mapValues { (_, list) -> list.sortedBy { it.sentAtMillis } },
    )

    /** chatId -> user ids currently typing. */
    private val _typing = MutableStateFlow<Map<String, Set<String>>>(emptyMap())

    /** Derived, sorted summaries - single source for all list screens. */
    private val _summaries = MutableStateFlow(emptyList<ChatSummary>())

    /** Chat currently on the conversation screen (null = none). */
    @Volatile
    private var activeConversation: String? = null

    private var sendCounter = 0L

    private fun buildInitialChats(): Map<String, ChatState> =
        SeedData.chats.associate { chat ->
            chat.id to ChatState(
                chat = chat,
                unread = SeedData.unreadCounts[chat.id] ?: 0,
                draft = SeedData.drafts[chat.id],
            )
        }

    /** Chats that auto-reply (about 40%; Pulse Assistant always replies). */
    private val autoReplyChats: Set<String> = setOf(
        "c-assistant", "c-aria", "c-noah", "c-mira", "c-design", "c-fam",
    )

    private val replyPool: Map<String, List<String>> = mapOf(
        "c-assistant" to listOf(
            "Got it - anything else you want to try?",
            "Nice! You can also try searching for 'route'.",
            "I always reply, so this is the chat for demos.",
            "Done. Swipe left on a chat to pin it.",
            "Try long-pressing a message bubble next.",
            "Folders are under the Chats filter menu.",
        ),
        "c-aria" to listOf(
            "Ooh, send me the screenshot!",
            "Agreed - let's keep it subtle.",
            "I'll check the contrast tonight.",
            "Adding it to the review list.",
            "That works for me.",
        ),
        "c-noah" to listOf(
            "Nice, grabbing it now.",
            "Testing on my side, one sec.",
            "That matches what I saw too.",
            "Cool - I'll run the device pass.",
        ),
        "c-mira" to listOf(
            "Ha, I knew you'd like it!",
            "Let's do it this weekend.",
            "Sending you the details later.",
        ),
        "c-design" to listOf(
            "I can take that one.",
            "Looks consistent to me.",
            "Adding notes to the doc.",
            "Let's discuss in the review.",
        ),
        "c-fam" to listOf(
            "I'm in!",
            "Count me in for Saturday.",
            "Sounds perfect.",
        ),
    )

    private fun user(id: String): User = SeedData.user(id)

    private fun peersOf(chat: Chat): List<String> = chat.participantIds.filter { it != "me" }

    /** True when the conversation is "online": direct peer or any member online. */
    private fun isOnline(chat: Chat): Boolean {
        val ids = if (chat.kind == ChatKind.Direct) peersOf(chat) else chat.participantIds
        return ids.any { SeedData.usersById[it]?.isOnline == true }
    }

    private fun messagesOf(chatId: String): List<Message> =
        _messages.value[chatId].orEmpty().sortedBy { it.sentAtMillis }

    private fun allMessages(): List<Message> =
        _messages.value.values.flatten()

    private fun buildSummary(id: String, state: ChatState, typingIds: Set<String>): ChatSummary {
        val chat = state.chat
        val messages = messagesOf(id)
        val last = messages.lastOrNull()
        val peers = peersOf(chat)
        val directPeer = if (chat.kind == ChatKind.Direct) peers.firstOrNull() else null
        val peerUser = directPeer?.let(::user)
        return ChatSummary(
            chatId = id,
            kind = chat.kind,
            displayName = chat.title ?: peerUser?.displayName ?: "Unknown",
            avatarSeed = peerUser?.avatarSeed ?: chat.avatarSeed,
            peerFirstName = peerUser?.firstName.orEmpty(),
            memberNames = if (chat.kind == ChatKind.Group) {
                peers.mapNotNull { pid -> SeedData.usersById[pid]?.firstName }
            } else emptyList(),
            participantCount = peers.size,
            lastMessage = last,
            lastSenderFirstName = last?.senderId?.let { pid -> SeedData.usersById[pid]?.firstName },
            unreadCount = state.unread,
            isArchived = chat.isArchived,
            isMuted = chat.isMuted,
            isPinned = chat.isPinned,
            isTyping = typingIds.isNotEmpty(),
            draft = state.draft,
            isVerified = directPeer?.let { SeedData.usersById[it]?.isVerified } == true,
        )
    }

    private fun rebuild() {
        val typing = _typing.value
        _summaries.value = _chatState.value.values
            .map { buildSummary(it.chat.id, it, typing[it.chat.id].orEmpty()) }
            .sortedByDescending { it.lastMessage?.sentAtMillis ?: 0L }
    }

    init {
        rebuild()
        // Idle typing pulses keep lists + the conversation screen alive (D4).
        scope.launch {
            while (true) {
                delay(Random.nextLong(12_000, 24_000))
                setTyping(SeedData.typingChatId, setOf(peerOf(SeedData.typingChatId)))
                delay(7_000)
                clearTyping(SeedData.typingChatId)
            }
        }
    }

    /* ---------- Flows ---------- */

    override fun observeChatSummaries(): Flow<List<ChatSummary>> =
        _summaries.map { list -> list.filterNot { it.isArchived } }

    override fun observeArchivedSummaries(): Flow<List<ChatSummary>> =
        _summaries.map { list -> list.filter { it.isArchived } }

    override fun observeAllMessages(): Flow<List<Message>> =
        flowOf(allMessages())

    override fun observeChat(chatId: String): Flow<Chat> =
        _chatState.map { states -> states[chatId]?.chat ?: Chat(id = chatId, kind = ChatKind.Direct) }

    override fun observeMessages(chatId: String): Flow<List<Message>> =
        _messages.map { states -> states[chatId].orEmpty() }

    override fun observeTyping(chatId: String): Flow<Set<String>> =
        _typing.map { states -> states[chatId].orEmpty() }

    private fun peerOf(chatId: String): String =
        _chatState.value[chatId]?.chat?.let { peersOf(it).firstOrNull() } ?: "u-aria"

    /* ---------- Sending (mock pipeline, PRD §9 + D2) ---------- */

    override suspend fun sendText(chatId: String, text: String, replyToMessageId: String?): String {
        val trimmed = text.trim()
        require(trimmed.isNotEmpty()) { "Empty message" }
        val state = _chatState.value[chatId] ?: return ""
        val id = "m-${System.currentTimeMillis()}-${sendCounter++}"
        val failed = Random.nextInt(100) < 5 // ~5% fail so retry UI is exercisable.
        appendMessage(
            Message(
                id = id,
                chatId = chatId,
                senderId = "me",
                content = MessageContent.Text(trimmed),
                sentAtMillis = System.currentTimeMillis(),
                status = MessageStatus.Sending,
                isOutgoing = true,
                replyToMessageId = replyToMessageId,
            ),
        )
        clearUnread(chatId)
        scope.launch { runPipeline(chatId, id, state.chat, failed) }
        return id
    }

    /**
     * The per-message status walk. Failed sends stop at Failed; retries always
     * succeed. Chat must stay consistent if the app is backgrounded - delays
     * simply run on the app scope.
     */
    private suspend fun runPipeline(chatId: String, messageId: String, chat: Chat, willFail: Boolean) {
        // Sending -> SENT (300-800ms), or -> FAILED for the ~5% demo path.
        delay(Random.nextLong(300, 800))
        if (willFail) {
            updateStatus(chatId, messageId, MessageStatus.Failed)
            return
        }
        updateStatus(chatId, messageId, MessageStatus.Sent)
        // SENT -> DELIVERED (+0.5-1.5s)
        delay(Random.nextLong(500, 1500))
        updateStatus(chatId, messageId, MessageStatus.Delivered)
        // DELIVERED -> READ (+1-3s) only when the chat is "online".
        if (isOnline(chat)) {
            delay(Random.nextLong(1000, 3000))
            updateStatus(chatId, messageId, MessageStatus.Read)
        }
        // Auto-reply (PRD §9 + D2): typing first, reply 1.5-4s after the send.
        if (chatId in autoReplyChats) {
            val replyAt = Random.nextLong(1500, 4000)
            val typingAt = (replyAt - Random.nextLong(1000, 2000)).coerceAtLeast(300L)
            delay(typingAt)
            val replier = replySender(chat)
            setTyping(chatId, setOf(replier))
            delay((replyAt - typingAt).coerceAtLeast(300L))
            clearTyping(chatId)
            appendReply(chatId, replier)
        }
    }

    override suspend fun retryMessage(messageId: String) {
        val entry = findMessage(messageId) ?: return
        if (entry.message.status != MessageStatus.Failed) return
        updateStatus(entry.chatId, messageId, MessageStatus.Sending)
        val chat = _chatState.value[entry.chatId]?.chat ?: return
        scope.launch { runPipeline(entry.chatId, messageId, chat, willFail = false) }
    }

    private fun replySender(chat: Chat): String {
        val peers = peersOf(chat)
        return if (chat.kind == ChatKind.Direct) {
            peers.first()
        } else {
            peers[Random.nextInt(peers.size)]
        }
    }

    private fun appendReply(chatId: String, senderId: String) {
        val chat = _chatState.value[chatId]?.chat ?: return
        val online = isOnline(chat)
        val pool = replyPool[chatId] ?: return
        val inScreen = activeConversation == chatId
        val message = Message(
            id = "m-${System.currentTimeMillis()}-r${sendCounter++}",
            chatId = chatId,
            senderId = senderId,
            content = MessageContent.Text(pool[Random.nextInt(pool.size)]),
            sentAtMillis = System.currentTimeMillis(),
            status = if (online) MessageStatus.Read else MessageStatus.Delivered,
            isOutgoing = false,
        )
        appendMessage(message)
        if (!inScreen) bumpUnread(chatId)
    }

    private fun setTyping(chatId: String, ids: Set<String>) {
        _typing.value = _typing.value + (chatId to ids)
        rebuild()
    }

    private fun clearTyping(chatId: String) {
        if (_typing.value[chatId].isNullOrEmpty()) return
        _typing.value = _typing.value - chatId
        rebuild()
    }

    /* ---------- Conversation & read state ---------- */

    override suspend fun markChatRead(chatId: String) {
        mutate(chatId) { it.unread = 0 }
    }

    override suspend fun setActiveConversation(chatId: String?) {
        activeConversation = chatId
    }

    override suspend fun setDraft(chatId: String, text: String) {
        mutate(chatId) { it.draft = text.trim().ifEmpty { null } }
    }

    private fun clearUnread(chatId: String) {
        _chatState.value = _chatState.value.toMutableMap().apply {
            get(chatId)?.let { it.unread = 0 }
        }
    }

    private fun bumpUnread(chatId: String) {
        _chatState.value = _chatState.value.toMutableMap().apply {
            get(chatId)?.let { it.unread += 1 }
        }
        rebuild()
    }

    /* ---------- Message store helpers ---------- */

    private fun appendMessage(message: Message) {
        _messages.value = _messages.value.toMutableMap().apply {
            val list = get(message.chatId).orEmpty()
            put(message.chatId, list + message)
        }
        rebuild()
    }

    private fun updateStatus(chatId: String, messageId: String, status: MessageStatus) {
        _messages.value = _messages.value.toMutableMap().apply {
            val list = get(chatId).orEmpty()
            put(
                chatId,
                list.map { if (it.id == messageId) it.copy(status = status) else it },
            )
        }
        rebuild()
    }

    private data class FoundMessage(val chatId: String, val message: Message)

    private fun findMessage(messageId: String): FoundMessage? {
        _messages.value.forEach { (chatId, list) ->
            list.firstOrNull { it.id == messageId }?.let { return FoundMessage(chatId, it) }
        }
        return null
    }

    /* ---------- Chat list actions (single source of truth: _chatState + rebuild) ---------- */

    private fun mutate(chatId: String, block: (ChatState) -> Unit) {
        _chatState.value = _chatState.value.toMutableMap().apply {
            get(chatId)?.let(block)
        }
        rebuild()
    }

    override suspend fun setArchived(chatId: String, archived: Boolean) {
        Simulator.shortDelay()
        mutate(chatId) { it.chat = it.chat.copy(isArchived = archived) }
    }

    override suspend fun setPinned(chatId: String, pinned: Boolean) {
        Simulator.shortDelay()
        mutate(chatId) { it.chat = it.chat.copy(isPinned = pinned) }
    }

    override suspend fun setMuted(chatId: String, muted: Boolean) {
        Simulator.shortDelay()
        mutate(chatId) { it.chat = it.chat.copy(isMuted = muted) }
    }

    override suspend fun markRead(chatId: String) = markChatRead(chatId)

    override suspend fun deleteChats(chatIds: List<String>) {
        Simulator.shortDelay()
        _chatState.value = _chatState.value.filterKeys { it !in chatIds }
        _messages.value = _messages.value.filterKeys { it !in chatIds }
        _typing.value = _typing.value.filterKeys { it !in chatIds }
        rebuild()
    }

    override suspend fun markAllRead() {
        _chatState.value = _chatState.value.toMutableMap().apply { values.forEach { it.unread = 0 } }
        rebuild()
    }

    override suspend fun refresh() {
        _typing.value = emptyMap()
        Simulator.networkDelay()
        rebuild()
    }
}
