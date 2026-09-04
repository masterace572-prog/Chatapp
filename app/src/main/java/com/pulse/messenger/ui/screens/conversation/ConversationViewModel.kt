package com.pulse.messenger.ui.screens.conversation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulse.messenger.domain.model.Chat
import com.pulse.messenger.domain.model.ChatKind
import com.pulse.messenger.domain.model.ChatSummary
import com.pulse.messenger.domain.model.Message
import com.pulse.messenger.domain.model.MessageContent
import com.pulse.messenger.domain.model.User
import com.pulse.messenger.domain.repository.ChatRepository
import com.pulse.messenger.domain.repository.ContactsRepository
import com.pulse.messenger.ui.components.ComposerUiState
import com.pulse.messenger.ui.util.MessageLabels
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Conversation screen state (S23 core + M4b interactions). The chat header,
 * message rows, typing set, draft, unread totals, composer surface and
 * selection/overlay state all derive from repository flows; the UI never
 * joins data itself.
 *
 * The unread divider is captured from the first summaries emission (the badge
 * is cleared immediately after), so it shows exactly once per open.
 */
data class ConversationUiState(
    val loading: Boolean = true,
    val chat: Chat? = null,
    val rows: List<ConversationRow> = emptyList(),
    val users: Map<String, User> = emptyMap(),
    /** Other chats' total unread (back-button badge). */
    val otherUnread: Int = 0,
    val draftText: String = "",
    /** New messages received while scrolled up (FAB pill). */
    val unseenCount: Int = 0,
    /** True when the view is pinned to the newest message. */
    val atBottom: Boolean = true,
    /* ---- M4b composer surface ---- */
    val composer: ComposerUiState = ComposerUiState.Idle,
    /* ---- M4b message interactions ---- */
    val selectionMode: Boolean = false,
    val selectedMessageIds: Set<String> = emptySet(),
    /** Message shown in the long-press overlay (null = none). */
    val actionMessageId: String? = null,
    /** Index into the valid pinned list shown by the banner. */
    val pinnedDisplayIndex: Int = 0,
    /** Scroll-to target after quote/pin taps (consumed by the screen). */
    val jumpTargetId: String? = null,
    /** Message whose bubble flashes (scroll highlight). */
    val flashMessageId: String? = null,
    /** Monotonic bump re-triggering the flash animation. */
    val flashTick: Int = 0,
    /** Group members mentionable in the composer. */
    val mentionMembers: List<User> = emptyList(),
    /** Non-archived chats usable as Forward targets. */
    val selectableChats: List<ChatSummary> = emptyList(),
) {
    val peerId: String?
        get() = chat?.participantIds?.firstOrNull { it != "me" }

    /** Pinned messages that still exist in this chat (deleted ones drop). */
    val validPinnedIds: List<String>
        get() {
            val chat = chat ?: return emptyList()
            val existing = rows.filterIsInstance<ConversationRow.MessageItem>()
                .map { it.message.id }
                .toSet()
            return chat.pinnedMessageIds.filter { it in existing }
        }
}

/** One row of the conversation list (display order: newest first). */
sealed interface ConversationRow {
    val key: String

    data class Day(val dateMillis: Long) : ConversationRow {
        override val key = "day-${dateMillis / 86_400_000L}"
    }

    data class Unread(val count: Int) : ConversationRow {
        override val key = "unread"
    }

    data class MessageItem(
        val message: Message,
        val senderName: String?,
        val senderSeed: Int?,
        val isFirstInRun: Boolean,
        val isLastInRun: Boolean,
    ) : ConversationRow {
        override val key = "m-${message.id}"
    }

    data class Typing(
        val senderName: String?,
        val senderSeed: Int?,
    ) : ConversationRow {
        override val key = "typing"
    }
}

/**
 * Builds the display-ordered rows (newest first) for the reverse LazyColumn.
 * Pure function - the previews reuse it with hand-made messages.
 */
