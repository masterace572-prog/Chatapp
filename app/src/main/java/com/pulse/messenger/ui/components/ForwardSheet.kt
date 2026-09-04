package com.pulse.messenger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pulse.messenger.R
import com.pulse.messenger.domain.model.ChatSummary
import com.pulse.messenger.domain.model.Message
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.theme.AvatarTones
import com.pulse.messenger.ui.theme.PulseIconSizes
import com.pulse.messenger.ui.theme.PulseShapes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme
import com.pulse.messenger.ui.util.MessageLabels
import com.pulse.messenger.ui.util.TimeFormat

/**
 * Forward sheet (S34, moved INTO M4b - it is a core message action):
 * full-height bottom sheet with a message preview bar, optional comment
 * field, search, a "Recent" avatar row and the chat list (reusing
 * [ChatListItem]'s selectable variant). Selected targets show as removable
 * avatar chips; the send pill carries the target count.
 */
@Composable
fun ForwardSheet(
    chats: List<ChatSummary>,
    messages: List<Message>,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    onSend: (targetChatIds: List<String>, comment: String) -> Unit = { _: List<String>, _: String -> },
) {
    val c = PulseTheme.colors
    var query by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(setOf<String>()) }

    val filtered = remember(chats, query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) {
            chats
        } else {
            chats.filter { it.displayName.lowercase().contains(q) }
        }
    }
    val recent = remember(chats) { chats.take(6) }
    val selectedChats = remember(chats, selected) {
        chats.filter { it.chatId in selected }
    }

    fun toggle(chatId: String) {
        selected = if (chatId in selected) selected - chatId else selected + chatId
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PulseSpacing.screen),
        verticalArrangement = Arrangement.spacedBy(PulseSpacing.sm),
    ) {
        // Header: title + message preview count.
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.forward_title),
                style = MaterialTheme.typography.titleLarge,
                color = c.textPrimary,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = pluralStringResource(R.plurals.forward_message_count, messages.size, messages.size),
                style = MaterialTheme.typography.labelMedium,
                color = c.textSecondary,
            )
        }

        // Message preview bar.
        ForwardPreviewBar(messages = messages)

        // Comment field.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(PulseShapes.md)
                .background(c.surfaceVariant)
                .padding(horizontal = PulseSpacing.md, vertical = 4.dp),
        ) {
            Icon(
                imageVector = AppIcons.Pencil,
                contentDescription = null,
                modifier = Modifier.size(PulseIconSizes.inline),
                tint = c.textTertiary,
            )
            Spacer(Modifier.width(PulseSpacing.sm))
            BasicTextField(
                value = comment,
                onValueChange = { comment = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = c.textPrimary),
                cursorBrush = SolidColor(c.accent),
                singleLine = true,
                decorationBox = { inner ->
                    Box {
                        if (comment.isEmpty()) {
                            Text(
                                text = stringResource(R.string.forward_comment_hint),
                                style = MaterialTheme.typography.bodyMedium,
                                color = c.textTertiary,
                            )
                        }
                        inner()
                    }
                },
            )
        }

        // Selected targets as removable avatar chips.
        if (selectedChats.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(PulseSpacing.sm)) {
                items(selectedChats, key = { it.chatId }) { chat ->
                    SelectedChip(chat = chat) { toggle(chat.chatId) }
                }
            }
        }

        // Search field.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(PulseShapes.full)
                .background(c.surfaceVariant)
                .padding(horizontal = PulseSpacing.md, vertical = 6.dp),
        ) {
            Icon(
                imageVector = AppIcons.Search,
                contentDescription = null,
                modifier = Modifier.size(PulseIconSizes.inline),
                tint = c.textTertiary,
            )
            Spacer(Modifier.width(PulseSpacing.sm))
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = c.textPrimary),
                cursorBrush = SolidColor(c.accent),
                singleLine = true,
                decorationBox = { inner ->
                    Box {
                        if (query.isEmpty()) {
                            Text(
                                text = stringResource(R.string.forward_search_hint),
                                style = MaterialTheme.typography.bodyMedium,
                                color = c.textTertiary,
                            )
                        }
                        inner()
                    }
                },
            )
        }

        // "Recent" quick picks.
        if (query.isBlank()) {
            Text(
                text = stringResource(R.string.forward_recent),
                style = MaterialTheme.typography.labelLarge,
                color = c.textSecondary,
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(PulseSpacing.lg)) {
                items(recent, key = { it.chatId }) { chat ->
                    RecentPick(
                        chat = chat,
                        checked = chat.chatId in selected,
                        onClick = { toggle(chat.chatId) },
                    )
                }
            }
        }

        // Chat list (ChatListItem selectable variant - single source of rows).
        if (filtered.isEmpty()) {
            Text(
                text = stringResource(R.string.forward_no_results),
                style = MaterialTheme.typography.bodyMedium,
                color = c.textSecondary,
                modifier = Modifier.padding(PulseSpacing.lg),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 12.dp),
            ) {
                items(filtered, key = { it.chatId }) { chat ->
                    ChatListItem(
                        summary = chat,
                        onClick = { toggle(chat.chatId) },
                        modifier = Modifier.padding(vertical = PulseSpacing.tight),
                        selectionMode = true,
                        selected = chat.chatId in selected,
                    )
                }
            }
        }

        // Send pill with count.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            val enabled = selected.isNotEmpty()
            Surface(
                shape = PulseShapes.full,
                color = if (enabled) c.accent else c.surfaceVariant,
                modifier = Modifier.clickable(enabled = enabled) {
                    onSend(selected.toList(), comment.trim())
                },
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = PulseSpacing.lg, vertical = PulseSpacing.sm),
                ) {
                    Icon(
                        imageVector = AppIcons.Send,
                        contentDescription = null,
                        modifier = Modifier.size(PulseIconSizes.inline),
                        tint = if (enabled) c.onAccent else c.textTertiary,
                    )
                    Spacer(Modifier.width(PulseSpacing.xs))
                    Text(
                        text = if (enabled) {
                            stringResource(R.string.forward_send_count, selected.size)
                        } else {
                            stringResource(R.string.forward_send_cd)
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = if (enabled) c.onAccent else c.textTertiary,
                    )
                }
            }
        }
    }
}

