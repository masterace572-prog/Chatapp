package com.pulse.messenger.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulse.messenger.domain.model.SessionState
import com.pulse.messenger.domain.repository.AuthRepository
import com.pulse.messenger.ui.navigation.PulseRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/** S01 states. */
sealed interface SplashUiState {
    data object Showing : SplashUiState
    /** [target] is the first destination after the brand pause. */
    data class Done(val target: String) : SplashUiState
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Showing)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // PRD S01: ~1.2s brand pause, then route by session:
            // LoggedIn -> Main shell, LoggedOut/Unknown -> Welcome (S02).
            val target = authRepository.observeSessionState()
                .map { state ->
                    when (state) {
                        is SessionState.LoggedIn -> PulseRoutes.MAIN
                        else -> PulseRoutes.WELCOME
                    }
                }
                .first()
            delay(1200)
            _uiState.value = SplashUiState.Done(target)
        }
    }
}
