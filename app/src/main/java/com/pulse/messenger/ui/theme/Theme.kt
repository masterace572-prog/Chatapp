package com.pulse.messenger.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * Pulse theme entry point.
 *
 * Builds the Material 3 scheme AND the extended Pulse palette from one set of
 * tokens, mapping every screen-facing color back to the PRD design system
 * (no default Material purple anywhere).
 */
@Composable
fun PulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentPreset: AccentPreset = AccentPresets.Indigo,
    content: @Composable () -> Unit,
) {
    val pulseColors = if (darkTheme) darkPulseColors(accentPreset) else lightPulseColors(accentPreset)
    val colorScheme = if (darkTheme) {
        pulseDarkScheme(pulseColors)
    } else {
        pulseLightScheme(pulseColors)
    }
    CompositionLocalProvider(LocalPulseColors provides pulseColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = PulseTypography,
            content = content,
        )
    }
}

/** Extended palette accessor used across the app. */
object PulseTheme {
    val colors: PulseColors
        @Composable
        @ReadOnlyComposable
        get() = LocalPulseColors.current
}

internal val LocalPulseColors = staticCompositionLocalOf<PulseColors> {
    error("PulseColors not provided - wrap the tree in PulseTheme")
}

/* ---------------- Material scheme mapping ---------------- */

private fun pulseLightScheme(p: PulseColors) = lightColorScheme(
    primary = p.accent,
    onPrimary = p.onAccent,
    primaryContainer = p.accentContainer,
    onPrimaryContainer = p.onAccentContainer,
    secondary = p.accent,
    onSecondary = p.onAccent,
    secondaryContainer = p.accentContainerMuted,
    onSecondaryContainer = p.onAccentContainer,
    tertiary = p.info,
    onTertiary = Color.White,
    background = p.background,
    onBackground = p.textPrimary,
    surface = p.surface,
    onSurface = p.textPrimary,
    surfaceVariant = p.surfaceVariant,
    onSurfaceVariant = p.textSecondary,
    outline = p.border,
    outlineVariant = p.border,
    error = p.error,
    onError = p.onError,
    errorContainer = lerp(p.error, Color.White, 0.88f),
    onErrorContainer = p.error,
    surfaceContainerLowest = p.surface,
    surfaceContainerLow = lerp(p.background, Color.White, 0.5f),
    surfaceContainer = p.surfaceVariant,
    surfaceContainerHigh = p.border,
    surfaceContainerHighest = lerp(p.border, Color.Black, 0.05f),
    inverseSurface = Color(0xFF2A2A33),
    inverseOnSurface = Color(0xFFF2F2F5),
    inversePrimary = p.accent,
    scrim = Color(0x66000000),
)

private fun pulseDarkScheme(p: PulseColors) = darkColorScheme(
    primary = p.accent,
    onPrimary = p.onAccent,
    primaryContainer = p.accentContainer,
    onPrimaryContainer = p.onAccentContainer,
    secondary = p.accent,
    onSecondary = p.onAccent,
    secondaryContainer = p.accentContainerMuted,
    onSecondaryContainer = p.onAccentContainer,
    tertiary = p.info,
    onTertiary = Color(0xFF0F0F12),
    background = p.background,
    onBackground = p.textPrimary,
    surface = p.surface,
    onSurface = p.textPrimary,
    surfaceVariant = p.surfaceVariant,
    onSurfaceVariant = p.textSecondary,
    outline = p.border,
    outlineVariant = p.border,
    error = p.error,
    onError = p.onError,
    errorContainer = lerp(p.error, Color.Black, 0.72f),
    onErrorContainer = p.error,
    surfaceContainerLowest = Color(0xFF121216),
    surfaceContainerLow = p.surface,
    surfaceContainer = p.surfaceVariant,
    surfaceContainerHigh = p.border,
    surfaceContainerHighest = lerp(p.border, Color.White, 0.06f),
    inverseSurface = Color(0xFFF2F2F5),
    inverseOnSurface = Color(0xFF111114),
    inversePrimary = p.accent,
    scrim = Color(0x99000000),
)
