package com.pulse.messenger.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pulse.messenger.R
import com.pulse.messenger.domain.model.Message
import com.pulse.messenger.domain.model.MessageReaction
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.theme.AvatarTones
import com.pulse.messenger.ui.theme.PulseIconSizes
import com.pulse.messenger.ui.theme.PulseShapes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme

/** Quick-reaction emoji sets (emoji are message *content* in this UI). */
object ReactionEmoji {
    val quick = listOf("👍", "❤️", "😂", "😮", "😢", "🙏")
    val more = listOf(
        "👍", "👎", "❤️", "🧡", "💛", "💚", "💙", "💜",
        "🔥", "✨", "🎉", "👏", "🙏", "😮", "😢", "😂",
        "🤔", "😍", "🥳", "😎", "🤝", "💯", "👀", "🙌",
    )
}

/** One reaction pill: emoji + count; [includesMe] draws the own highlight. */
data class ReactionPillUi(
    val emoji: String,
    val count: Int,
    val includesMe: Boolean,
)

/** Builds pills for a message's reactions with the current user included. */
fun reactionPills(message: Message): List<ReactionPillUi> =
    message.reactions.map { r ->
        ReactionPillUi(
            emoji = r.emoji,
            count = r.userIds.size,
            includesMe = "me" in r.userIds,
        )
    }

/**
 * Reactions row under a bubble (S23): pills of emoji + count, aligned to the
 * bubble's side. Own reaction = accent border + accentContainer background.
 * Tap toggles; long-press opens the reactor list.
 */
@Composable
fun ReactionPillRow(
    pills: List<ReactionPillUi>,
    isOutgoing: Boolean,
    modifier: Modifier = Modifier,
    onToggle: (String) -> Unit = {},
    onLongPress: (String) -> Unit = {},
) {
    val c = PulseTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isOutgoing) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        pills.forEach { pill ->
            ReactionPill(
                pill = pill,
                modifier = Modifier.padding(
                    start = if (isOutgoing) PulseSpacing.xs else 0.dp,
                    end = if (isOutgoing) 0.dp else PulseSpacing.xs,
                ),
                onClick = { onToggle(pill.emoji) },
                onLongPress = { onLongPress(pill.emoji) },
            )
            Spacer(Modifier.width(PulseSpacing.xs))
        }
    }
}

/** Single pill; content is an emoji glyph + count. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ReactionPill(
    pill: ReactionPillUi,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = PulseTheme.colors
    val background = if (pill.includesMe) c.accentContainer else c.surfaceVariant
    val borderColor = if (pill.includesMe) c.accent else c.border
    Row(
        modifier = modifier
            .clip(PulseShapes.full)
            .background(background)
            .border(1.dp, borderColor, PulseShapes.full)
            .combinedClickable(onClick = onClick, onLongClick = onLongPress)
            .padding(horizontal = PulseSpacing.sm, vertical = PulseSpacing.tight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = pill.emoji,
            style = MaterialTheme.typography.labelMedium,
        )
        Spacer(Modifier.width(PulseSpacing.xs))
        Text(
            text = pill.count.toString(),
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (pill.includesMe) c.accent else c.textSecondary,
            ),
        )
    }
}

/** A reactor row shown in the who-reacted sheet. */
data class ReactorUi(val name: String, val avatarSeed: Int)

/**
 * Bottom-sheet content listing who reacted with a given emoji
 * (avatar + name); tap outside dismisses via [onDismissRequest].
 */
@Composable
fun ReactorsSheetContent(
    emoji: String,
    reactors: List<ReactorUi>,
    modifier: Modifier = Modifier,
) {
    val c = PulseTheme.colors
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(Modifier.width(PulseSpacing.sm))
            Text(
                text = stringResource(R.string.conversation_reactions_title),
                style = MaterialTheme.typography.titleMedium,
                color = c.textPrimary,
            )
        }
        Spacer(Modifier.height(PulseSpacing.sm))
        if (reactors.isEmpty()) {
            Text(
                text = stringResource(R.string.conversation_you),
                style = MaterialTheme.typography.bodyMedium,
                color = c.textSecondary,
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(reactors, key = { it.name }) { reactor ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = PulseSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Avatar(
                            name = reactor.name,
                            avatarTone = AvatarTones.bySeed(reactor.avatarSeed),
                            size = 36.dp,
                        )
                        Spacer(Modifier.width(PulseSpacing.md))
                        Text(
                            text = reactor.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = c.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}
