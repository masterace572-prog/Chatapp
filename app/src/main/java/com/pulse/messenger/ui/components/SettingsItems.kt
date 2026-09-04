package com.pulse.messenger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.theme.PulseIconSizes
import com.pulse.messenger.ui.theme.PulseShapes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme

/**
 * Section header above grouped lists/settings (PRD §7 - SectionHeader).
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val c = PulseTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = PulseSpacing.xxl, bottom = PulseSpacing.sm)
            .padding(horizontal = PulseSpacing.screen),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = c.textSecondary,
            modifier = Modifier.weight(1f),
            maxLines = 1,
        )
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = c.accent,
                modifier = Modifier
                    .clip(PulseShapes.sm)
                    .clickable(onClick = onAction)
                    .padding(horizontal = PulseSpacing.xs, vertical = 2.dp),
                maxLines = 1,
            )
        }
    }
}

/**
 * Standard settings/list row with a tinted icon tile (PRD §7 - SettingsItem).
 */
@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    value: String? = null,
    iconTint: Color = PulseTheme.colors.accent,
    iconContainer: Color = PulseTheme.colors.accentContainer,
    destructive: Boolean = false,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val c = PulseTheme.colors
    val titleColor = if (destructive) c.error else c.textPrimary
    val tint = if (destructive) c.error else iconTint
    val container = if (destructive) c.error.copy(alpha = 0.1f) else iconContainer

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(PulseShapes.md)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = PulseSpacing.screen, vertical = PulseSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(color = container, shape = PulseShapes.sm),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(PulseIconSizes.inline),
                tint = tint,
            )
        }
        Spacer(Modifier.width(PulseSpacing.lg))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) titleColor else titleColor.copy(alpha = 0.5f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = c.textTertiary,
                maxLines = 1,
            )
        }
        if (!destructive && trailingIcon == null) {
            Spacer(Modifier.width(PulseSpacing.xs))
            Icon(
                imageVector = AppIcons.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(PulseIconSizes.inline),
                tint = c.textTertiary,
            )
        }
    }
}

/**
 * Settings row ending in a themed switch (PRD §7 - SettingsSwitchItem).
 */
@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    iconTint: Color = PulseTheme.colors.accent,
    iconContainer: Color = PulseTheme.colors.accentContainer,
    enabled: Boolean = true,
) {
    val c = PulseTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PulseSpacing.screen, vertical = PulseSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(color = iconContainer, shape = PulseShapes.sm),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(PulseIconSizes.inline),
                tint = iconTint,
            )
        }
        Spacer(Modifier.width(PulseSpacing.lg))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) c.textPrimary else c.textTertiary,
                maxLines = 1,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textSecondary,
                    maxLines = 2,
                )
            }
        }
        AppSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
    }
}
