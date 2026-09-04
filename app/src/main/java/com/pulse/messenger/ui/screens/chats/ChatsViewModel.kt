package com.pulse.messenger.ui.screens.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulse.messenger.domain.model.ChatFolder
import com.pulse.messenger.domain.model.ChatSummary
import com.pulse.messenger.domain.repository.ChatRepository
import com.pulse.messenger.domain.repository.FoldersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Chats list state (S19): rows, filters, folders, selection and bulk actions.
 * All navigation stays in the caller; this VM only mutates repository state.
 */
data class ChatsUiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val chats: List<ChatSummary> = emptyList(),
    val archivedCount: Int = 0,
    val folders: List<ChatFolder> = emptyList(),
    val filterKind: ChatFilterKind = ChatFilterKind.All,
    /** Active user-folder id; null when a built-in filter is active. */
    val activeFolderId: String? = null,
    val selection: Set<String> = emptySet(),
    val askDeleteConfirmation: Boolean = false,
) {
    val selectionMode: Boolean get() = selection.isNotEmpty()

    /** Rows shown for the current filter selection. */
    val filteredChats: List<ChatSummary>
        get() {
            val folder = activeFolderId?.let { id -> folders.firstOrNull { it.id == id } }
            return chats.filter { summary ->
                when {
                    folder != null -> ChatFilters.matches(folder, summary)
                    else -> ChatFilters.matches(filterKind, summary)
                }
            }
        }
}

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    foldersRepository: FoldersRepository,
) : ViewModel() {

    private val filterKind = MutableStateFlow(ChatFilterKind.All)
    private val activeFolderId = MutableStateFlow<String?>(null)
    private val selection = MutableStateFlow<Set<String>>(emptySet())
    private val askDelete = MutableStateFlow(false)
    private val refreshing = MutableStateFlow(false)

    /** First combine stage: repository sources (rows, archive count, folders). */
    private data class BaseState(
        val chats: List<ChatSummary>,
        val archivedCount: Int,
        val folders: List<ChatFolder>,
    )

    /** Second combine stage: transient UI toggles. */
    private data class ControlsState(
        val kind: ChatFilterKind,
        val folderId: String?,
        val selection: Set<String>,
        val askDelete: Boolean,
        val refreshing: Boolean,
    )

    private val baseState = combine(
        chatRepository.observeChatSummaries(),
        chatRepository.observeArchivedSummaries(),
        foldersRepository.observeFolders(),
    ) { chats: List<ChatSummary>, archived: List<ChatSummary>, folders: List<ChatFolder> ->
        BaseState(chats = chats, archivedCount = archived.size, folders = folders)
    }

    private val controlsState = combine(
        filterKind,
        activeFolderId,
        selection,
        askDelete,
        refreshing,
    ) { kind: ChatFilterKind, folderId: String?, sel: Set<String>, ask: Boolean, ref: Boolean ->
        ControlsState(
            kind = kind,
            folderId = folderId,
            selection = sel,
            askDelete = ask,
            refreshing = ref,
        )
    }

    val uiState: StateFlow<ChatsUiState> = combine(
        baseState,
        controlsState,
    ) { base: BaseState, controls: ControlsState ->
        ChatsUiState(
            loading = false,
            refreshing = controls.refreshing,
            chats = base.chats,
            archivedCount = base.archivedCount,
            folders = base.folders,
            filterKind = controls.kind,
            activeFolderId = controls.folderId,
            selection = controls.selection,
            askDeleteConfirmation = controls.askDelete,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ChatsUiState(),
    )

    /* ---------- Filters & folders ---------- */

    fun selectFilter(kind: ChatFilterKind) {
        filterKind.value = kind
        activeFolderId.value = null
    }

    fun selectFolder(id: String) {
        activeFolderId.value = id
    }

    /* ---------- Selection ---------- */

    fun toggleSelection(chatId: String) {
        selection.value = selection.value.let { current ->
            if (chatId in current) current - chatId else current + chatId
        }
    }

    fun clearSelection() {
        selection.value = emptySet()
        askDelete.value = false
    }

    /* ---------- Single-row actions (swipes) ---------- */

    fun archive(chatId: String) = viewModelScope.launch {
        chatRepository.setArchived(chatId, true)
    }

    fun unarchive(chatId: String) = viewModelScope.launch {
        chatRepository.setArchived(chatId, false)
    }

    fun togglePin(chatId: String) {
        val summary = uiState.value.chats.firstOrNull { it.chatId == chatId } ?: return
        viewModelScope.launch { chatRepository.setPinned(chatId, !summary.isPinned) }
    }

    fun toggleMute(chatId: String) {
        val summary = uiState.value.chats.firstOrNull { it.chatId == chatId } ?: return
        viewModelScope.launch { chatRepository.setMuted(chatId, !summary.isMuted) }
    }

    /* ---------- Bulk actions (multi-select) ---------- */

    fun pinSelected(pinned: Boolean) = bulkAction { chatRepository.setPinned(it, pinned) }

    fun muteSelected(muted: Boolean) = bulkAction { chatRepository.setMuted(it, muted) }

    fun archiveSelected() = bulkAction { chatRepository.setArchived(it, true) }

    fun markSelectedRead() = bulkAction { chatRepository.markRead(it) }

    fun requestDeleteSelected() {
        if (selection.value.isNotEmpty()) askDelete.value = true
    }

    fun cancelDelete() {
        askDelete.value = false
    }

    fun confirmDeleteSelected() {
        val ids = selection.value.toList()
        viewModelScope.launch {
            chatRepository.deleteChats(ids)
            clearSelection()
        }
    }

    private inline fun bulkAction(crossinline action: suspend (String) -> Unit) {
        val ids = selection.value.toList()
        viewModelScope.launch {
            ids.forEach { action(it) }
            clearSelection()
        }
    }

    /* ---------- Refresh ---------- */

    fun refresh() {
        if (uiState.value.refreshing) return
        viewModelScope.launch {
            refreshing.value = true
            chatRepository.refresh()
            refreshing.value = false
        }
    }
}
