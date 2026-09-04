package com.pulse.messenger.domain.model

/** Dialling code for the S03b phone field (flags are rendered as ISO badges, never emoji). */
data class CountryCode(
    val iso: String,
    val name: String,
    val prefix: String,
)

/**
 * OTP channels used by the two mock OTP flows (PRD S06/S11b):
 *  - LoginPhone: code delivered to the phone during login (S03b -> S06)
 *  - EmailVerification: code delivered during sign-up email verification (S11b)
 */
enum class OtpChannel { LoginPhone, EmailVerification }

/**
 * Outcome of "Continue with Google" (S05). When [isNewUser] is true the UI
 * routes to profile setup (S12-S17); otherwise the user is already signed in.
 */
data class SignInResult(val user: User, val isNewUser: Boolean)

/**
 * A mock Google account offered by the account-picker sheet (S05).
 * Phase 2 replaces this with real Google One Tap / OAuth accounts.
 */
data class GoogleAccount(
    val id: String,
    val email: String,
    val name: String,
    val avatarSeed: Int,
) {
    val firstName: String get() = name.substringBefore(' ').ifBlank { name }
    val lastName: String get() = name.substringAfter(' ', "").trim()
}

/**
 * Ordered stages of the post-auth profile setup (S12-S15). The auth event
 * router advances the user through one screen per stage.
 */
enum class ProfileStage { Name, Username, Photo, Bio }
