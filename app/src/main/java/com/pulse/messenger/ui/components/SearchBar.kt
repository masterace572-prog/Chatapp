package com.pulse.messenger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.theme.PulseIconSizes
import com.pulse.messenger.ui.theme.PulseShapes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme

/**
 * Search bar with collapsed and expanded appearances (PRD §7 - SearchBar).
 *
 * Collapsed: quiet pill on surfaceVariant that activates search (S19).
 * Expanded: back + full-width input with clear (S20 in a later milestone).
 */
@Composable
fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
) {
    val c = PulseTheme.colors

    if (!active) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(PulseShapes.full)
                .background(c.surfaceVariant)
                .clickable { onActiveChange(true) }
                .padding(horizontal = PulseSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = AppIcons.Search,
                contentDescription = null,
                modifier = Modifier.size(PulseIconSizes.inline),
                tint = c.textSecondary,
            )
            Spacer(Modifier.width(PulseSpacing.md))
            Text(
                text = value.ifEmpty { placeholder },
                style = MaterialTheme.typography.bodyMedium,
                color = if (value.isEmpty()) c.textTertiary else c.textPrimary,
                maxLines = 1,
            )
        }
    } else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = PulseSpacing.screen)
                .clip(PulseShapes.full)
                .background(c.surfaceVariant)
                .padding(start = 4.dp, end = PulseSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = AppIcons.ArrowLeft,
                    contentDescription = "Back",
                    modifier = Modifier.size(PulseIconSizes.inline),
                    tint = c.textSecondary,
                )
            }
            Spacer(Modifier.width(PulseSpacing.xs))
            Box(Modifier.weight(1f)) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = c.textPrimary),
                    cursorBrush = SolidColor(c.accent),
                    decorationBox = { inner ->
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = c.textTertiary,
                            )
                        }
                        inner()
                    },
                )
            }
            if (value.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onValueChange("") },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = AppIcons.Close,
                        contentDescription = "Clear search",
                        modifier = Modifier.size(PulseIconSizes.inline),
                        tint = c.textSecondary,
                    )
                }
            }
        }
    }
}
