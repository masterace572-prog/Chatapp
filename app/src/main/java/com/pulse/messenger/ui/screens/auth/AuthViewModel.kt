package com.pulse.messenger.ui.screens.auth

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulse.messenger.R
import com.pulse.messenger.domain.model.CountryCode
import com.pulse.messenger.domain.model.GoogleAccount
import com.pulse.messenger.domain.model.OtpChannel
import com.pulse.messenger.domain.model.ProfileStage
import com.pulse.messenger.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * One-shot navigation events emitted after a successful async auth action.
 * A single router in PulseApp maps each event to a destination (UDF: the
 * ViewModel never knows navigation routes).
 */
sealed interface AuthEvent {
    data object OtpSent : AuthEvent
    data object SignInComplete : AuthEvent
    data object EmailVerifiedNext : AuthEvent
    data object ForgotEmailSent : AuthEvent
    data object ResetComplete : AuthEvent
    data object SignupPasswordSaved : AuthEvent
    data object GoogleNewUser : AuthEvent
    data class StageSaved(val stage: ProfileStage) : AuthEvent
    data object AccountCreated : AuthEvent
}

/** Snackbar notices requested by auth actions (kept tiny and centralised). */
enum class AuthSnackbar(@StringRes val resId: Int) {
    CodeSent(R.string.snack_code_sent),
    ResetLinkSent(R.string.snack_reset_link_sent),
    AddedToContacts(R.string.snack_added_contact),
    InviteSent(R.string.snack_invite_sent),
    ComingLater(R.string.snack_coming_later),
    NoEmailApp(R.string.snack_no_email_app),
    AccountExists(R.string.snack_account_exists),
}

data class AuthUiState(
    val busy: Boolean = false,
    val error: String? = null,
)

/** Live result of the S13 username availability check. */
sealed interface UsernameCheck {
    data object Idle : UsernameCheck
    data object Checking : UsernameCheck
    data object Available : UsernameCheck
    data class Taken(val message: String) : UsernameCheck
}

