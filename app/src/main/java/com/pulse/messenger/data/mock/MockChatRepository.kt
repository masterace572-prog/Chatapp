package com.pulse.messenger.data.mock

import com.pulse.messenger.domain.model.ChatSummary
import com.pulse.messenger.domain.model.Message
import com.pulse.messenger.domain.model.User
import com.pulse.messenger.domain.repository.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Mock conversations (PRD §5 + §9). Singleton state store so lists stay
 * consistent across screens (Chats, Archived, Search):
 *  - chat flags (pinned/muted/archived) + unread counts mutate in memory;
 *  - typing indicator pulses on [SeedData.typingChatId] every few seconds;
 *  - refresh() simulates a fetch round-trip.
 * M4 adds send/reply behaviours on the same store.
 */
@Singleton
class MockChatRepository @Inject constructor(
    @com.pulse.messenger.di.ApplicationScope private val scope: CoroutineScope,
) : ChatRepository {

    /** Live per-chat state: the chat + its unread counter. */
    private data class ChatState(var chat: com.pulse.messenger.domain.model.Chat, var unread: Int)

    /** Offset so seed timestamps render relative to "now" on first launch. */
    private val timeOffset = System.currentTimeMillis() - SeedData.BaseMillis

    private val _chatState = MutableStateFlow(buildInitial())
    private val _typing = MutableStateFlow<String?>(null)

    /** Derived, sorted summaries - single source for all list screens. */
    private val _summaries = MutableStateFlow(emptyList<ChatSummary>())

    private fun buildInitial(): Map<String, ChatState> =
        SeedData.chats.associate { chat ->
            chat.id to ChatState(chat, SeedData.unreadCounts[chat.id] ?: 0)
        }

    private fun messagesOf(chatId: String): List<Message> =
        SeedData.messagesByChatId[chatId].orEmpty()
            .map { it.copy(sentAtMillis = it.sentAtMillis + timeOffset) }
            .sortedBy { it.sentAtMillis }

    private fun allMessages(): List<Message> =
        SeedData.messagesByChatId.keys.flatMap(::messagesOf)

    private fun user(id: String): User = SeedData.user(id)

    private fun buildSummary(id: String, state: ChatState, typing: String?): ChatSummary {
        val chat = state.chat
        val messages = messagesOf(id)
        val last = messages.lastOrNull()
        val peers = chat.participantIds.filter { it != "me" }
        val directPeer = if (chat.kind == com.pulse.messenger.domain.model.ChatKind.Direct) {
            peers.firstOrNull()
        } else null
        val peerUser = directPeer?.let(::user)
        return ChatSummary(
            chatId = id,
            kind = chat.kind,
            displayName = chat.title ?: peerUser?.displayName ?: "Unknown",
            avatarSeed = peerUser?.avatarSeed ?: chat.avatarSeed,
            peerFirstName = peerUser?.firstName.orEmpty(),
            memberNames = if (chat.kind == com.pulse.messenger.domain.model.ChatKind.Group) {
                peers.mapNotNull { pid -> SeedData.usersById[pid]?.firstName }
            } else emptyList(),
            participantCount = peers.size,
            lastMessage = last,
            lastSenderFirstName = last?.senderId?.let { pid -> SeedData.usersById[pid]?.firstName },
            unreadCount = state.unread,
            isArchived = chat.isArchived,
            isMuted = chat.isMuted,
            isPinned = chat.isPinned,
            isTyping = typing == id,
            draft = SeedData.drafts[id],
        )
    }

    private fun rebuild() {
        val typing = _typing.value
        _summaries.value = _chatState.value.values
            .map { buildSummary(it.chat.id, it, typing) }
            .sortedByDescending { it.lastMessage?.sentAtMillis ?: 0L }
    }

    init {
        rebuild()
        // Pulse the typing indicator on the seeded chat to keep the list alive.
        scope.launch {
            while (true) {
                delay(Random.nextLong(12_000, 24_000))
                _typing.value = SeedData.typingChatId
                rebuild()
                delay(7_000)
                _typing.value = null
                rebuild()
            }
        }
    }

    override fun observeChatSummaries(): Flow<List<ChatSummary>> =
        _summaries.map { list -> list.filterNot { it.isArchived } }

    override fun observeArchivedSummaries(): Flow<List<ChatSummary>> =
        _summaries.map { list -> list.filter { it.isArchived } }

    override fun observeAllMessages(): Flow<List<Message>> = flow {
        Simulator.shortDelay()
        emit(allMessages())
    }

    override fun observeMessages(chatId: String): Flow<List<Message>> = flow {
        emit(messagesOf(chatId))
    }

    /* ---------- Actions (single source of truth: _chatState + rebuild) ---------- */

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

    override suspend fun markRead(chatId: String) {
        mutate(chatId) { it.unread = 0 }
    }

    override suspend fun deleteChats(chatIds: List<String>) {
        Simulator.shortDelay()
        _chatState.value = _chatState.value.filterKeys { it !in chatIds }
        rebuild()
    }

    override suspend fun markAllRead() {
        _chatState.value = _chatState.value.toMutableMap().apply { values.forEach { it.unread = 0 } }
        rebuild()
    }

    override suspend fun refresh() {
        _typing.value = null
        Simulator.networkDelay()
        rebuild()
    }
}