fun buildConversationRows(
    messages: List<Message>,
    users: Map<String, User>,
    unreadMarker: Int,
    typingIds: Set<String> = emptySet(),
): List<ConversationRow> {
    if (messages.isEmpty()) return emptyList()
    val ascending = messages.sortedBy { it.sentAtMillis }

    val runFirst = BooleanArray(ascending.size) { true }
    val runLast = BooleanArray(ascending.size) { true }
    for (i in 1 until ascending.size) {
        val prev = ascending[i - 1]
        val cur = ascending[i]
        val sameRun = prev.senderId == cur.senderId &&
            cur.sentAtMillis - prev.sentAtMillis <= 2 * 60_000L &&
            !prev.isSystem && !cur.isSystem
        if (sameRun) {
            runFirst[i] = false
            runLast[i - 1] = false
        }
    }

    val firstUnread = (ascending.size - unreadMarker).coerceIn(0, ascending.size)

    val rows = ArrayList<ConversationRow>(ascending.size + 8)
    val typingName = typingIds.firstOrNull()?.let { id ->
        users[id]?.let { user -> user.displayName to user.avatarSeed }
    }
    if (typingName != null) {
        rows.add(ConversationRow.Typing(typingName.first, typingName.second))
    }

    var currentDay = -1L
    val dayMillis = 86_400_000L
    for (i in ascending.indices.reversed()) {
        val message = ascending[i]
        val day = message.sentAtMillis / dayMillis
        if (day != currentDay) {
            currentDay = day
            rows.add(ConversationRow.Day(message.sentAtMillis))
        }
        if (unreadMarker > 0 && i == firstUnread) {
            rows.add(ConversationRow.Unread(unreadMarker))
        }
        val sender = message.senderId
        rows.add(
            ConversationRow.MessageItem(
                message = message,
                senderName = if (message.isSystem) null else users[sender]?.displayName,
                senderSeed = users[sender]?.avatarSeed,
                isFirstInRun = runFirst[i],
                isLastInRun = runLast[i],
            ),
        )
    }
    return rows
}

