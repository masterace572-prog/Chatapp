package com.pulse.messenger.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pulse.messenger.ui.theme.PulseShapes
import com.pulse.messenger.ui.theme.PulseTheme

/**
 * Plain card container (PRD §7 - AppCard). Optional press interaction.
 * Surface colour by default; hairline border when `bordered`.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = PulseTheme.colors.surface,
    shape: Shape = PulseShapes.md,
    borderColor: Color? = PulseTheme.colors.border,
    borderVisible: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = shape,
        color = containerColor,
        border = if (borderVisible && borderColor != null) {
            BorderStroke(1.dp, borderColor)
        } else {
            null
        },
    ) {
        Box(Modifier.padding(contentPadding)) {
            ColumnScopeContent { content() }
        }
    }
}

@Composable
private fun ColumnScopeContent(content: @Composable ColumnScope.() -> Unit) {
    androidx.compose.foundation.layout.Column(Modifier.fillMaxWidth(), content = content)
}

/** Hairline divider (PRD §7 - AppDivider). Optional leading inset. */
@Composable
fun AppDivider(
    modifier: Modifier = Modifier,
    insetStart: Dp = 0.dp,
    color: Color = PulseTheme.colors.border,
    thickness: Dp = 1.dp,
) {
    HorizontalDivider(
        modifier = modifier
            .padding(start = insetStart)
            .fillMaxWidth(),
        thickness = thickness,
        color = color,
    )
}

/**
 * Quiet horizontal row used inside cards/lists when a labelled value is needed
 * (placeholder for later settings rows; not part of the M1 required set).
 */
@Composable
fun RowScope.LabeledValueRow(
    label: String,
    value: String,
    contentColor: Color = PulseTheme.colors.textSecondary,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(32.dp)) {
        androidx.compose.material3.Text(
            text = label,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = contentColor,
            modifier = Modifier.weight(1f),
        )
        androidx.compose.material3.Text(
            text = value,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = contentColor,
        )
    }
}
