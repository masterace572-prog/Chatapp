package com.pulse.messenger.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulse.messenger.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** S01 states. M2 replaces the unconditional splash exit with session-aware routing. */
sealed interface SplashUiState {
    data object Showing : SplashUiState
    data object Done : SplashUiState
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Showing)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    private val session = authRepository.observeSessionState()

    init {
        viewModelScope.launch {
            // PRD S01: splash ~1.2s. M2: route by session (LoggedIn -> Main,
            // LoggedOut -> Welcome, AppLock if enabled) right here.
            session.collect {
                delay(1200)
                _uiState.value = SplashUiState.Done
            }
        }
    }
}
