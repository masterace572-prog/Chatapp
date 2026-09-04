package com.pulse.messenger.domain.model

/**
 * A person on Pulse (PRD §5 domain model). Phase 1 stores no photos - the UI
 * derives initials + a deterministic muted tone from name/username.
 * Domain stays UI-free on purpose.
 */
data class User(
    val id: String,
    val firstName: String,
    val lastName: String = "",
    val username: String,
    val phone: String? = null,
    val bio: String? = null,
    /** Stable seed selecting an AvatarTone in the UI - deterministic where missing. */
    val avatarSeed: Int = 0,
) {
    val displayName: String
        get() = if (lastName.isBlank()) firstName else "$firstName $lastName"

    companion object {
        /** Fallback identity used before auth lands (M2 replaces with real session). */
        val Me = User(
            id = "me",
            firstName = "You",
            username = "you",
            phone = "+91 00000 00000",
        )
    }
}
