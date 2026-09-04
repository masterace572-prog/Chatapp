package com.pulse.messenger.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pulse.messenger.R
import com.pulse.messenger.domain.model.Message
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.theme.PulseBubbleShape
import com.pulse.messenger.ui.theme.PulseIconSizes
import com.pulse.messenger.ui.theme.PulseShapes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme
import kotlinx.coroutines.delay
import kotlin.math.abs

/**
 * Voice-note bubble content (S23 voice note).
 *
 * Without a controller (previews) playback is simulated: pressing play
 * advances a timer through the duration and tints the played portion of the
 * waveform. In the conversation the row mirrors the app-wide
 * [com.pulse.messenger.media.VoicePlaybackController] state: [controllerDriven]
 * rows show the real clip duration once prepared, reset their head on fresh
 * play sessions, seek on waveform taps and cycle the real 1x/1.5x/2x speed.
 * The static bars are generated from the seeded waveform samples
 * ([MessageContent.Voice]) or, when a message carries none, from a
 * deterministic hash of the message id, so previews and seeds always render a
 * full waveform.
 */
@Composable
fun VoiceNoteContent(
    isOutgoing: Boolean,
    durationSeconds: Int,
    waveformSamples: List<Int>,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier,
    seedKey: String = "voice",
    // M4c real-playback mirror (conversation rows only; previews stay simulated).
    controllerDriven: Boolean = false,
    playSession: Int = 0,
    voiceDurationMs: Int = 0,
    voiceSpeedIndex: Int = 0,
    onSeekFraction: ((Float) -> Unit)? = null,
    onSpeedCycle: (() -> Unit)? = null,
) {
    val c = PulseTheme.colors
    // Total length shown by the visuals: the real clip length once the player
    // reports it, otherwise the seeded metadata (previews / clip preparing).
    val totalMs = if (controllerDriven && voiceDurationMs > 0) voiceDurationMs.toFloat()
    else durationSeconds * 1000f
    // Playback head (ms), owned by the bubble so progress survives brief
    // recompositions. remember(playSession): a fresh controller session (new
    // play after stop/completion) resets the head exactly when audio restarts.
    var progressMs by remember(playSession) { mutableFloatStateOf(0f) }
    // Fallback speed index for simulated hosts (chip cycles locally).
    var fallbackSpeed by remember { mutableIntStateOf(0) }
    val speeds = listOf(1f, 1.5f, 2f)
    val speedIndex = if (controllerDriven) voiceSpeedIndex else fallbackSpeed
    val samples = remember(waveformSamples, seedKey) {
        if (waveformSamples.isNotEmpty()) waveformSamples else deterministicWave(seedKey)
    }
    val maxSample = remember(samples) { samples.maxOrNull()?.coerceAtLeast(1) ?: 1 }

    LaunchedEffect(isPlaying, playSession) {
        if (isPlaying) {
            if (progressMs >= totalMs) progressMs = 0f
            val stepMs = 50L
            val rateMs = (stepMs / speeds[speedIndex]).toLong().coerceAtLeast(16L)
            while (isPlaying && progressMs < totalMs) {
                delay(rateMs)
                progressMs = (progressMs + stepMs).coerceAtMost(totalMs)
            }
            // Simulated hosts auto-stop at the visual end; real playback is
            // stopped by the controller's completion instead.
            if (!controllerDriven && progressMs >= totalMs) onPlayPause()
        }
    }

    val contentColor = if (isOutgoing) c.onAccent else c.textPrimary
    val metaColor = if (isOutgoing) c.onAccent.copy(alpha = 0.8f) else c.textSecondary
    val remaining = ((totalMs - progressMs) / 1000f).toInt().coerceAtLeast(0)

    // Waveform tap -> audio seek + jump the local head (controller rows only).
    val seekTo: ((Float) -> Unit)? = onSeekFraction?.let { external ->
        { fraction ->
            external(fraction)
            progressMs = totalMs * fraction.coerceIn(0f, 1f)
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Play / pause circle.
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isOutgoing) c.onAccent.copy(alpha = 0.18f) else c.accentContainer)
                .clickable(onClick = onPlayPause),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isPlaying) AppIcons.Pause else AppIcons.Play,
                contentDescription = stringResource(
                    if (isPlaying) R.string.conversation_voice_pause_cd
                    else R.string.conversation_voice_play_cd,
                ),
                modifier = Modifier.size(PulseIconSizes.inline),
                tint = if (isOutgoing) c.onAccent else c.accent,
            )
        }
        Spacer(Modifier.width(PulseSpacing.md))

        VoiceWaveform(
            samples = samples,
            maxSample = maxSample,
            playedFraction = if (totalMs > 0f) (progressMs / totalMs).coerceIn(0f, 1f) else 0f,
            baseColor = if (isOutgoing) c.onAccent.copy(alpha = 0.55f) else c.textTertiary,
            playedColor = if (isOutgoing) c.onAccent else c.accent,
            onSeekFraction = seekTo,
            modifier = Modifier
                .weight(1f)
                .height(34.dp),
        )

        Spacer(Modifier.width(PulseSpacing.md))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = durationText(remaining),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFeatureSettings = "tnum",
                    color = metaColor,
                ),
            )
            Spacer(Modifier.height(PulseSpacing.tight))
            SpeedChip(
                label = speedLabel(speedIndex),
                onClick = {
                    if (controllerDriven) {
                        onSpeedCycle?.invoke()
                    } else {
                        fallbackSpeed = (fallbackSpeed + 1) % speeds.size
                    }
                },
                contentColor = if (isOutgoing) c.onAccent.copy(alpha = 0.9f) else c.textSecondary,
                borderColor = if (isOutgoing) c.onAccent.copy(alpha = 0.45f) else c.border,
            )
        }
    }
}

