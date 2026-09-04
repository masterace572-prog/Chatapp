package com.pulse.messenger.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pulse.messenger.R
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.theme.AvatarTone
import com.pulse.messenger.ui.theme.AvatarTones
import com.pulse.messenger.ui.theme.PulseSizes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme

/** Menu actions of the chat header (S23). Mute is wired; the rest are M4d+. */
enum class ChatHeaderAction {
    ViewProfile, InChatSearch, ToggleMute, Wallpaper, ClearChat, Block, Report,
}

/** Status line flavours of the chat header (S23). */
enum class ChatHeaderStatus {
    /** Green dot + "online". */
    Online,

    /** Accent "typing…". */
    Typing,

    /** Group: "N members" in textSecondary. */
    Group,

    /** Plain secondary text (e.g. "last seen …", muted note). */
    Neutral,
}

/**
 * Conversation top bar (PRD §7 AppTopBar chat-header variant + S23):
 * back with the total-unread-across-other-chats badge, 40dp avatar,
 * name (titleMedium) + status line, trailing voice/video call icons and the
 * "more" menu. Stateless: the screen resolves the title/status and handles
 * actions (mute is wired to the mock; remaining items show placeholders).
 */
@Composable
fun ChatHeader(
    title: String,
    statusText: String,
    modifier: Modifier = Modifier,
    status: ChatHeaderStatus = ChatHeaderStatus.Neutral,
    avatarSeed: Int = 0,
    avatarName: String? = null,
    isGroup: Boolean = false,
    memberNames: List<String> = emptyList(),
    otherUnread: Int = 0,
    onBack: () -> Unit = {},
    onVoiceCall: () -> Unit = {},
    onVideoCall: () -> Unit = {},
    onMenuAction: (ChatHeaderAction) -> Unit = {},
    muted: Boolean = false,
    blocked: Boolean = false,
) {
    val c = PulseTheme.colors
    var menuOpen by remember { mutableStateOf(false) }

    Surface(color = c.background, modifier = modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PulseSizes.topBarHeight)
                    .padding(horizontal = PulseSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Back with an unread badge of the OTHER chats.
                Box {
                    AppBackButton(onBack = onBack)
                    if (otherUnread > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 1.dp, end = 1.dp)
                                .size(com.pulse.messenger.ui.theme.PulseSizes.unreadOverlayBadge)
                                .clip(CircleShape)
                                .background(c.accent),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (otherUnread > 9) "9+" else otherUnread.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = c.onAccent,
                                    fontSize = MaterialTheme.typography.labelSmall.fontSize,
                                ),
                            )
                        }
                    }
                }
                Spacer(Modifier.width(PulseSpacing.xs))

                Avatar(
                    name = avatarName ?: title,
                    avatarTone = avatarTone(avatarSeed),
                    size = PulseSizes.avatarHeader,
                    isOnline = status == ChatHeaderStatus.Online,
                    isGroup = isGroup,
                    groupMemberNames = memberNames.take(2),
                    onlineRingColor = c.background,
                )
                Spacer(Modifier.width(PulseSpacing.md))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = c.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (status == ChatHeaderStatus.Online || status == ChatHeaderStatus.Typing) {
                            val dotColor = when (status) {
                                ChatHeaderStatus.Typing -> c.accent
                                else -> c.success
                            }
                            Box(
                                Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(dotColor),
                            )
                            Spacer(Modifier.width(PulseSpacing.xs))
                        }
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelMedium,
                            color = when (status) {
                                ChatHeaderStatus.Typing -> c.accent
                                ChatHeaderStatus.Online -> c.success
                                else -> c.textSecondary
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                AppIconButton(
                    icon = AppIcons.Phone,
                    contentDescription = stringResource(R.string.conversation_call_cd),
                    onClick = onVoiceCall,
                    tint = c.textSecondary,
                )
                AppIconButton(
                    icon = AppIcons.Video,
                    contentDescription = stringResource(R.string.conversation_video_cd),
                    onClick = onVideoCall,
                    tint = c.textSecondary,
                )
                AppIconButton(
                    icon = AppIcons.EllipsisVertical,
                    contentDescription = stringResource(R.string.conversation_more_cd),
                    onClick = { menuOpen = true },
                    tint = c.textSecondary,
                )
            }

            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
                containerColor = c.surface,
            ) {
                HeaderMenuItem(AppIcons.User, stringResource(R.string.conversation_menu_view_profile)) {
                    menuOpen = false
                    onMenuAction(ChatHeaderAction.ViewProfile)
                }
                HeaderMenuItem(AppIcons.Search, stringResource(R.string.conversation_menu_search)) {
                    menuOpen = false
                    onMenuAction(ChatHeaderAction.InChatSearch)
                }
                HeaderMenuItem(
                    if (muted) AppIcons.Bell else AppIcons.BellOff,
                    stringResource(if (muted) R.string.conversation_menu_unmute else R.string.conversation_menu_mute),
                ) {
                    menuOpen = false
                    onMenuAction(ChatHeaderAction.ToggleMute)
                }
                HeaderMenuItem(AppIcons.Palette, stringResource(R.string.conversation_menu_wallpaper)) {
                    menuOpen = false
                    onMenuAction(ChatHeaderAction.Wallpaper)
                }
                HeaderMenuItem(AppIcons.Trash, stringResource(R.string.conversation_menu_clear_chat)) {
                    menuOpen = false
                    onMenuAction(ChatHeaderAction.ClearChat)
                }
                HeaderMenuItem(
                    AppIcons.Ban,
                    stringResource(
                        if (blocked) R.string.conversation_menu_unblock
                        else R.string.conversation_menu_block,
                    ),
                ) {
                    menuOpen = false
                    onMenuAction(ChatHeaderAction.Block)
                }
                HeaderMenuItem(AppIcons.Flag, stringResource(R.string.conversation_menu_report)) {
                    menuOpen = false
                    onMenuAction(ChatHeaderAction.Report)
                }
            }
        }
    }
}

@Composable
private fun HeaderMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    val c = PulseTheme.colors
    DropdownMenuItem(
        text = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = c.textPrimary,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(com.pulse.messenger.ui.theme.PulseIconSizes.inline),
                tint = c.textSecondary,
            )
        },
        onClick = onClick,
    )
}

private fun avatarTone(seed: Int): AvatarTone = AvatarTones.bySeed(seed)
