package com.pulse.messenger.ui.screens.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulse.messenger.domain.model.ChatFolder
import com.pulse.messenger.domain.model.ChatKind
import com.pulse.messenger.domain.repository.ChatRepository
import com.pulse.messenger.domain.repository.FoldersRepository
import com.pulse.messenger.ui.screens.chats.ChatFilters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Chat folders editor state (S22). */
data class FoldersUiState(
    val loading: Boolean = true,
    val folders: List<ChatFolder> = emptyList(),
    /** Number of chats each folder would include (live preview). */
    val includeCounts: Map<String, Int> = emptyMap(),
    /** Chat requested for deletion (id) - drives the confirm dialog. */
    val pendingDeleteId: String? = null,
) {
    val totalChats: Int get() = includeCounts.values.sum()
}

/** Draft for the create/edit folder sheet. */
data class FolderDraft(
    val id: String? = null,
    val name: String = "",
    val includeGroups: Boolean = false,
    val includePersonal: Boolean = false,
    val onlyUnread: Boolean = false,
) {
    val includeKinds: Set<ChatKind>
        get() = buildSet {
            if (includeGroups) add(ChatKind.Group)
            if (includePersonal) add(ChatKind.Direct)
        }

    val valid: Boolean get() = name.isNotBlank() && includeKinds.isNotEmpty()
}

@HiltViewModel
class FoldersViewModel @Inject constructor(
    private val foldersRepository: FoldersRepository,
    chatRepository: ChatRepository,
) : ViewModel() {

    private val chats = chatRepository.observeChatSummaries()
    private val pendingDelete = MutableStateFlow<String?>(null)

    val uiState: StateFlow<FoldersUiState> = combine(
        foldersRepository.observeFolders(),
        chats,
        pendingDelete,
    ) { folders, chatList, pending ->
        val counts = folders.associate { folder ->
            folder.id to ChatFilters.countIn(folder, chatList)
        }
        FoldersUiState(
            loading = false,
            folders = folders,
            includeCounts = counts,
            pendingDeleteId = pending,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FoldersUiState(),
    )

    fun createFolder(name: String, kinds: Set<ChatKind>, onlyUnread: Boolean) {
        if (name.isBlank() || kinds.isEmpty()) return
        viewModelScope.launch { foldersRepository.addFolder(name, kinds, onlyUnread) }
    }

    fun updateFolder(folder: ChatFolder) {
        viewModelScope.launch { foldersRepository.updateFolder(folder) }
    }

    fun moveUp(id: String) = viewModelScope.launch { foldersRepository.moveUp(id) }

    fun moveDown(id: String) = viewModelScope.launch { foldersRepository.moveDown(id) }

    fun requestDelete(id: String) {
        pendingDelete.value = id
    }

    fun cancelDelete() {
        pendingDelete.value = null
    }

    fun confirmDelete() {
        val id = pendingDelete.value ?: return
        pendingDelete.value = null
        viewModelScope.launch { foldersRepository.deleteFolder(id) }
    }
}