/** Playback-speed chip cycling 1× / 1.5× / 2×. */
@Composable
private fun SpeedChip(
    label: String,
    onClick: () -> Unit,
    contentColor: Color,
    borderColor: Color,
) {
    val c = PulseTheme.colors
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
            color = contentColor,
            fontWeight = FontWeight.Medium,
        ),
        modifier = Modifier
            .clip(PulseShapes.full)
            .background(c.accent.copy(alpha = 0.08f))
            .clickable(onClick = onClick)
            .padding(horizontal = PulseSpacing.sm, vertical = 1.dp),
    )
}

/**
 * Static waveform bars (S23 voice note). Bars before [playedFraction] are
 * tinted with [playedColor] (the played portion), the rest with [baseColor].
 */
@Composable
fun VoiceWaveform(
    samples: List<Int>,
    playedFraction: Float,
    modifier: Modifier = Modifier,
    maxSample: Int = 100,
    baseColor: Color = PulseTheme.colors.textTertiary,
    playedColor: Color = PulseTheme.colors.accent,
    /** M4c: tap the wave to seek; fraction 0..1 across the whole wave. */
    onSeekFraction: ((Float) -> Unit)? = null,
) {
    val tapModifier = if (onSeekFraction != null) {
        modifier.pointerInput(onSeekFraction) {
            detectTapGestures { offset ->
                onSeekFraction((offset.x / size.width).coerceIn(0f, 1f))
            }
        }
    } else {
        modifier
    }
    Canvas(modifier = tapModifier) {
        if (samples.isEmpty()) return@Canvas
        val n = samples.size
        val gapPx = 2.dp.toPx()
        val barWidthPx = (size.width - gapPx * (n - 1)) / n
        val playedX = size.width * playedFraction.coerceIn(0f, 1f)
        for (i in samples.indices) {
            val h = (samples[i].toFloat() / maxSample) * size.height * 0.9f
            val x = i * (barWidthPx + gapPx)
            val barColor = if (x + barWidthPx / 2f <= playedX) playedColor else baseColor
            drawRoundRect(
                color = barColor,
                topLeft = androidx.compose.ui.geometry.Offset(x, (size.height - h) / 2f),
                size = androidx.compose.ui.geometry.Size(barWidthPx, h),
                cornerRadius = CornerRadius(barWidthPx / 2f),
            )
        }
    }
}

/** Deterministic 40-bar waveform for messages without seeded samples. */
internal fun deterministicWave(key: String): List<Int> {
    var h = key.hashCode()
    return List(40) { i ->
        h = h * 31 + i * 7 + 11
        val v = abs(h) % 100
        // Lift small values so the wave reads as speech-like activity.
        14 + (v * 82) / 100
    }
}

internal fun durationText(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%d:%02d".format(m, s)
}

internal fun speedLabel(index: Int): String = when (index) {
    0 -> "1×"
    1 -> "1.5×"
    else -> "2×"
}

/**
 * Standalone voice-message bubble used by previews and mirrors the chrome the
 * conversation list draws around [VoiceNoteContent] (bubble colours + meta).
 */
@Composable
fun VoiceMessageBubble(
    message: Message,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = PulseTheme.colors
    val voice = message.content as? com.pulse.messenger.domain.model.MessageContent.Voice
    Column(
        modifier = modifier
            .background(
                color = if (message.isOutgoing) c.accent else c.surfaceVariant,
                shape = PulseBubbleShape(message.isOutgoing, true, true),
            )
            .padding(horizontal = PulseSpacing.md, vertical = PulseSpacing.sm),
    ) {
        if (voice != null) {
            VoiceNoteContent(
                isOutgoing = message.isOutgoing,
                durationSeconds = voice.durationSeconds,
                waveformSamples = voice.waveformSamples,
                isPlaying = isPlaying,
                onPlayPause = onPlayPause,
                seedKey = message.id,
            )
        }
    }
}
