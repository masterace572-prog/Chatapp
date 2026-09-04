package com.pulse.messenger.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pulse.messenger.ui.theme.PulseShapes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme
import kotlin.math.sin
import kotlin.math.PI

/**
 * Shared conversation list chrome (PRD §7 + S23). All rows are stateless;
 * the conversation screen places them between message bubbles.
 */

/** Centered date pill: "Today" / "Yesterday" / weekday / full date (S23). */
@Composable
fun DateSeparator(
    label: String,
    modifier: Modifier = Modifier,
) {
    val c = PulseTheme.colors
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = c.textSecondary,
            modifier = Modifier
                .clip(PulseShapes.full)
                .background(c.surfaceVariant)
                .padding(horizontal = PulseSpacing.md, vertical = PulseSpacing.xs),
        )
    }
}

/** Centered "N unread messages" pill shown once when a chat opens (S23). */
@Composable
fun UnreadDivider(
    label: String,
    modifier: Modifier = Modifier,
) {
    val c = PulseTheme.colors
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = c.onAccentContainer,
            modifier = Modifier
                .clip(PulseShapes.full)
                .background(c.accentContainer)
                .padding(horizontal = PulseSpacing.md, vertical = PulseSpacing.xs),
        )
    }
}

/** Centered system row, e.g. "You created the group", "Aria joined" (S23). */
@Composable
fun SystemMessageRow(
    text: String,
    modifier: Modifier = Modifier,
) {
    val c = PulseTheme.colors
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = c.textTertiary,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PulseSpacing.xxxl, vertical = PulseSpacing.xs),
    )
}

/**
 * Incoming typing bubble (S23): three dots with a subtle, offset opacity
 * pulse. Optional sender line for group chats; the bubble itself is static.
 */
@Composable
fun TypingIndicator(
    modifier: Modifier = Modifier,
    senderName: String? = null,
    senderNameColor: Color = PulseTheme.colors.textSecondary,
) {
    val c = PulseTheme.colors
    Column(modifier = modifier) {
        if (senderName != null) {
            Text(
                text = senderName,
                style = MaterialTheme.typography.labelMedium,
                color = senderNameColor,
                modifier = Modifier.padding(start = PulseSpacing.xs, bottom = PulseSpacing.tight),
            )
        }
        Row(
            modifier = Modifier
                .clip(PulseShapes.full)
                .background(c.surfaceVariant)
                .padding(horizontal = PulseSpacing.lg, vertical = PulseSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TypingDot(phase = 0f)
            Spacer(Modifier.width(PulseSpacing.xs))
            TypingDot(phase = 0.33f)
            Spacer(Modifier.width(PulseSpacing.xs))
            TypingDot(phase = 0.66f)
        }
    }
}

@Composable
private fun TypingDot(phase: Float) {
    val c = PulseTheme.colors
    val transition = rememberInfiniteTransition(label = "typing")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1_100, easing = LinearEasing), RepeatMode.Restart),
        label = "typing-progress",
    )
    // Phase-shifted sine keeps the three dots breathing in sequence.
    val radians = (progress + phase) * 2.0 * PI
    val alpha = ((sin(radians) + 1.0) / 2.0)
        .coerceIn(0.25, 1.0)
        .toFloat()
    Box(
        Modifier
            .size(8.dp)
            .alpha(alpha)
            .clip(PulseShapes.full)
            .background(c.textSecondary),
    )
}
