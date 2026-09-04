package com.pulse.messenger.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/*
 * Pulse color tokens (PRD §4.1).
 *
 * This file is the ONLY place where raw color values are declared.
 * Screens and components must consume colors through PulseTheme.colors
 * (MaterialTheme.colorScheme + the Pulse extended palette) - never raw hex.
 */

/* ---------- Base palette (light theme) ---------- */
internal val LightBackground = Color(0xFFF7F7F8)
internal val LightSurface = Color(0xFFFFFFFF)
internal val LightSurfaceVariant = Color(0xFFF0F0F3)
internal val LightBorder = Color(0xFFE6E6EA)
internal val LightTextPrimary = Color(0xFF111114)
internal val LightTextSecondary = Color(0xFF6B6B75)
internal val LightTextTertiary = Color(0xFF9A9AA3)
internal val LightAccent = Color(0xFF3B5BDB)
internal val LightOnAccent = Color(0xFFFFFFFF)
internal val LightSuccess = Color(0xFF2E8B57)
internal val LightWarning = Color(0xFFC27C0E)
internal val LightError = Color(0xFFC43D3D)
internal val LightInfo = Color(0xFF2F6FB0)

/* ---------- Base palette (dark theme) ---------- */
internal val DarkBackground = Color(0xFF0F0F12)
internal val DarkSurface = Color(0xFF1A1A1F)
internal val DarkSurfaceVariant = Color(0xFF24242B)
internal val DarkBorder = Color(0xFF2A2A32)
internal val DarkTextPrimary = Color(0xFFF2F2F5)
internal val DarkTextSecondary = Color(0xFFA0A0AA)
internal val DarkTextTertiary = Color(0xFF6E6E78)
internal val DarkAccent = Color(0xFF5C7CFA)
internal val DarkOnAccent = Color(0xFFFFFFFF)
internal val DarkSuccess = Color(0xFF3FA372)
internal val DarkWarning = Color(0xFFD4922E)
internal val DarkError = Color(0xFFE05A5A)
internal val DarkInfo = Color(0xFF4A8AD0)

/**
 * A muted accent choice (PRD §4.1 / S60 swatches). The app defaults to Indigo.
 * Each preset provides light and dark variants of the same hue family.
 */
data class AccentPreset(val id: String, val light: Color, val dark: Color)

object AccentPresets {
    val Indigo = AccentPreset("indigo", Color(0xFF3B5BDB), Color(0xFF5C7CFA))
    val Ocean = AccentPreset("ocean", Color(0xFF23779B), Color(0xFF3E94BE))
    val Teal = AccentPreset("teal", Color(0xFF2E8574), Color(0xFF4FA695))
    val Clay = AccentPreset("clay", Color(0xFFB0652F), Color(0xFFD08A52))
    val Plum = AccentPreset("plum", Color(0xFF7D4BA0), Color(0xFF9F6FC4))
    val Steel = AccentPreset("steel", Color(0xFF4A6A8A), Color(0xFF6D8FAD))

    val All: List<AccentPreset> = listOf(Indigo, Ocean, Teal, Clay, Plum, Steel)

    fun byId(id: String): AccentPreset = All.firstOrNull { it.id == id } ?: Indigo
}

/**
 * Extended Pulse palette - theme + accent aware.
 * Exposed through PulseTheme.colors; never construct manually in screens.
 */
data class PulseColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val accentContainer: Color,
    val onAccent: Color,
    val onAccentContainer: Color,
    val success: Color,
    val onSuccess: Color,
    val warning: Color,
    val error: Color,
    val onError: Color,
    val info: Color,
    val accentContainerMuted: Color,
    /** True when the palette is the dark theme (drives derived tints). */
    val isDark: Boolean = false,
)

private fun buildPulseColors(
    background: Color,
    surface: Color,
    surfaceVariant: Color,
    border: Color,
    textPrimary: Color,
    textSecondary: Color,
    textTertiary: Color,
    accent: Color,
    onAccent: Color,
    success: Color,
    warning: Color,
    error: Color,
    info: Color,
    containerAlpha: Float,
    isDark: Boolean,
): PulseColors {
    val accentContainer = lerp(accent, surface, 1f - containerAlpha)
    val onAccentContainer = lerp(accent, surface, 0.25f)
    val onError = Color.White
    val onSuccess = Color.White
    // Muted tinted container used for icon tiles and quiet highlights.
    val accentContainerMuted = lerp(accent, background, 1f - containerAlpha * 0.55f)
    return PulseColors(
        background = background,
        surface = surface,
        surfaceVariant = surfaceVariant,
        border = border,
        textPrimary = textPrimary,
        textSecondary = textSecondary,
        textTertiary = textTertiary,
        accent = accent,
        accentContainer = accentContainer,
        onAccent = onAccent,
        onAccentContainer = onAccentContainer,
        success = success,
        onSuccess = onSuccess,
        warning = warning,
        error = error,
        onError = onError,
        info = info,
        accentContainerMuted = accentContainerMuted,
        isDark = isDark,
    )
}

/**
 * Readable text colour derived from an avatar tone for group sender names on
 * bubbles: the tone is blended toward the theme's primary ink so it keeps the
 * muted hue family but stays legible on surfaceVariant in both themes.
 */
internal fun senderToneTextColor(tone: AvatarTone, isDark: Boolean): Color =
    lerp(tone.background, if (isDark) DarkTextPrimary else LightTextPrimary, if (isDark) 0.42f else 0.52f)

internal fun lightPulseColors(accent: AccentPreset): PulseColors = buildPulseColors(
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    border = LightBorder,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textTertiary = LightTextTertiary,
    accent = accent.light,
    onAccent = LightOnAccent,
    success = LightSuccess,
    warning = LightWarning,
    error = LightError,
    info = LightInfo,
    containerAlpha = 0.10f, // accentContainer = accent @ 10%
    isDark = false,
)

internal fun darkPulseColors(accent: AccentPreset): PulseColors = buildPulseColors(
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    border = DarkBorder,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textTertiary = DarkTextTertiary,
    accent = accent.dark,
    onAccent = DarkOnAccent,
    success = DarkSuccess,
    warning = DarkWarning,
    error = DarkError,
    info = DarkInfo,
    containerAlpha = 0.14f, // accentContainer = accent @ 14%
    isDark = true,
)

/** Muted initials-avatar tones (PRD §4.1): 8 desaturated pairs, each with a matching foreground. */
data class AvatarTone(val background: Color, val foreground: Color)

object AvatarTones {
    val Slate = AvatarTone(Color(0xFF5E6E83), Color(0xFFEFF3F8))
    val Sage = AvatarTone(Color(0xFF78896B), Color(0xFFF0F5EC))
    val Clay = AvatarTone(Color(0xFFA06A52), Color(0xFFFBF1EA))
    val Sand = AvatarTone(Color(0xFF9A8A58), Color(0xFFF7F2E6))
    val Dusk = AvatarTone(Color(0xFF6E6C9E), Color(0xFFF1F0FA))
    val Moss = AvatarTone(Color(0xFF5F7A52), Color(0xFFEDF4EA))
    val Plum = AvatarTone(Color(0xFF8A5A80), Color(0xFFF8EFF7))
    val Steel = AvatarTone(Color(0xFF4E7186), Color(0xFFEDF4F9))

    val All: List<AvatarTone> = listOf(Slate, Sage, Clay, Sand, Dusk, Moss, Plum, Steel)

    fun bySeed(seed: Int): AvatarTone = All[((seed % All.size) + All.size) % All.size]
}
