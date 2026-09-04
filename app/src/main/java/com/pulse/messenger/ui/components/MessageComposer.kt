package com.pulse.messenger.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.pulse.messenger.R
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.theme.PulseShapes
import com.pulse.messenger.ui.theme.PulseSizes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme

/**
 * Basic conversation composer (S23; full states - reply/edit bars, recording,
 * mention suggestions - land in M4b). Attachment opens nothing yet; camera
 * and mic are styled no-op placeholders for M4b/M4c. The mic/send swap is
 * cross-faded when text appears; Enter inserts a newline (PRD: "Enter is
 * send" is an M6 setting).
 */
@Composable
fun MessageComposer(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSend: () -> Unit = {},
    onAttachment: () -> Unit = {},
    onCamera: () -> Unit = {},
    onMic: () -> Unit = {},
    enabled: Boolean = true,
) {
    val c = PulseTheme.colors
    val hasText = value.isNotBlank()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(c.background)
            .padding(start = PulseSpacing.xs, end = PulseSpacing.xs, bottom = PulseSpacing.xs),
        verticalAlignment = Alignment.Bottom,
    ) {
        AppIconButton(
            icon = AppIcons.Paperclip,
            contentDescription = stringResource(R.string.conversation_composer_attachment_cd),
            onClick = onAttachment,
            tint = c.textSecondary,
            enabled = enabled,
        )

        Spacer(Modifier.width(PulseSpacing.xs))

        // Growing pill field (1-6 lines).
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(PulseShapes.full)
                .background(if (enabled) c.surfaceVariant else c.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = PulseSpacing.lg, vertical = PulseSpacing.sm),
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(
                        min = PulseSizes.inputHeight - PulseSpacing.sm * 2,
                        max = PulseSpacing.sm * 2 + 20.dp * 6,
                    ),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = c.textPrimary),
                cursorBrush = SolidColor(c.accent),
                enabled = enabled,
                keyboardOptions = KeyboardOptions.Default,
                maxLines = 6,
                decorationBox = { inner ->
                    Box {
                        if (value.isEmpty()) {
                            Text(
                                text = stringResource(R.string.conversation_composer_hint),
                                style = MaterialTheme.typography.bodyMedium,
                                color = c.textTertiary,
                            )
                        }
                        inner()
                    }
                },
            )
        }

        Spacer(Modifier.width(PulseSpacing.xs))

        // Camera appears inside the trailing cluster while the field is empty;
        // mic swaps to the accent send circle once there is text (animated).
        Crossfade(
            targetState = hasText,
            animationSpec = androidx.compose.animation.core.tween(180),
            label = "composer-swap",
        ) { typing ->
            if (typing) {
                SendButton(onSend = onSend, enabled = enabled)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppIconButton(
                        icon = AppIcons.Camera,
                        contentDescription = stringResource(R.string.conversation_composer_camera_cd),
                        onClick = onCamera,
                        tint = c.textSecondary,
                        enabled = enabled,
                    )
                    AppIconButton(
                        icon = AppIcons.Mic,
                        contentDescription = stringResource(R.string.conversation_composer_mic_cd),
                        onClick = onMic,
                        tint = c.textSecondary,
                        enabled = enabled,
                    )
                }
            }
        }
    }
}

/** Accent circle send button (52dp target, 40dp visual circle). */
@Composable
private fun SendButton(onSend: () -> Unit, enabled: Boolean) {
    val c = PulseTheme.colors
    Box(
        modifier = Modifier
            .size(PulseSizes.minTouchTarget)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onSend),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(com.pulse.messenger.ui.theme.PulseSizes.sendCircle)
                .clip(CircleShape)
                .background(if (enabled) c.accent else c.accent.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = AppIcons.Send,
                contentDescription = stringResource(R.string.conversation_composer_send_cd),
                modifier = Modifier.size(com.pulse.messenger.ui.theme.PulseIconSizes.inline),
                tint = c.onAccent,
            )
        }
    }
}
