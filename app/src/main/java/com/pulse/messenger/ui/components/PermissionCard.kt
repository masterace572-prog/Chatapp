package com.pulse.messenger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.pulse.messenger.ui.theme.PulseIconSizes
import com.pulse.messenger.ui.theme.PulseShapes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme

/**
 * One permission explainer card (PRD S16): icon, title, one-line reason and an
 * Allow action that triggers the real system permission dialog via [onAllow].
 * When already granted the card shows an "Allowed" tag instead.
 */
@Composable
fun PermissionCard(
    icon: ImageVector,
    title: String,
    reason: String,
    granted: Boolean,
    onAllow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = PulseTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(c.surface, PulseShapes.md)
            .padding(PulseSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(c.accentContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(PulseIconSizes.default),
                tint = c.accent,
            )
        }
        Spacer(Modifier.width(PulseSpacing.lg))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = c.textPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = reason,
                style = MaterialTheme.typography.bodyMedium,
                color = c.textSecondary,
            )
        }
        Spacer(Modifier.width(PulseSpacing.md))
        if (granted) {
            Tag(
                text = "Allowed",
                containerColor = c.success.copy(alpha = 0.14f),
                contentColor = c.success,
            )
        } else {
            Box(
                Modifier
                    .clip(PulseShapes.sm)
                    .background(c.accentContainer)
                    .clickable(onClick = onAllow)
                    .padding(horizontal = PulseSpacing.lg, vertical = PulseSpacing.sm),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Allow",
                    style = MaterialTheme.typography.labelLarge,
                    color = c.onAccentContainer,
                )
            }
        }
    }
}
