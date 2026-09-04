package com.pulse.messenger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pulse.messenger.R
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.theme.PulseIconSizes
import com.pulse.messenger.ui.theme.PulseSizes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme

/** One pinned message resolved for the banner. */
data class PinnedBannerData(
    val messageId: String,
    val excerpt: String,
)

/**
 * Pinned-message banner below the chat header (S23, M4b): pin icon +
 * "Pinned message" label + excerpt; tapping jumps to the message (and cycles
 * through several pinned ones - the vertical segment indicator shows where
 * the shown excerpt sits); the close icon asks to unpin.
 */
@Composable
fun PinnedBanner(
    items: List<PinnedBannerData>,
    displayIndex: Int,
    modifier: Modifier = Modifier,
    onTap: () -> Unit = {},
    onClose: () -> Unit = {},
) {
    val c = PulseTheme.colors
    val current = items.getOrNull(displayIndex % items.size.coerceAtLeast(1))
    Surface(color = c.surfaceVariant, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = current != null, onClick = onTap)
                .padding(start = PulseSpacing.lg, end = PulseSpacing.xs, top = PulseSpacing.sm, bottom = PulseSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (items.size > 1) {
                // Vertical segment indicator (position of the shown pin).
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(end = PulseSpacing.sm),
                ) {
                    items.forEachIndexed { i, _ ->
                        Box(
                            modifier = Modifier
                                .padding(vertical = 1.dp)
                                .size(width = 3.dp, height = 10.dp)
                                .clip(RoundedCornerShape(1.5.dp))
                                .background(if (i == displayIndex % items.size) c.accent else c.border),
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .size(PulseIconSizes.default)
                    .clip(CircleShape)
                    .background(c.accentContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = AppIcons.Pin,
                    contentDescription = null,
                    modifier = Modifier.size(PulseIconSizes.small),
                    tint = c.accent,
                )
            }
            Spacer(Modifier.width(PulseSpacing.md))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.conversation_pinned_message),
                    style = MaterialTheme.typography.labelMedium,
                    color = c.accent,
                )
                Text(
                    text = current?.excerpt.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (current != null) {
                Icon(
                    imageVector = AppIcons.Close,
                    contentDescription = stringResource(R.string.conversation_unpin_cd),
                    tint = c.textSecondary,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onClose)
                        .padding(PulseSpacing.xs),
                )
            }
        }
    }
}

/**
 * Multi-select top bar in a conversation (S23): close, "N selected" and the
 * batch actions Copy (text only), Star, Forward, Delete. Replaces the chat
 * header while [ConversationUiState.selectionMode] is on.
 */
@Composable
fun MultiSelectTopBar(
    count: Int,
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
    canCopy: Boolean = true,
    onCopy: () -> Unit = {},
    onForward: () -> Unit = {},
    onStar: () -> Unit = {},
    onDelete: () -> Unit = {},
) {
    val c = PulseTheme.colors
    Surface(color = c.background, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(PulseSizes.topBarHeight)
                .padding(horizontal = PulseSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIconButton(
                icon = AppIcons.Close,
                contentDescription = stringResource(R.string.conversation_close_selection_cd),
                onClick = onClose,
                tint = c.textSecondary,
            )
            Text(
                text = stringResource(R.string.chats_selected_count, count),
                style = MaterialTheme.typography.titleMedium,
                color = c.textPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            SelectBarAction(AppIcons.Copy, stringResource(R.string.conversation_action_copy), enabled = canCopy, onClick = onCopy, tint = c.textSecondary)
            SelectBarAction(AppIcons.Star, stringResource(R.string.conversation_action_star), onClick = onStar, tint = c.textSecondary)
            SelectBarAction(AppIcons.Share, stringResource(R.string.conversation_action_forward), onClick = onForward, tint = c.textSecondary)
            SelectBarAction(AppIcons.Trash, stringResource(R.string.conversation_action_delete), onClick = onDelete, tint = c.error)
        }
    }
}

@Composable
private fun SelectBarAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    tint: Color,
) {
    AppIconButton(
        icon = icon,
        contentDescription = contentDescription,
        onClick = onClick,
        tint = tint,
        enabled = enabled,
    )
}
