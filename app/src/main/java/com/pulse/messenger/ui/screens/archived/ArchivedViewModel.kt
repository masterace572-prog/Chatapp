package com.pulse.messenger.ui.screens.archived

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulse.messenger.domain.model.ChatSummary
import com.pulse.messenger.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Archived chats state (S21) - backed by the same singleton mock store. */
data class ArchivedUiState(
    val loading: Boolean = true,
    val chats: List<ChatSummary> = emptyList(),
)

@HiltViewModel
class ArchivedViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
) : ViewModel() {

    val uiState: StateFlow<ArchivedUiState> = chatRepository.observeArchivedSummaries()
        .map { ArchivedUiState(loading = false, chats = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ArchivedUiState(),
        )

    fun unarchive(chatId: String) {
        viewModelScope.launch { chatRepository.setArchived(chatId, false) }
    }
}
