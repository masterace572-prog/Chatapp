package com.pulse.messenger.domain.model

/** App theme preference (S60 in a later milestone; DataStore-backed already). */
enum class ThemeMode { System, Light, Dark }

/** Authentication session visibility for the UI (M2 fills in real states). */
sealed interface SessionState {
    data object Unknown : SessionState
    data object LoggedOut : SessionState
    data class LoggedIn(val user: User) : SessionState
}
