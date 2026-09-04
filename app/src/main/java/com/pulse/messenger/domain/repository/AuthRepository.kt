package com.pulse.messenger.domain.repository

import com.pulse.messenger.domain.model.GoogleAccount
import com.pulse.messenger.domain.model.OtpChannel
import com.pulse.messenger.domain.model.SessionState
import com.pulse.messenger.domain.model.SignInResult
import com.pulse.messenger.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Auth contract (PRD §5 - interfaces only; Phase 1 mock, Phase 2 Supabase).
 *
 * M2 surface covering the S03-S17 journeys:
 *  - email/password login (S03/S04)
 *  - phone login + OTP (S03b/S06)
 *  - Google account picker (S05)
 *  - forgot/reset password (S07-S10)
 *  - sign-up email + OTP + password (S11a-c)
 *  - profile setup persistence (S12-S15) + onboarding completion (S17)
 *
 * Mock rules (PRD §9): any valid email + password >= 8 chars succeeds except
 * the literal password "wrong"; OTP code 123456 succeeds, anything else fails.
 */
interface AuthRepository {

    /** Session stream; Splash (S01) routes on it, Main reads it for "me". */
    fun observeSessionState(): Flow<SessionState>

    /** S03 -> S04. Completes the session when valid. */
    suspend fun signInWithEmail(email: String, password: String): Result<User>

    /** S03b. Sends the OTP (mock); session completes at verifyOtp. */
    suspend fun signInWithPhone(countryCode: String, phoneNumber: String): Result<Unit>

    /** S05. Existing account completes the session; a new one returns isNewUser. */
    suspend fun signInWithGoogle(account: GoogleAccount): Result<SignInResult>

    /** S06 / S11b. Completes phone login or verifies the sign-up email. */
    suspend fun verifyOtp(channel: OtpChannel, code: String): Result<Unit>

    /** S06 / S11b resend with a fresh mock code. */
    suspend fun resendOtp(channel: OtpChannel): Result<Unit>

    /** S11a. Starts email sign-up (mock code is sent on success). */
    suspend fun signUpWithEmail(email: String): Result<Unit>

    /** S11c after email verification. */
    suspend fun savePassword(password: String): Result<Unit>

    /** S07. Requests a reset link; always "delivers" for a valid email. */
    suspend fun requestPasswordReset(email: String): Result<Unit>

    /** S09. Accepts the new password. */
    suspend fun resetPassword(newPassword: String): Result<Unit>

    /** S13. Live username availability (mock). */
    suspend fun checkUsernameAvailable(username: String): Result<Boolean>

    /** S12/S14/S15 profile builders accumulated until completeOnboarding. */
    suspend fun saveProfile(firstName: String, lastName: String): Result<Unit>
    suspend fun saveAvatarSeed(seed: Int): Result<Unit>
    suspend fun saveUsername(username: String): Result<Unit>
    suspend fun saveBio(bio: String): Result<Unit>

    /** S17. Finishes onboarding -> session becomes LoggedIn. */
    suspend fun completeOnboarding(): Result<User>

    suspend fun signOut(): Result<Unit>
}
