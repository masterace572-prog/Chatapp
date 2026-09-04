package com.pulse.messenger.ui.navigation

/**
 * Central route table - the single source of truth for every destination.
 *
 * Screen mapping (PRD §6):
 *  - S01 Splash (registered, session-aware since M2)
 *  - Onboarding/Auth S02-S17 (registered since M2)
 *  - Main shell S18 (M1); Chats S19-S22 (M3)
 *  - Conversation S23-S40 (M4); Calls S41-S46 + People S47-S52 (M5)
 *  - Settings S53-S68 (M6); Misc S69-S74 (M7)
 */
object PulseRoutes {

    /* ---------- Registered ---------- */

    const val SPLASH = "splash"
    const val MAIN = "main"

    /** S02 */
    const val WELCOME = "welcome"

    /** S03 -> S04 */
    const val LOGIN_EMAIL = "login/email"
    const val LOGIN_PASSWORD = "login/password"

    /** S03b -> S06 */
    const val LOGIN_PHONE = "login/phone"

    /** S06 (phone login) + S11b (sign-up email verification) share this screen. */
    const val OTP = "otp"

    /** S07 -> S08 -> S09 -> S10(reset) */
    const val FORGOT_EMAIL = "forgot/email"
    const val FORGOT_INBOX = "forgot/inbox"
    const val RESET_PASSWORD = "forgot/reset"
    const val SUCCESS_RESET = "success/reset"

    /** S11a -> S11b -> S11c -> S12..S17 -> S10(account) */
    const val SIGNUP_EMAIL = "signup/email"
    const val SIGNUP_PASSWORD = "signup/password"
    const val SUCCESS_ACCOUNT = "success/account"

    /** Profile setup S12-S15 + S16 + S17 (shared by sign-up and Google flows). */
    const val PROFILE_NAME = "profile/name"
    const val PROFILE_USERNAME = "profile/username"
    const val PROFILE_PHOTO = "profile/photo"
    const val PROFILE_BIO = "profile/bio"
    const val PERMISSIONS = "permissions"
    const val FIND_FRIENDS = "find-friends"

    /* ---------- Planned (register with their milestone) ---------- */

    /** M4 - Conversation root with the chat id argument. */
    const val CHAT = "chat/{chatId}"
    fun chatRoute(chatId: String) = "chat/$chatId"
}
