package com.pulse.messenger.data.mock

import com.pulse.messenger.domain.model.GoogleAccount
import com.pulse.messenger.domain.model.OtpChannel
import com.pulse.messenger.domain.model.SessionState
import com.pulse.messenger.domain.model.SignInResult
import com.pulse.messenger.domain.model.User
import com.pulse.messenger.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mock auth (PRD §9):
 *  - any valid email + password >= 8 chars succeeds; the password "wrong" fails;
 *  - OTP "123456" succeeds, others fail;
 *  - Google picker succeeds; unknown Google email => new user -> profile setup;
 *  - profile fields accumulate in-memory and completeOnboarding() signs in.
 */
@Singleton
class MockAuthRepository @Inject constructor() : AuthRepository {

    private val session = MutableStateFlow<SessionState>(SessionState.LoggedOut)

    /** Accumulates profile setup (S12-S15) until completeOnboarding(). */
    private data class PendingProfile(
        var firstName: String = "",
        var lastName: String = "",
        var username: String = "",
        var bio: String = "",
        var avatarSeed: Int? = null,
    )

    private var pending = PendingProfile()
    private var googleAccount: GoogleAccount? = null

    private val existingGoogleEmails = mapOf(
        "aarav.kapoor@gmail.com" to User(
            id = "me", firstName = "Aarav", lastName = "Kapoor",
            username = "aaravk", phone = "+91 98110 90000",
            bio = "Building Pulse.", avatarSeed = 3,
        ),
    )

    private val takenEmails = setOf("noah.k@example.com", "mira@example.com")
    private val blockedUsernames =
        setOf("admin", "root", "pulse", "support", "test", "help", "official", "you")

    override fun observeSessionState(): Flow<SessionState> = session

    override suspend fun signInWithEmail(email: String, password: String): Result<User> {
        Simulator.networkDelay()
        val validEmail = email.isNotBlank() && email.contains('@') && email.contains('.')
        if (!validEmail) return Result.failure(IllegalArgumentException("Enter a valid email address."))
        if (password.length < 8) return Result.failure(IllegalArgumentException("Password must be at least 8 characters."))
        if (password == "wrong") return Result.failure(IllegalArgumentException("Wrong password. Try again."))
        val local = email.substringBefore('@').replace(Regex("[._-]+"), " ").trim()
        val user = User(
            id = "me",
            firstName = local.replaceFirstChar { it.uppercase() },
            username = email.substringBefore('@').lowercase(),
            phone = null,
            bio = null,
            avatarSeed = email.hashCode(),
        )
        session.value = SessionState.LoggedIn(user)
        return Result.success(user)
    }

    override suspend fun signInWithPhone(countryCode: String, phoneNumber: String): Result<Unit> {
        Simulator.networkDelay()
        val digits = phoneNumber.filter(Char::isDigit)
        return if (digits.length >= 7) Result.success(Unit)
        else Result.failure(IllegalArgumentException("Enter a valid phone number."))
    }

    override suspend fun signInWithGoogle(account: GoogleAccount): Result<SignInResult> {
        Simulator.networkDelay()
        val existing = existingGoogleEmails[account.email.lowercase()]
        return if (existing != null) {
            session.value = SessionState.LoggedIn(existing)
            Result.success(SignInResult(user = existing, isNewUser = false))
        } else {
            googleAccount = account
            pending = PendingProfile(
                firstName = account.firstName,
                lastName = account.lastName,
                avatarSeed = account.avatarSeed,
            )
            val draft = User(
                id = "me",
                firstName = account.firstName,
                lastName = account.lastName,
                username = account.email.substringBefore('@'),
                avatarSeed = account.avatarSeed,
            )
            Result.success(SignInResult(user = draft, isNewUser = true))
        }
    }