/**
 * Shared feature ViewModel for the whole onboarding/auth journey (S02-S17),
 * scoped to the Activity so one instance serves every step. It owns all async
 * auth work; screens stay pure UI + validation.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    private val _snackbar = MutableSharedFlow<AuthSnackbar>(extraBufferCapacity = 8)
    val snackbar: SharedFlow<AuthSnackbar> = _snackbar.asSharedFlow()

    private val _usernameCheck = MutableStateFlow<UsernameCheck>(UsernameCheck.Idle)
    val usernameCheck: StateFlow<UsernameCheck> = _usernameCheck.asStateFlow()

    private var usernameJob: Job? = null

    /* ---------- Cross-step context (survives navigation between steps) ---------- */

    var enteredEmail: String = ""
        private set

    var enteredPhone: String = ""
        private set

    var otpChannel: OtpChannel = OtpChannel.LoginPhone
        private set

    var otpIdentifier: String = ""
        private set

    /** Google account chosen on S05 when it leads to profile setup. */
    var googleAccount: GoogleAccount? = null
        private set

    val googleFirstName: String? get() = googleAccount?.firstName
    val googleLastName: String? get() = googleAccount?.lastName
    val googleAvatarSeed: Int? get() = googleAccount?.avatarSeed

    /* ---------- Helpers ---------- */

    private fun setError(message: String?) {
        _uiState.value = _uiState.value.copy(error = message)
    }

    private suspend fun run(action: suspend () -> Unit) {
        _uiState.value = AuthUiState(busy = true, error = null)
        try {
            action()
        } finally {
            _uiState.value = _uiState.value.copy(busy = false)
        }
    }

    private fun fail(t: Throwable) = setError(t.message ?: "Something went wrong. Try again.")

    fun dismissError() = setError(null)

    /** Fire a UI notice through the shared snackbar channel. */
    fun notice(message: AuthSnackbar) {
        viewModelScope.launch { _snackbar.emit(message) }
    }

    /* ---------- Login (S03/S04) ---------- */

    fun onEmailEntered(email: String) {
        enteredEmail = email.trim()
        dismissError()
    }

    fun loginWithPassword(password: String) {
        viewModelScope.launch {
            run {
                authRepository.signInWithEmail(enteredEmail, password)
                    .onSuccess { _events.emit(AuthEvent.SignInComplete) }
                    .onFailure { fail(it) }
            }
        }
    }

    /* ---------- Phone login (S03b/S06) ---------- */

    fun loginWithPhone(countryCode: CountryCode, number: String) {
        enteredPhone = "${countryCode.prefix} $number"
        otpChannel = OtpChannel.LoginPhone
        otpIdentifier = enteredPhone
        viewModelScope.launch {
            run {
                authRepository.signInWithPhone(countryCode.prefix, number)
                    .onSuccess { _events.emit(AuthEvent.OtpSent) }
                    .onFailure { fail(it) }
            }
        }
    }

    /* ---------- OTP (S06/S11b) ---------- */

    fun verifyOtp(code: String) {
        viewModelScope.launch {
            run {
                authRepository.verifyOtp(otpChannel, code)
                    .onSuccess {
                        when (otpChannel) {
                            OtpChannel.LoginPhone -> _events.emit(AuthEvent.SignInComplete)
                            OtpChannel.EmailVerification -> _events.emit(AuthEvent.EmailVerifiedNext)
                        }
                    }
                    .onFailure { fail(it) }
            }
        }
    }

    fun resendOtp() {
        viewModelScope.launch {
            run {
                authRepository.resendOtp(otpChannel)
                    .onSuccess { _snackbar.emit(AuthSnackbar.CodeSent) }
                    .onFailure { fail(it) }
            }
        }
    }

    /* ---------- Google (S05) ---------- */

    fun signInWithGoogle(account: GoogleAccount) {
        viewModelScope.launch {
            run {
                authRepository.signInWithGoogle(account)
                    .onSuccess { result ->
                        if (result.isNewUser) {
                            googleAccount = account
                            enteredEmail = account.email
                            _events.emit(AuthEvent.GoogleNewUser)
                        } else {
                            _events.emit(AuthEvent.SignInComplete)
                        }
                    }
                    .onFailure { fail(it) }
            }
        }
    }

    /* ---------- Forgot / reset (S07-S10) ---------- */

    fun onForgotEmailEntered(email: String) {
        enteredEmail = email.trim()
        dismissError()
    }

    fun requestPasswordReset() {
        val email = enteredEmail
        viewModelScope.launch {
            run {
                authRepository.requestPasswordReset(email)
                    .onSuccess { _events.emit(AuthEvent.ForgotEmailSent) }
                    .onFailure { fail(it) }
            }
        }
    }

    fun resendResetLink() {
        viewModelScope.launch {
            run {
                authRepository.requestPasswordReset(enteredEmail)
                    .onSuccess { _snackbar.emit(AuthSnackbar.ResetLinkSent) }
                    .onFailure { fail(it) }
            }
        }
    }

    fun resetPassword(newPassword: String) {
        viewModelScope.launch {
            run {
                authRepository.resetPassword(newPassword)
                    .onSuccess { _events.emit(AuthEvent.ResetComplete) }
                    .onFailure { fail(it) }
            }
        }
    }

    /* ---------- Sign up (S11a-c) ---------- */

    fun onSignupEmailEntered(email: String) {
        enteredEmail = email.trim()
        dismissError()
    }

    fun signUpWithEmail() {
        val email = enteredEmail
        otpChannel = OtpChannel.EmailVerification
        otpIdentifier = email
        viewModelScope.launch {
            run {
                authRepository.signUpWithEmail(email)
                    .onSuccess { _events.emit(AuthEvent.OtpSent) }
                    .onFailure { fail(it) }
            }
        }
    }

    fun saveSignupPassword(password: String) {
        viewModelScope.launch {
            run {
                authRepository.savePassword(password)
                    .onSuccess { _events.emit(AuthEvent.SignupPasswordSaved) }
                    .onFailure { fail(it) }
            }
        }
    }

    /* ---------- Profile setup (S12-S15) ---------- */

    fun saveProfileName(firstName: String, lastName: String) {
        viewModelScope.launch {
            run {
                authRepository.saveProfile(firstName, lastName)
                    .onSuccess { _events.emit(AuthEvent.StageSaved(ProfileStage.Name)) }
                    .onFailure { fail(it) }
            }
        }
    }

    fun checkUsername(username: String) {
        usernameJob?.cancel()
        val trimmed = username.trim()
        if (trimmed.isEmpty() || trimmed.length < 3) {
            _usernameCheck.value = UsernameCheck.Idle
            return
        }
        _usernameCheck.value = UsernameCheck.Checking
        usernameJob = viewModelScope.launch {
            authRepository.checkUsernameAvailable(trimmed)
                .onSuccess { available ->
                    _usernameCheck.value =
                        if (available) UsernameCheck.Available
                        else UsernameCheck.Taken("That username is taken. Try another.")
                }
                .onFailure { _usernameCheck.value = UsernameCheck.Taken(it.message.orEmpty()) }
        }
    }

    fun saveUsername(username: String) {
        viewModelScope.launch {
            run {
                authRepository.saveUsername(username)
                    .onSuccess { _events.emit(AuthEvent.StageSaved(ProfileStage.Username)) }
                    .onFailure { fail(it) }
            }
        }
    }

    fun saveAvatarSeed(seed: Int, advance: Boolean = false) {
        viewModelScope.launch {
            run {
                authRepository.saveAvatarSeed(seed)
                    .onSuccess {
                        if (advance) _events.emit(AuthEvent.StageSaved(ProfileStage.Photo))
                    }
                    .onFailure { fail(it) }
            }
        }
    }

    fun saveBio(bio: String) {
        viewModelScope.launch {
            run {
                authRepository.saveBio(bio)
                    .onSuccess { _events.emit(AuthEvent.StageSaved(ProfileStage.Bio)) }
                    .onFailure { fail(it) }
            }
        }
    }

    /* ---------- Finish (S17) ---------- */

    fun completeOnboarding() {
        viewModelScope.launch {
            run {
                authRepository.completeOnboarding()
                    .onSuccess { _events.emit(AuthEvent.AccountCreated) }
                    .onFailure { fail(it) }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