/** Recording phase held by the composer (M4b). */
private enum class RecordingPhase { Active, Locked }

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class ConversationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository,
    contactsRepository: ContactsRepository,
) : ViewModel() {

    private val chatId: String = savedStateHandle.get<String>("chatId").orEmpty()

    private val draftText = MutableStateFlow("")
    private val draftInited = MutableStateFlow(false)

    private val atBottom = MutableStateFlow(true)
    private val unseenCount = MutableStateFlow(0)
    private val unreadMarker = MutableStateFlow(0)
    private val markerCaptured = MutableStateFlow(false)

    /* ---- M4b interaction state ---- */

    private val replyId = MutableStateFlow<String?>(null)
    private val editId = MutableStateFlow<String?>(null)
    private val recordingPhase = MutableStateFlow<RecordingPhase?>(null)

    private val selectionMode = MutableStateFlow(false)
    private val selectedIds = MutableStateFlow<Set<String>>(emptySet())
    private val actionMessageId = MutableStateFlow<String?>(null)
    private val pinnedDisplayIndex = MutableStateFlow(0)
    private val jumpTargetId = MutableStateFlow<String?>(null)
    private val flashMessageId = MutableStateFlow<String?>(null)
    private val flashTick = MutableStateFlow(0)

    init {
        viewModelScope.launch {
            draftText
                .debounce(350)
                .distinctUntilChanged()
                .collect { text ->
                    if (draftInited.value) chatRepository.setDraft(chatId, text)
                }
        }

        viewModelScope.launch {
            var prevSize = -1
            chatRepository.observeMessages(chatId).collect { list ->
                val size = list.size
                if (prevSize >= 0 && size > prevSize) {
                    val delta = size - prevSize
                    unseenCount.value = if (atBottom.value) 0 else unseenCount.value + delta
                    if (atBottom.value) chatRepository.markChatRead(chatId)
                }
                prevSize = size
            }
        }
    }

    private data class ChatMessages(
        val chat: Chat,
        val messages: List<Message>,
        val typing: Set<String>,
    )

    private data class ScrollInputs(val unseen: Int, val bottom: Boolean)

    private data class Base(
        val chat: Chat,
        val messages: List<Message>,
        val typing: Set<String>,
        val summaries: List<ChatSummary>,
        val users: Map<String, User>,
        val mentionCandidates: List<User>,
    )

    private data class ComposerInputs(
        val reply: String?,
        val edit: String?,
        val recording: RecordingPhase?,
        val text: String,
    )

    private data class SelectionInputs(
        val selecting: Boolean,
        val selected: Set<String>,
        val action: String?,
        val pinnedIdx: Int,
        val jump: String?,
    )

    private data class FlashInputs(val flashId: String?, val tick: Int, val marker: Int)

    private val chatStream = combine(
        chatRepository.observeChat(chatId),
        chatRepository.observeMessages(chatId),
        chatRepository.observeTyping(chatId),
    ) { chat, messages, typing -> ChatMessages(chat, messages, typing) }

    private val baseState = combine(
        chatStream,
        chatRepository.observeChatSummaries(),
        contactsRepository.observeContacts(),
        chatRepository.observeMentionCandidates(chatId),
    ) { chatMsg, summaries, contacts, mentions ->
        val chat = chatMsg.chat
        val messages = chatMsg.messages
        val typing = chatMsg.typing
        if (!draftInited.value) {
            draftInited.value = true
            draftText.value = summaries.firstOrNull { it.chatId == chatId }?.draft.orEmpty()
        }
        if (!markerCaptured.value) {
            markerCaptured.value = true
            val unread = summaries.firstOrNull { it.chatId == chatId }?.unreadCount ?: 0
            unreadMarker.value = unread
            if (unread > 0) viewModelScope.launch { chatRepository.markChatRead(chatId) }
        }
        Base(
            chat = chat,
            messages = messages,
            typing = typing,
            summaries = summaries,
            users = contacts.associateBy { it.id },
            mentionCandidates = mentions,
        )
    }

    private val composerInputs = combine(
        replyId,
        editId,
        recordingPhase,
        draftText,
    ) { r, e, rec, t -> ComposerInputs(r, e, rec, t) }

    private val selectionInputs = combine(
        selectionMode,
        selectedIds,
        actionMessageId,
        pinnedDisplayIndex,
        jumpTargetId,
    ) { s, ids, a, p, j -> SelectionInputs(s, ids, a, p, j) }

    private val flashInputs = combine(
        flashMessageId,
        flashTick,
        unreadMarker,
    ) { f, t, m -> FlashInputs(f, t, m) }

    private val scrollInputs = combine(unseenCount, atBottom) { unseen, bottom ->
        ScrollInputs(unseen, bottom)
    }

    private val uiParts = combine(
        baseState,
        composerInputs,
        selectionInputs,
        flashInputs,
        scrollInputs,
    ) { base, comp, sel, flash, scroll ->
        val chat = base.chat
        val messagesById = base.messages.associateBy { it.id }

        val composer = when {
            chat.isBlocked -> ComposerUiState.Blocked
            chat.isGroup && !chat.permissions.sendMessages -> ComposerUiState.ReadOnly
            comp.recording == RecordingPhase.Active -> ComposerUiState.Recording
            comp.recording == RecordingPhase.Locked -> ComposerUiState.LockedRecording
            comp.reply != null -> {
                val target = messagesById[comp.reply]
                ComposerUiState.Reply(
                    messageId = comp.reply,
                    senderName = target?.senderId?.let { base.users[it]?.displayName }
                        ?: base.users[chat.participantIds.firstOrNull { it != "me" }]?.displayName
                        ?: "Message",
                    excerpt = target?.let { excerptOf(it) }.orEmpty(),
                )
            }
            comp.edit != null -> ComposerUiState.Edit(
                messageId = comp.edit,
                excerpt = messagesById[comp.edit]?.let { excerptOf(it) }.orEmpty(),
            )
            comp.text.isBlank() -> ComposerUiState.Idle
            else -> ComposerUiState.Typing
        }

        ConversationUiState(
            loading = false,
            chat = chat,
            rows = buildConversationRows(
                messages = base.messages,
                users = base.users,
                unreadMarker = flash.marker,
                typingIds = base.typing,
            ),
            users = base.users,
            otherUnread = base.summaries
                .filter { it.chatId != chatId && !it.isArchived }
                .sumOf { it.unreadCount },
            draftText = comp.text,
            unseenCount = scroll.unseen,
            atBottom = scroll.bottom,
            composer = composer,
            selectionMode = sel.selecting,
            selectedMessageIds = sel.selected,
            actionMessageId = sel.action,
            pinnedDisplayIndex = sel.pinnedIdx,
            jumpTargetId = sel.jump,
            flashMessageId = flash.flashId,
            flashTick = flash.tick,
            mentionMembers = base.mentionCandidates,
            selectableChats = base.summaries.filter { it.chatId != chatId && !it.isArchived },
        )
    }

    val uiState: StateFlow<ConversationUiState> = uiParts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ConversationUiState(),
    )

    /** One-line excerpt used by reply/edit bars, quotes and the banner. */
    private fun excerptOf(message: Message): String {
        if (message.isDeleted) return ""
        return when (val content = message.content) {
            is MessageContent.Text -> content.text
            else -> content.text.ifBlank { MessageLabels.typeLabel(message.type).orEmpty() }
        }
    }

    /* ---------- Lifecycle / base UI events (M4a) ---------- */

    fun onScreenOpened() {
        viewModelScope.launch { chatRepository.setActiveConversation(chatId) }
    }

    fun onScreenClosed() {
        viewModelScope.launch { chatRepository.setActiveConversation(null) }
    }

    fun onDraftChange(text: String) {
        draftText.value = text
    }

    fun scrolledToBottom() {
        atBottom.value = true
        unseenCount.value = 0
        unreadMarker.value = 0
        viewModelScope.launch { chatRepository.markChatRead(chatId) }
    }

    fun scrolledUp() {
        atBottom.value = false
    }

    /** The user scrolled past the unread divider -> dismiss it. */
    fun unreadDividerPassed() {
        unreadMarker.value = 0
    }

    fun retry(messageId: String) {
        viewModelScope.launch { chatRepository.retryMessage(messageId) }
    }

    fun toggleMuted() {
        viewModelScope.launch {
            val muted = uiState.value.chat?.isMuted == true
            chatRepository.setMuted(chatId, !muted)
        }
    }

    /* ---------- Send / edit / reply (M4b) ---------- */

    fun send() {
        val text = draftText.value.trim()
        val editing = editId.value
        if (text.isEmpty()) return
        unreadMarker.value = 0
        if (editing != null) {
            val id = editing
            draftText.value = ""
            editId.value = null
            viewModelScope.launch { chatRepository.editMessage(id, text) }
            return
        }
        val replying = replyId.value
        draftText.value = ""
        replyId.value = null
        viewModelScope.launch {
            chatRepository.sendText(chatId, text, replyToMessageId = replying)
        }
    }

    fun beginReply(messageId: String) {
        replyId.value = messageId
        editId.value = null
    }

    fun beginEdit(messageId: String) {
        val message = uiState.value.rows
            .filterIsInstance<ConversationRow.MessageItem>()
            .firstOrNull { it.message.id == messageId }?.message ?: return
        replyId.value = null
        editId.value = messageId
        draftText.value = message.content.takeIf { it !is MessageContent.System }?.let {
            excerptOf(message)
        }.orEmpty()
    }

    fun closeComposerBar() {
        editId.value = null
        replyId.value = null
    }

    /* ---------- Media & rich sends (M4c S24-S26) ---------- */

    /** Sends picked/captured photos as one grid message (1..10 uris). */
    fun sendImages(uris: List<String>, caption: String?) {
        if (uris.isEmpty()) return
        val replying = replyId.value
        replyId.value = null
        editId.value = null
        draftText.value = ""
        unreadMarker.value = 0
        viewModelScope.launch {
            chatRepository.sendImages(
                chatId,
                uris,
                caption = caption?.trim()?.takeIf { it.isNotEmpty() },
                replyToMessageId = replying,
            )
        }
    }

    /** Sends one video (camera simulation / picker) as a video message. */
    fun sendVideo(uri: String, durationSeconds: Int, caption: String?) {
        val replying = replyId.value
        replyId.value = null
        editId.value = null
        draftText.value = ""
        unreadMarker.value = 0
        viewModelScope.launch {
            chatRepository.sendVideo(
                chatId,
                uri,
                durationSeconds = durationSeconds,
                caption = caption?.trim()?.takeIf { it.isNotEmpty() },
                replyToMessageId = replying,
            )
        }
    }

    /** Creates a poll message (2..10 options, optional quiz/multi/anonymous). */
    fun sendPoll(
        question: String,
        options: List<String>,
        allowsMultiple: Boolean,
        isAnonymous: Boolean,
        isQuiz: Boolean,
        correctOptionIndex: Int?,
    ) {
        val cleaned = options.map { it.trim() }.filter { it.isNotEmpty() }
        val q = question.trim()
        if (q.isEmpty() || cleaned.size < 2) return
        val replying = replyId.value
        replyId.value = null
        editId.value = null
        draftText.value = ""
        unreadMarker.value = 0
        viewModelScope.launch {
            chatRepository.sendPoll(
                chatId,
                question = q,
                options = cleaned,
                allowsMultiple = allowsMultiple,
                isAnonymous = isAnonymous,
                isQuiz = isQuiz,
                correctOptionIndex = correctOptionIndex,
                replyToMessageId = replying,
            )
        }
    }

    /** Toggles the current user's vote on a poll (toggle-style per option). */
    fun votePoll(messageId: String, optionIndexes: List<Int>) {
        viewModelScope.launch { chatRepository.votePoll(messageId, optionIndexes) }
    }

    /** Removes the current user's votes from a poll. */
    fun retractVote(messageId: String) {
        viewModelScope.launch { chatRepository.retractVote(messageId) }
    }

    /** Shares one Pulse contact card (User carries the card fields). */
    fun sendContact(user: User) {
        val replying = replyId.value
        replyId.value = null
        editId.value = null
        draftText.value = ""
        unreadMarker.value = 0
        viewModelScope.launch {
            chatRepository.sendContact(
                chatId,
                userId = user.id,
                displayName = user.displayName,
                phone = user.phone,
                username = user.username,
                replyToMessageId = replying,
            )
        }
    }

    /** Sends a SAF-opened document/audio as a FILE message (uri persisted). */
    fun sendFile(uri: String, name: String, sizeBytes: Long, mimeType: String) {
        val replying = replyId.value
        replyId.value = null
        editId.value = null
        draftText.value = ""
        unreadMarker.value = 0
        viewModelScope.launch {
            chatRepository.sendFile(
                chatId,
                uri = uri,
                name = name,
                sizeBytes = sizeBytes,
                mimeType = mimeType,
                replyToMessageId = replying,
            )
        }
    }

    /** Sends a static or live location card (mock coordinates). */
    fun sendLocation(
        latitude: Double,
        longitude: Double,
        address: String,
        isLive: Boolean,
        liveDurationMs: Long?,
    ) {
        val replying = replyId.value
        replyId.value = null
        editId.value = null
        draftText.value = ""
        unreadMarker.value = 0
        viewModelScope.launch {
            chatRepository.sendLocation(
                chatId,
                latitude = latitude,
                longitude = longitude,
                address = address,
                isLive = isLive,
                liveDurationMs = liveDurationMs,
                replyToMessageId = replying,
            )
        }
    }

    /* ---------- Voice recording (M4b) ---------- */

    fun recordStart() {
        if (recordingPhase.value == null) recordingPhase.value = RecordingPhase.Active
    }

    fun recordCancel() {
        recordingPhase.value = null
    }

    fun recordLock() {
        recordingPhase.value = RecordingPhase.Locked
    }

    fun recordFinish(durationMs: Long, samples: List<Int>) {
        if (durationMs < 500L) {
            recordingPhase.value = null
            return
        }
        val replying = replyId.value
        recordingPhase.value = null
        replyId.value = null
        editId.value = null
        viewModelScope.launch {
            chatRepository.sendVoice(chatId, durationMs, samples, replyToMessageId = replying)
        }
    }

    /* ---------- Reactions / star / pin (M4b) ---------- */

    fun toggleReaction(messageId: String, emoji: String) {
        viewModelScope.launch { chatRepository.toggleReaction(messageId, emoji) }
    }

    fun toggleStar(messageId: String) {
        viewModelScope.launch {
            val message = rowMessage(messageId) ?: return@launch
            chatRepository.setStarred(messageId, !message.isStarred)
        }
    }

    fun pinMessage(messageId: String) {
        viewModelScope.launch { chatRepository.pinMessage(chatId, messageId) }
    }

    fun unpinMessage(messageId: String) {
        viewModelScope.launch { chatRepository.unpinMessage(chatId, messageId) }
    }

    fun deleteForMe(messageIds: List<String>) {
        viewModelScope.launch {
            messageIds.forEach { chatRepository.deleteMessage(it, forEveryone = false) }
            exitSelection()
        }
    }

    fun deleteForEveryone(messageIds: List<String>) {
        viewModelScope.launch {
            messageIds.forEach { chatRepository.deleteMessage(it, forEveryone = true) }
            exitSelection()
        }
    }

    fun setBlocked(blocked: Boolean) {
        viewModelScope.launch { chatRepository.setBlocked(chatId, blocked) }
    }

    fun forwardMessages(messageIds: List<String>, targetChatIds: List<String>, comment: String) {
        viewModelScope.launch {
            chatRepository.forwardMessages(messageIds, targetChatIds, comment.ifBlank { null })
            exitSelection()
        }
    }

    private fun rowMessage(id: String): Message? =
        uiState.value.rows
            .filterIsInstance<ConversationRow.MessageItem>()
            .firstOrNull { it.message.id == id }
            ?.message

    /* ---------- Long-press overlay (M4b) ---------- */

    fun openActions(messageId: String) {
        actionMessageId.value = messageId
    }

    fun dismissActions() {
        actionMessageId.value = null
    }

    /* ---------- Multi-select (M4b) ---------- */

    fun enterSelection(messageId: String) {
        selectionMode.value = true
        selectedIds.value = selectedIds.value + messageId
        actionMessageId.value = null
    }

    fun exitSelection() {
        selectionMode.value = false
        selectedIds.value = emptySet()
        actionMessageId.value = null
    }

    fun toggleSelect(messageId: String) {
        selectedIds.value =
            if (messageId in selectedIds.value) selectedIds.value - messageId
            else selectedIds.value + messageId
    }

    fun starSelected() {
        viewModelScope.launch {
            val ids = uiState.value.selectedMessageIds
            val selected = ids.mapNotNull { rowMessage(it) }
            val allStarred = selected.isNotEmpty() && selected.all { it.isStarred }
            ids.forEach { id -> chatRepository.setStarred(id, !allStarred) }
        }
    }

    /* ---------- Pinned banner / scroll targets (M4b) ---------- */

    /** Cycles the banner to the next valid pin and jumps/flashes it. */
    fun pinnedBannerTap() {
        val valid = uiState.value.validPinnedIds
        if (valid.isEmpty()) return
        val index = pinnedDisplayIndex.value % valid.size
        jumpToMessage(valid[index])
        pinnedDisplayIndex.value = index + 1
    }

    fun jumpToMessage(messageId: String) {
        jumpTargetId.value = messageId
        flash(messageId)
    }

    fun consumeJump() {
        jumpTargetId.value = null
    }

    private fun flash(messageId: String) {
        flashMessageId.value = messageId
        flashTick.value = flashTick.value + 1
        viewModelScope.launch {
            delay(700)
            if (flashMessageId.value == messageId) flashMessageId.value = null
        }
    }

    fun directPeer(chat: Chat, users: Map<String, User>): User? {
        if (chat.kind != ChatKind.Direct) return null
        val peerId = chat.participantIds.firstOrNull { it != "me" } ?: return null
        return users[peerId]
    }
}
