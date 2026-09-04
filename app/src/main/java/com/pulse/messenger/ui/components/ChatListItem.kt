package com.pulse.messenger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulse.messenger.R
import com.pulse.messenger.domain.model.ChatSummary
import com.pulse.messenger.domain.model.MessageStatus
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.theme.AvatarTones
import com.pulse.messenger.ui.theme.PulseIconSizes
import com.pulse.messenger.ui.theme.PulseShapes
import com.pulse.messenger.ui.theme.PulseSizes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme
import com.pulse.messenger.ui.util.MessageLabels
import com.pulse.messenger.ui.util.TimeFormat

/**
 * Chat list row (PRD S19/S21 - ChatListItem): 52dp avatar with online/group
 * states, title + pin/verified markers, preview line (sender prefix, media
 * label, typing…, draft), timestamp, unread badge or delivery ticks.
 * Selection checkbox overlay is provided for multi-select mode.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatListItem(
    summary: ChatSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    selectionMode: Boolean = false,
    selected: Boolean = false,
) {
    val c = PulseTheme.colors
    val last = summary.lastMessage
    val hasUnread = summary.unreadCount > 0

    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .then(
                if (selectionMode && selected) {
                    Modifier.background(c.accentContainerMuted)
                } else {
                    Modifier
                },
            )
            .padding(horizontal = PulseSpacing.screen, vertical = PulseSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Selection checkbox / avatar column
        if (selectionMode) {
            SelectionCheck(selected = selected)
            Spacer(Modifier.width(PulseSpacing.md))
        }
        Box {
            Avatar(
                name = summary.displayName,
                avatarTone = AvatarTones.bySeed(summary.avatarSeed),
                size = PulseSizes.avatarChat,
                isOnline = false,
                isGroup = summary.isGroup,
                groupMemberNames = summary.memberNames.take(2),
            )
        }
        Spacer(Modifier.width(PulseSpacing.lg))

        // Middle: title line + preview line
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = summary.displayName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (hasUnread) FontWeight.SemiBold else FontWeight.Medium,
                    ),
                    color = if (hasUnread) c.textPrimary else c.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (summary.isPinned) {
                    Spacer(Modifier.width(PulseSpacing.xs))
                    Icon(
                        imageVector = AppIcons.Pin,
                        contentDescription = stringResource(R.string.chats_pin_cd),
                        modifier = Modifier.size(14.dp),
                        tint = c.textTertiary,
                    )
                }
                if (summary.isVerified) {
                    Spacer(Modifier.width(PulseSpacing.xs))
                    Icon(
                        imageVector = AppIcons.BadgeCheck,
                        contentDescription = stringResource(R.string.chats_verified_cd),
                        modifier = Modifier.size(14.dp),
                        tint = c.info,
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
            PreviewLine(summary = summary, unread = hasUnread)
        }

        Spacer(Modifier.width(PulseSpacing.sm))

        // Trailing: timestamp + badge/ticks
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = last?.let { TimeFormat.chatTimestamp(it.sentAtMillis) } ?: "",
                style = MaterialTheme.typography.labelMedium,
                color = if (hasUnread) c.textPrimary else c.textTertiary,
            )
            Spacer(Modifier.height(PulseSpacing.xs))
            when {
                hasUnread -> Badge(
                    count = summary.unreadCount,
                    muted = summary.isMuted,
                )
                summary.draft != null -> {
                    // Draft indicator (grey dot) - the preview line carries "Draft:".
                    Icon(
                        imageVector = AppIcons.Pencil,
                        contentDescription = stringResource(R.string.chats_draft_cd),
                        modifier = Modifier.size(PulseIconSizes.inline),
                        tint = c.textTertiary,
                    )
                }
                last?.isOutgoing == true -> DeliveryTicks(status = last.status)
                else -> Box(Modifier.height(20.dp))
            }
        }
    }
}

/** Preview line: draft prefix, typing state, sender prefix + media label/text. */
@Composable
private fun PreviewLine(summary: ChatSummary, unread: Boolean) {
    val c = PulseTheme.colors
    val last = summary.lastMessage

    val text: String
    val color: Color
    when {
        summary.isTyping -> {
            text = stringResource(R.string.chats_typing)
            color = c.accent
        }
        summary.draft != null -> {
            text = summary.draft.orEmpty()
            color = if (unread) c.textPrimary else c.textSecondary
        }
        else -> {
            text = buildString {
                if (summary.isGroup && last?.isOutgoing == false && last?.senderId != "me") {
                    append(summary.lastSenderFirstName ?: "")
                    append(": ")
                }
                val label = last?.let { MessageLabels.typeLabel(it.type) }
                if (label != null) append(label)
                else append(last?.text.orEmpty())
            }
            color = if (unread) c.textPrimary else c.textSecondary
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (summary.draft != null && !summary.isTyping) {
            Text(
                text = stringResource(R.string.chats_draft_prefix),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = c.error,
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (unread) FontWeight.Medium else FontWeight.Normal,
            ),
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Small round selection checkbox used in multi-select mode. */
@Composable
fun SelectionCheck(
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    val c = PulseTheme.colors
    Box(
        modifier = modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(if (selected) c.accent else Color.Transparent)
            .border(
                width = 2.dp,
                color = if (selected) c.accent else c.border,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                imageVector = AppIcons.Check,
                contentDescription = stringResource(R.string.chats_selected_cd),
                modifier = Modifier.size(13.dp),
                tint = c.onAccent,
            )
        }
    }
}

/** Single/double delivery ticks for own messages (S19 right column). */
@Composable
fun DeliveryTicks(
    status: MessageStatus,
    modifier: Modifier = Modifier,
) {
    val c = PulseTheme.colors
    val color = if (status == MessageStatus.Read) c.accent else c.textTertiary
    Icon(
        imageVector = when (status) {
            MessageStatus.Read, MessageStatus.Delivered -> AppIcons.CheckCheck
            else -> AppIcons.Check
        },
        contentDescription = null,
        modifier = modifier.size(16.dp),
        tint = color,
    )
}
