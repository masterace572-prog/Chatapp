package com.pulse.messenger.ui.screens.conversation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulse.messenger.domain.model.Chat
import com.pulse.messenger.domain.model.ChatKind
import com.pulse.messenger.domain.model.ChatSummary
import com.pulse.messenger.domain.model.Message
import com.pulse.messenger.domain.model.User
import com.pulse.messenger.domain.repository.ChatRepository
import com.pulse.messenger.domain.repository.ContactsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
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
 * Conversation screen state (S23 core). The chat header, message rows (with
 * day/unread markers), typing set, draft and the unread totals all derive
 * from repository flows; the UI never joins data itself.
 *
 * The unread divider is captured from the first summaries emission (the
 * badge is cleared immediately after), so it shows exactly once per open.
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
) {
    val peerId: String?
        get() = chat?.participantIds?.firstOrNull { it != "me" }
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
 *
 * @param unreadMarker how many trailing messages form the unread block whose
 *   divider is still shown (0 = hidden).
 */
fun buildConversationRows(
    messages: List<Message>,
    users: Map<String, User>,
    unreadMarker: Int,
    typingIds: Set<String> = emptySet(),
): List<ConversationRow> {
    if (messages.isEmpty()) return emptyList()
    val ascending = messages.sortedBy { it.sentAtMillis }

    // Run geometry (ascending): same sender within 2 minutes = one run.
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

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class ConversationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository,
    contactsRepository: ContactsRepository,
) : ViewModel() {

    private val chatId: String = savedStateHandle.get<String>("chatId").orEmpty()

    /** UI-side composer text; initialised from the repository draft. */
    private val draftText = MutableStateFlow("")
    private val draftInited = MutableStateFlow(false)

    private val atBottom = MutableStateFlow(true)
    private val unseenCount = MutableStateFlow(0)
    private val unreadMarker = MutableStateFlow(0)
    private val markerCaptured = MutableStateFlow(false)
    private var lastMessageCount = -1

    init {
        // Draft persistence: debounce user edits into the repository.
        viewModelScope.launch {
            draftText
                .debounce(350)
                .distinctUntilChanged()
                .collect { text ->
                    if (draftInited.value) chatRepository.setDraft(chatId, text)
                }
        }

        // Unseen pill: count messages that arrive while scrolled up.
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

    private data class Base(
        val chat: Chat,
        val messages: List<Message>,
        val typing: Set<String>,
        val summaries: List<ChatSummary>,
        val users: Map<String, User>,
    )

    private val baseState = combine(
        chatRepository.observeChat(chatId),
        chatRepository.observeMessages(chatId),
        chatRepository.observeTyping(chatId),
        chatRepository.observeChatSummaries(),
        contactsRepository.observeContacts(),
    ) { chat: Chat, messages: List<Message>, typing: Set<String>,
        summaries: List<ChatSummary>, contacts: List<User> ->
        // Draft restore happens once, from the repository value.
        if (!draftInited.value) {
            draftInited.value = true
            draftText.value = summaries.firstOrNull { it.chatId == chatId }?.draft.orEmpty()
        }
        // Unread divider: capture exactly once per open, then clear the badge.
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
        )
    }

    private val controls = combine(
        atBottom,
        unseenCount,
        unreadMarker,
    ) { bottom: Boolean, unseen: Int, marker: Int ->
        Triple(bottom, unseen, marker)
    }

    val uiState: StateFlow<ConversationUiState> = combine(
        baseState,
        controls,
        draftText,
    ) { base: Base, control: Triple<Boolean, Int, Int>, draft: String ->
        val chat = base.chat
        ConversationUiState(
            loading = false,
            chat = chat,
            rows = buildConversationRows(
                messages = base.messages,
                users = base.users,
                unreadMarker = control.third,
                typingIds = base.typing,
            ),
            users = base.users,
            otherUnread = base.summaries
                .filter { it.chatId != chatId && !it.isArchived }
                .sumOf { it.unreadCount },
            draftText = draft,
            unseenCount = control.second,
            atBottom = control.first,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ConversationUiState(),
    )

    /* ---------- UI events ---------- */

    fun onScreenOpened() {
        viewModelScope.launch { chatRepository.setActiveConversation(chatId) }
    }

    fun onScreenClosed() {
        viewModelScope.launch { chatRepository.setActiveConversation(null) }
    }

    fun onDraftChange(text: String) {
        draftText.value = text
    }

    fun send() {
        val text = draftText.value.trim()
        if (text.isEmpty()) return
        draftText.value = ""
        unreadMarker.value = 0
        viewModelScope.launch {
            chatRepository.sendText(chatId, text)
        }
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

    /** Called when the list is pinned to the bottom (also on FAB tap). */
    fun scrolledToBottom() {
        atBottom.value = true
        unseenCount.value = 0
        unreadMarker.value = 0
        viewModelScope.launch { chatRepository.markChatRead(chatId) }
    }

    fun scrolledUp() {
        atBottom.value = false
    }

    /** Header helpers (status text/tone) resolved from chat + users. */
    fun directPeer(chat: Chat, users: Map<String, User>): User? {
        if (chat.kind != ChatKind.Direct) return null
        val peerId = chat.participantIds.firstOrNull { it != "me" } ?: return null
        return users[peerId]
    }
}