/** Excerpt + type icon of the first message, "+N more". */
@Composable
private fun ForwardPreviewBar(messages: List<Message>) {
    val c = PulseTheme.colors
    val first = messages.firstOrNull() ?: return
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(PulseShapes.md)
            .background(c.accentContainerMuted)
            .padding(horizontal = PulseSpacing.md, vertical = PulseSpacing.sm),
    ) {
        if (first.type != com.pulse.messenger.domain.model.MessageType.Text) {
            Icon(
                imageVector = when (first.type) {
                    com.pulse.messenger.domain.model.MessageType.Voice -> AppIcons.Mic
                    com.pulse.messenger.domain.model.MessageType.Video -> AppIcons.Play
                    com.pulse.messenger.domain.model.MessageType.Image -> AppIcons.Image
                    com.pulse.messenger.domain.model.MessageType.File -> AppIcons.File
                    com.pulse.messenger.domain.model.MessageType.Location -> AppIcons.MapPin
                    com.pulse.messenger.domain.model.MessageType.Contact -> AppIcons.User
                    com.pulse.messenger.domain.model.MessageType.Poll -> AppIcons.List
                    com.pulse.messenger.domain.model.MessageType.Sticker -> AppIcons.Smile
                    else -> AppIcons.MessageCircle
                },
                contentDescription = null,
                modifier = Modifier.size(PulseIconSizes.inline),
                tint = c.accent,
            )
            Spacer(Modifier.width(PulseSpacing.sm))
            Text(
                text = MessageLabels.typeLabel(first.type) ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = c.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        } else {
            Text(
                text = first.text,
                style = MaterialTheme.typography.bodyMedium,
                color = c.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
        if (messages.size > 1) {
            Text(
                text = "+${messages.size - 1}",
                style = MaterialTheme.typography.labelMedium,
                color = c.textSecondary,
            )
        }
    }
}

/** Removable target chip (avatar + name + close). */
@Composable
private fun SelectedChip(chat: ChatSummary, onRemove: () -> Unit) {
    val c = PulseTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(PulseShapes.full)
            .background(c.surfaceVariant)
            .padding(start = PulseSpacing.xs, end = PulseSpacing.xs, top = PulseSpacing.xs, bottom = PulseSpacing.xs),
    ) {
        Avatar(
            name = chat.displayName,
            avatarTone = AvatarTones.bySeed(chat.avatarSeed),
            size = 22.dp,
            isGroup = chat.isGroup,
            groupMemberNames = chat.memberNames.take(2),
        )
        Spacer(Modifier.width(PulseSpacing.xs))
        Text(
            text = chat.displayName,
            style = MaterialTheme.typography.labelMedium,
            color = c.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.width(PulseSpacing.xs))
        Icon(
            imageVector = AppIcons.Close,
            contentDescription = stringResource(R.string.forward_remove_cd, chat.displayName),
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .clickable(onClick = onRemove),
            tint = c.textTertiary,
        )
    }
}

/** Recent chat avatar pick (name below, accent ring when selected). */
@Composable
private fun RecentPick(chat: ChatSummary, checked: Boolean, onClick: () -> Unit) {
    val c = PulseTheme.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (checked) c.accentContainer else c.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Avatar(
                name = chat.displayName,
                avatarTone = AvatarTones.bySeed(chat.avatarSeed),
                size = 42.dp,
                isGroup = chat.isGroup,
                groupMemberNames = chat.memberNames.take(2),
            )
            if (checked) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(c.accent),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = AppIcons.Check,
                        contentDescription = null,
                        modifier = Modifier.size(10.dp),
                        tint = c.onAccent,
                    )
                }
            }
        }
        Text(
            text = chat.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = c.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(64.dp),
        )
    }
}
