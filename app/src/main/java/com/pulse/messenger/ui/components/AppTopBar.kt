package com.pulse.messenger.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.theme.PulseSizes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme

enum class AppTopBarStyle {
    /** Tab-level titles, e.g. "Chats" (headlineMedium). */
    Large,

    /** Screen titles, e.g. chat partner name (titleMedium). */
    Medium,

    /** Compact in-context titles (titleMedium, no extra spacing). */
    Compact,
}

/**
 * Shared top bar (PRD component table - AppTopBar).
 * Draws its own background + status-bar inset so content never collides.
 * Icons live in `actions` / `navigationIcon` slots via AppIconButton.
 */
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    style: AppTopBarStyle = AppTopBarStyle.Medium,
    containerColor: Color = PulseTheme.colors.background,
    showDivider: Boolean = false,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val c = PulseTheme.colors
    Surface(color = containerColor, modifier = modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(PulseSizes.topBarHeight)
                    .padding(horizontal = PulseSpacing.screen),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (navigationIcon != null) {
                    navigationIcon()
                    Spacer(Modifier.width(PulseSpacing.xs))
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = when (style) {
                            AppTopBarStyle.Large -> MaterialTheme.typography.headlineMedium
                            AppTopBarStyle.Medium,
                            AppTopBarStyle.Compact,
                            -> MaterialTheme.typography.titleMedium
                        },
                        color = c.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = c.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Spacer(Modifier.width(PulseSpacing.sm))
                actions()
            }
            if (showDivider) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = c.border,
                )
            }
        }
    }
}

/** Back arrow in a top bar - routes the caller-provided onBack. */
@Composable
fun AppBackButton(
    onBack: () -> Unit,
    tint: Color = PulseTheme.colors.textSecondary,
    contentDescription: String = "Back",
) {
    AppIconButton(
        icon = AppIcons.ArrowLeft,
        contentDescription = contentDescription,
        onClick = onBack,
        tint = tint,
    )
}
