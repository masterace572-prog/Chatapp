package com.pulse.messenger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pulse.messenger.R
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.theme.PulseIconSizes
import com.pulse.messenger.ui.theme.PulseShapes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme
import com.pulse.messenger.ui.theme.PulseSizes

/** One row of the long-press action sheet. */
data class MessageActionItem(
    val icon: ImageVector,
    val labelRes: Int,
    val destructive: Boolean = false,
    val onClick: () -> Unit = {},
)

/**
 * Long-press scrim (S23): dims the conversation list while the pressed bubble
 * (drawn above it by the screen) stays visible and lifted. Any tap outside
 * the floating content dismisses via [onDismiss].
 */
@Composable
fun LongPressScrim(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = PulseTheme.colors
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(c.scrim)
            .clickable(onClick = onDismiss),
    )
}

/**
 * Pill-shaped quick-reaction bar above the long-pressed bubble: the six core
 * emoji (content) + a "+" that opens the emoji sheet.
 */
@Composable
fun QuickReactionBar(
    modifier: Modifier = Modifier,
    onReact: (String) -> Unit = {},
    onMore: () -> Unit = {},
) {
    val c = PulseTheme.colors
    Surface(
        shape = PulseShapes.full,
        color = c.surface,
        shadowElevation = 6.dp,
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = PulseSpacing.xs, vertical = PulseSpacing.xs),
        ) {
            ReactionEmoji.quick.forEachIndexed { index, emoji ->
                if (index > 0) Spacer(Modifier.width(2.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onReact(emoji) }
                        .padding(6.dp),
                ) {
                    Text(
                        text = emoji,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
            Spacer(Modifier.width(2.dp))
            Box(
                modifier = Modifier
                    .size(PulseSizes.minTouchTarget * 0.7f)
                    .clip(CircleShape)
                    .background(c.surfaceVariant)
                    .clickable(onClick = onMore),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = AppIcons.Plus,
                    contentDescription = stringResource(R.string.conversation_more_reactions_cd),
                    modifier = Modifier.size(PulseIconSizes.inline),
                    tint = c.textSecondary,
                )
            }
        }
    }
}

/**
 * The long-press action list (S23): Reply, Forward, Copy, Pin/Unpin,
 * Star/Unstar, Edit (own recent text), Info, Delete, Select. The screen picks
 * rows per message; this renders the sheet-shaped column on [Surface].
 */
@Composable
fun MessageActionSheet(
    items: List<MessageActionItem>,
    modifier: Modifier = Modifier,
) {
    val c = PulseTheme.colors
    Surface(
        shape = PulseShapes.sheetTop,
        color = c.surface,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = PulseSpacing.xs),
        ) {
            items.forEach { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = item.onClick)
                        .padding(horizontal = PulseSpacing.xl, vertical = PulseSpacing.sm),
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(PulseShapes.full)
                            .background(c.accentContainerMuted),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            modifier = Modifier.size(PulseIconSizes.inline),
                            tint = if (item.destructive) c.error else c.textPrimary,
                        )
                    }
                    Spacer(Modifier.width(PulseSpacing.lg))
                    Text(
                        text = stringResource(item.labelRes),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (item.destructive) c.error else c.textPrimary,
                    )
                }
            }
        }
    }
}

/** Emoji sheet content (opened from the quick bar "+"). */
@Composable
fun EmojiSheetContent(
    modifier: Modifier = Modifier,
    onEmoji: (String) -> Unit = {},
) {
    val c = PulseTheme.colors
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.conversation_reactions_title),
            style = MaterialTheme.typography.titleMedium,
            color = c.textPrimary,
        )
        Spacer(Modifier.height(PulseSpacing.sm))
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            modifier = Modifier
                .fillMaxWidth()
                .height(224.dp),
            horizontalArrangement = Arrangement.spacedBy(PulseSpacing.xs),
            verticalArrangement = Arrangement.spacedBy(PulseSpacing.xs),
        ) {
            items(ReactionEmoji.more) { emoji ->
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onEmoji(emoji) }
                        .padding(4.dp),
                ) {
                    Text(
                        text = emoji,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
        }
    }
}
