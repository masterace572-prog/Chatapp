package com.pulse.messenger.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulse.messenger.ui.theme.PulseShapes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme

/**
 * Filter / folder chip (PRD §7 - AppChip).
 * Selected: accentContainer fill + accent label. Unselected: hairline border.
 */
@Composable
fun AppChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    shape: Shape = PulseShapes.full,
) {
    val c = PulseTheme.colors
    val contentColor = if (selected) c.onAccentContainer else c.textSecondary
    val backgroundColor = if (selected) c.accentContainer else Color.Transparent
    val interaction = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .clickable(
                interactionSource = interaction,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = onClick,
            )
            .padding(horizontal = PulseSpacing.lg, vertical = PulseSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = contentColor,
            )
            Spacer(Modifier.width(PulseSpacing.xs))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            ),
            color = contentColor,
            maxLines = 1,
        )
    }
}

/** Small count pill (PRD §7 - Badge): unread counts, muted-variant support. */
@Composable
fun Badge(
    count: Int,
    modifier: Modifier = Modifier,
    muted: Boolean = false,
    maxCount: Int = 99,
) {
    val c = PulseTheme.colors
    val text = if (count > maxCount) "$maxCount+" else count.toString()
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(if (muted) c.surfaceVariant else c.accent)
            .padding(horizontal = PulseSpacing.sm, vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
            ),
            color = if (muted) c.textSecondary else c.onAccent,
            maxLines = 1,
        )
    }
}

/** Neutral micro-label, e.g. "Admin", "You" (PRD §7 - Tag; xs radius). */
@Composable
fun Tag(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = PulseTheme.colors.surfaceVariant,
    contentColor: Color = PulseTheme.colors.textSecondary,
    shape: Shape = PulseShapes.xs,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(containerColor)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            maxLines = 1,
        )
    }
}
