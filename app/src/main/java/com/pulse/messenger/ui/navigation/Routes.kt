package com.pulse.messenger.ui.navigation

/**
 * Central route table. One constant per destination graph; the onboarding/auth
 * graph (S02-S17), conversation graph (S23+) and friends attach in their
 * milestones without touching existing routes.
 */
object PulseRoutes {
    const val SPLASH = "splash"
    const val MAIN = "main"
}

object PulseNavArgs {
    const val ChatId = "chatId"
}
