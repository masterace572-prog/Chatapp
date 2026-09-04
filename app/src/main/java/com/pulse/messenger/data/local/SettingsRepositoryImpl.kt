package com.pulse.messenger.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pulse.messenger.domain.model.ThemeMode
import com.pulse.messenger.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.pulseDataStore by preferencesDataStore(name = "pulse_settings")

/**
 * DataStore-backed settings (PRD stack: "DataStore (settings)").
 * Theme mode + accent survive restarts; more preferences register here with
 * their own milestone.
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : SettingsRepository {

    private object Keys {
        val ThemeMode = stringPreferencesKey("theme_mode")
        val AccentPresetId = stringPreferencesKey("accent_preset_id")
    }

    override fun observeThemeMode(): Flow<ThemeMode> =
        context.pulseDataStore.data.map { prefs ->
            runCatching {
                ThemeMode.valueOf(prefs[Keys.ThemeMode] ?: ThemeMode.System.name)
            }.getOrDefault(ThemeMode.System)
        }

    override suspend fun setThemeMode(mode: ThemeMode) {
        context.pulseDataStore.edit { it[Keys.ThemeMode] = mode.name }
    }

    override fun observeAccentPresetId(): Flow<String> =
        context.pulseDataStore.data.map { prefs ->
            prefs[Keys.AccentPresetId] ?: SettingsRepository.DefaultAccentPresetId
        }

    override suspend fun setAccentPresetId(id: String) {
        context.pulseDataStore.edit { it[Keys.AccentPresetId] = id }
    }
}