    override suspend fun verifyOtp(channel: OtpChannel, code: String): Result<Unit> {
        Simulator.networkDelay()
        if (code != "123456") {
            return Result.failure(IllegalArgumentException("Incorrect code. Try again."))
        }
        if (channel == OtpChannel.LoginPhone) {
            // Phone login completes here (S06 -> Main).
            val user = User(
                id = "me", firstName = "You", username = "you",
                phone = "+91 98110 00000", avatarSeed = 1,
            )
            session.value = SessionState.LoggedIn(user)
        }
        return Result.success(Unit)
    }

    override suspend fun resendOtp(channel: OtpChannel): Result<Unit> {
        Simulator.shortDelay()
        return Result.success(Unit)
    }

    override suspend fun signUpWithEmail(email: String): Result<Unit> {
        Simulator.networkDelay()
        val normalized = email.lowercase()
        val validEmail = normalized.isNotBlank() && normalized.contains('@') && normalized.contains('.')
        if (!validEmail) return Result.failure(IllegalArgumentException("Enter a valid email address."))
        if (normalized in takenEmails) {
            return Result.failure(IllegalArgumentException("An account with this email already exists. Log in instead."))
        }
        pending.firstName = ""
        pending.lastName = ""
        googleAccount = null
        return Result.success(Unit)
    }

    override suspend fun savePassword(password: String): Result<Unit> {
        Simulator.shortDelay()
        return if (password.length >= 8) Result.success(Unit)
        else Result.failure(IllegalArgumentException("Password must be at least 8 characters."))
    }

    override suspend fun requestPasswordReset(email: String): Result<Unit> {
        Simulator.networkDelay()
        val valid = email.isNotBlank() && email.contains('@') && email.contains('.')
        return if (valid) Result.success(Unit)
        else Result.failure(IllegalArgumentException("Enter a valid email address."))
    }

    override suspend fun resetPassword(newPassword: String): Result<Unit> {
        Simulator.networkDelay()
        return if (newPassword.length >= 8) Result.success(Unit)
        else Result.failure(IllegalArgumentException("Password must be at least 8 characters."))
    }

    override suspend fun checkUsernameAvailable(username: String): Result<Boolean> {
        Simulator.shortDelay()
        val candidate = username.trim().lowercase()
        val valid = candidate.matches(Regex("[a-z0-9_.]{3,24}"))
        if (!valid) return Result.failure(IllegalArgumentException("3-24 characters: letters, numbers, _ and ."))
        return Result.success(candidate !in blockedUsernames)
    }

    override suspend fun saveProfile(firstName: String, lastName: String): Result<Unit> {
        Simulator.shortDelay()
        pending.firstName = firstName.trim()
        pending.lastName = lastName.trim()
        return Result.success(Unit)
    }

    override suspend fun saveAvatarSeed(seed: Int): Result<Unit> {
        Simulator.shortDelay()
        pending.avatarSeed = seed
        return Result.success(Unit)
    }

    override suspend fun saveUsername(username: String): Result<Unit> {
        Simulator.shortDelay()
        pending.username = username.trim().removePrefix("@")
        return Result.success(Unit)
    }

    override suspend fun saveBio(bio: String): Result<Unit> {
        Simulator.shortDelay()
        pending.bio = bio.trim()
        return Result.success(Unit)
    }

    override suspend fun completeOnboarding(): Result<User> {
        Simulator.networkDelay()
        val google = googleAccount
        val firstName = pending.firstName.ifBlank { google?.firstName ?: "You" }
        val lastName = pending.lastName.ifBlank { google?.lastName.orEmpty() }
        val user = User(
            id = "me",
            firstName = firstName,
            lastName = lastName,
            username = pending.username.ifBlank {
                firstName.lowercase().filter { it.isLetterOrDigit() }.ifBlank { "you" }
            },
            phone = null,
            bio = pending.bio.ifBlank { null },
            avatarSeed = pending.avatarSeed ?: google?.avatarSeed ?: firstName.hashCode(),
        )
        session.value = SessionState.LoggedIn(user)
        return Result.success(user)
    }

    override suspend fun signOut(): Result<Unit> {
        Simulator.shortDelay()
        pending = PendingProfile()
        googleAccount = null
        session.value = SessionState.LoggedOut
        return Result.success(Unit)
    }
}
