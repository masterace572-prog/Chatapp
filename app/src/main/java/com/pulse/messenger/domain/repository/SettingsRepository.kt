package com.pulse.messenger.domain.repository

import com.pulse.messenger.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/**
 * User preferences contract (DataStore-backed from day one; PRD §5 + §9).
 * Theme + accent power the UI theme in [com.pulse.messenger.ui.theme.PulseTheme];
 * further prefs (notifications, wallpaper...) register here as milestones land.
 */
interface SettingsRepository {

    /** Id of the accent preset shown in the UI (see AccentPresets in ui/theme). */
    fun observeAccentPresetId(): Flow<String>
    suspend fun setAccentPresetId(id: String)

    fun observeThemeMode(): Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)

    companion object {
        const val DefaultAccentPresetId = "indigo"
    }
}
