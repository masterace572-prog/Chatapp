package com.pulse.messenger.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.pulse.messenger.ui.theme.PulseShapes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme

/**
 * Segmented control (PRD §7 - SegmentedControl): e.g. theme System/Light/Dark.
 * surfaceVariant track, surface + border selected thumb.
 */
@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = PulseShapes.md,
) {
    val c = PulseTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color = c.surfaceVariant, shape = shape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEachIndexed { index, option ->
            val selected = index == selectedIndex
            val bg by animateColorAsState(
                targetValue = if (selected) c.surface else Color.Transparent,
                animationSpec = tween(180),
                label = "segmentBg",
            )
            val fg = if (selected) c.textPrimary else c.textSecondary
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .background(color = bg, shape = shape)
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = option,
                    style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                    color = fg,
                    maxLines = 1,
                )
            }
        }
    }
}

/**
 * Themed switch (PRD §7 - AppSwitch): accent when on, muted otherwise.
 */
@Composable
fun AppSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val c = PulseTheme.colors
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = c.onAccent,
            checkedTrackColor = c.accent,
            checkedBorderColor = Color.Transparent,
            uncheckedThumbColor = c.surface,
            uncheckedTrackColor = c.surfaceVariant,
            uncheckedBorderColor = c.border,
            disabledCheckedThumbColor = c.onAccent.copy(alpha = 0.6f),
            disabledCheckedTrackColor = c.accent.copy(alpha = 0.4f),
            disabledUncheckedThumbColor = c.surface.copy(alpha = 0.8f),
            disabledUncheckedTrackColor = c.surfaceVariant.copy(alpha = 0.6f),
            disabledUncheckedBorderColor = c.border,
        ),
    )
}
