package com.pulse.messenger.ui.navigation

/**
 * Central route table - the single source of truth for every destination.
 *
 * Status legend:
 *   [registered]  = wired into the NavHost today (M1)
 *   [planned]     = reserved now so graphs can be appended without renaming;
 *                   the owning milestone registers the composable(s)
 *
 * Screen mapping (PRD §6):
 *  - Onboarding & Auth (S02-S17)  -> ONBOARDING graph          (M2)
 *  - Main shell (S18)             -> MAIN (bottom tabs)        (M1)
 *  - Chats list, folders (S19-S22)-> tabs inside MAIN + CHAT_LIST graph (M3)
 *  - Conversation (S23-S40)       -> CHAT graph (chatId arg)   (M4)
 *  - Calls (S41-S46)              -> tab + CALL_* destinations (M5)
 *  - People (S47-S52)             -> tab + PEOPLE graph        (M5)
 *  - Settings (S53-S68)           -> tab + SETTINGS graph      (M6)
 *  - Misc (S69-S74)               -> MISCELLANEOUS graph       (M7)
 */
object PulseRoutes {

    /* ---------- Registered (M1) ---------- */

    const val SPLASH = "splash"
    const val MAIN = "main"

    /* ---------- Planned (register with their milestone) ---------- */

    /** M2 - Welcome screen and the whole auth journey (S02-S17). */
    const val ONBOARDING = "onboarding"

    /** M4 - Conversation root with the chat id argument. */
    const val CHAT = "chat/{chatId}"
    fun chatRoute(chatId: String) = "chat/$chatId"
}

/** Shared navigation argument names. */
object PulseNavArgs {
    const val ChatId = "chatId"
}
