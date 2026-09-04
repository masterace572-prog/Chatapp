package com.pulse.messenger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulse.messenger.domain.model.ThemeMode
import com.pulse.messenger.domain.repository.SettingsRepository
import com.pulse.messenger.ui.navigation.PulseApp
import com.pulse.messenger.ui.theme.AccentPresets
import com.pulse.messenger.ui.theme.PulseTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        // Android 12 SplashScreen API (S01); the OS splash holds while Compose boots.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by settingsRepository.observeThemeMode()
                .collectAsStateWithLifecycle(initialValue = ThemeMode.System)
            val accentId by settingsRepository.observeAccentPresetId()
                .collectAsStateWithLifecycle(
                    initialValue = SettingsRepository.DefaultAccentPresetId,
                )

            val darkTheme = when (themeMode) {
                ThemeMode.System -> isSystemInDarkTheme()
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
            }

            PulseTheme(
                darkTheme = darkTheme,
                accentPreset = AccentPresets.byId(accentId),
            ) {
                PulseApp()
            }
        }
    }
}
