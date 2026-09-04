package com.pulse.messenger.ui.screens.conversation

import androidx.compose.foundation.ExperimentalFoundationApi

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulse.messenger.R
import com.pulse.messenger.domain.model.Chat
import com.pulse.messenger.domain.model.ChatKind
import com.pulse.messenger.domain.model.MessageContent
import com.pulse.messenger.domain.model.MessageStatus
import com.pulse.messenger.domain.model.MessageType
import com.pulse.messenger.domain.model.User
import com.pulse.messenger.ui.components.ChatHeader
import com.pulse.messenger.ui.components.ChatHeaderAction
import com.pulse.messenger.ui.components.ChatHeaderStatus
import com.pulse.messenger.ui.components.DateSeparator
import com.pulse.messenger.ui.components.EmptyState
import com.pulse.messenger.ui.components.MessageBubble
import com.pulse.messenger.ui.components.MessageComposer
import com.pulse.messenger.ui.components.SystemMessageRow
import com.pulse.messenger.ui.components.TypingIndicator
import com.pulse.messenger.ui.components.UnreadDivider
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.theme.AvatarTones
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme
import com.pulse.messenger.ui.theme.senderToneTextColor
import com.pulse.messenger.ui.util.ConversationFormat
import kotlinx.coroutines.launch

/** User events the screen forwards to the ViewModel (keeps UI decoupled). */
data class ConversationCallbacks(
    val onBack: () -> Unit = {},
    val onRetry: (String) -> Unit = {},
    val onDraftChange: (String) -> Unit = {},
    val onSend: () -> Unit = {},
    val onToggleMuted: () -> Unit = {},
    val onScrolledToBottom: () -> Unit = {},
    val onScrolledUp: () -> Unit = {},
    val onAttachment: () -> Unit = {},
    val onCamera: () -> Unit = {},
    val onMic: () -> Unit = {},
    val onVoiceCall: () -> Unit = {},
    val onVideoCall: () -> Unit = {},
)

/**
 * S23 conversation screen core (M4a): real header, message list with day /
 * unread / typing rows, and the basic composer. Long-press actions, replies,
 * reactions, multi-select and the pinned banner are M4b and intentionally
 * absent; media content types render as labelled placeholders.
 */
@Composable
fun ConversationScreen(
    vm: ConversationViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    DisposableEffect(Unit) {
        vm.onScreenOpened()
        onDispose { vm.onScreenClosed() }
    }

    val callbacks = remember {
        ConversationCallbacks(
            onBack = onBack,
            onRetry = vm::retry,
            onDraftChange = vm::onDraftChange,
            onSend = vm::send,
            onToggleMuted = vm::toggleMuted,
            onScrolledToBottom = vm::scrolledToBottom,
            onScrolledUp = vm::scrolledUp,
            onAttachment = { toastComingSoon(context, R.string.conversation_composer_attachment_cd) },
            onCamera = { toastComingSoon(context, R.string.conversation_composer_camera_cd) },
            onMic = { toastComingSoon(context, R.string.conversation_composer_mic_cd) },
            onVoiceCall = { toastComingSoon(context, R.string.conversation_call_cd) },
            onVideoCall = { toastComingSoon(context, R.string.conversation_video_cd) },
        )
    }

    ConversationContent(
        state = state,
        callbacks = callbacks,
        modifier = modifier,
    )
}

/**
 * Stateless conversation body - shares its layout with the previews.
 * [ConversationScreen] collects the ViewModel and forwards events here.
 */
@Composable
internal fun ConversationContent(
    state: ConversationUiState,
    callbacks: ConversationCallbacks = ConversationCallbacks(),
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PulseTheme.colors.background)
            .imePadding(),
    ) {
        state.chat?.let { chat ->
            ChatHeader(
                title = headerTitle(chat, state.users),
                statusText = headerStatusText(chat, state),
                status = headerStatus(chat, state),
                avatarSeed = headerAvatarSeed(chat, state.users),
                avatarName = headerAvatarName(chat, state.users),
                isGroup = chat.isGroup,
                memberNames = headerMemberNames(chat, state.users),
                otherUnread = state.otherUnread,
                muted = chat.isMuted,
                onBack = callbacks.onBack,
                onVoiceCall = callbacks.onVoiceCall,
                onVideoCall = callbacks.onVideoCall,
                onMenuAction = { action ->
                    if (action == ChatHeaderAction.ToggleMute) {
                        callbacks.onToggleMuted()
                    } else {
                        toastComingSoon(context, menuLabelRes(action))
                    }
                },
            )
        }

        ConversationList(
            state = state,
            isGroup = state.chat?.isGroup == true,
            modifier = Modifier.weight(1f),
            onRetry = callbacks.onRetry,
            onScrolledToBottom = callbacks.onScrolledToBottom,
            onScrolledUp = callbacks.onScrolledUp,
        )

        MessageComposer(
            value = state.draftText,
            onValueChange = callbacks.onDraftChange,
            onSend = callbacks.onSend,
            onAttachment = callbacks.onAttachment,
            onCamera = callbacks.onCamera,
            onMic = callbacks.onMic,
            modifier = Modifier.navigationBarsPadding(),
        )
    }
}

/** Menu item label resources (toast titles for M4b-M4d placeholders). */
private fun menuLabelRes(action: ChatHeaderAction): Int = when (action) {
    ChatHeaderAction.ViewProfile -> R.string.conversation_menu_view_profile
    ChatHeaderAction.InChatSearch -> R.string.conversation_menu_search
    ChatHeaderAction.Wallpaper -> R.string.conversation_menu_wallpaper
    ChatHeaderAction.ClearChat -> R.string.conversation_menu_clear_chat
    ChatHeaderAction.Block -> R.string.conversation_menu_block
    ChatHeaderAction.Report -> R.string.conversation_menu_report
    ChatHeaderAction.ToggleMute -> R.string.conversation_menu_mute
}

/* ---------- Header resolution ---------- */

private fun headerTitle(chat: Chat, users: Map<String, User>): String {
    if (chat.isGroup) return chat.title ?: "Group"
    val peer = users[chat.participantIds.firstOrNull { it != "me" }]
    return peer?.displayName ?: "Chat"
}

private fun headerAvatarName(chat: Chat, users: Map<String, User>): String? =
    if (chat.isGroup) chat.title else headerTitle(chat, users)

private fun headerAvatarSeed(chat: Chat, users: Map<String, User>): Int {
    if (chat.isGroup) return chat.avatarSeed
    return users[chat.participantIds.firstOrNull { it != "me" }]?.avatarSeed ?: chat.avatarSeed
}

private fun headerMemberNames(chat: Chat, users: Map<String, User>): List<String> =
    if (chat.isGroup) {
        chat.participantIds.filter { it != "me" }.mapNotNull { users[it]?.firstName }
    } else {
        emptyList()
    }

@Composable
private fun headerStatus(chat: Chat, state: ConversationUiState): ChatHeaderStatus {
    if (chat.isGroup) return ChatHeaderStatus.Group
    if (state.rows.any { it is ConversationRow.Typing }) return ChatHeaderStatus.Typing
    val peer = state.users[chat.participantIds.firstOrNull { it != "me" }]
    return if (peer?.isOnline == true) ChatHeaderStatus.Online else ChatHeaderStatus.Neutral
}

@Composable
private fun headerStatusText(chat: Chat, state: ConversationUiState): String {
    val typing = state.rows.any { it is ConversationRow.Typing }
    val peer = state.users[chat.participantIds.firstOrNull { it != "me" }]
    return when {
        chat.isGroup -> stringResource(R.string.conversation_members, chat.participantIds.size)
        typing -> stringResource(R.string.conversation_typing)
        peer?.isOnline == true -> stringResource(R.string.conversation_online)
        else -> ConversationFormat.lastSeen(peer?.lastSeenAtMillis)
    }
}

/* ---------- Message list ---------- */

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ConversationList(
    state: ConversationUiState,
    isGroup: Boolean,
    modifier: Modifier = Modifier,
    onRetry: (String) -> Unit = {},
    onScrolledToBottom: () -> Unit = {},
    onScrolledUp: () -> Unit = {},
) {
    val c = PulseTheme.colors
    val listState = rememberLazyListState()

    val atBottom by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        }
    }
    LaunchedEffect(atBottom) {
        if (atBottom) onScrolledToBottom() else onScrolledUp()
    }

    if (state.loading || state.rows.isEmpty()) {
        EmptyState(
            icon = AppIcons.MessageCircle,
            title = if (state.loading) "" else stringResource(R.string.conversation_empty_title),
            modifier = modifier,
        )
        return
    }

    Box(modifier = modifier.fillMaxWidth()) {
        LazyColumn(
            state = listState,
            reverseLayout = true,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(PulseSpacing.tight),
            contentPadding = PaddingValues(vertical = PulseSpacing.sm),
        ) {
            items(
                count = state.rows.size,
                key = { index -> state.rows[index].key },
            ) { index ->
                RowItem(
                    row = state.rows[index],
                    users = state.users,
                    isGroup = isGroup,
                    onRetry = onRetry,
                    modifier = Modifier.animateItem(fadeInSpec = tween(220)),
                )
            }
        }

        val showFab by remember { derivedStateOf { !atBottom } }
        if (showFab) {
            val scope = rememberCoroutineScope()
            Surface(
                onClick = {
                    scope.launch { listState.animateScrollToItem(0) }
                },
                shape = CircleShape,
                color = c.surface,
                border = BorderStroke(1.dp, c.border),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(PulseSpacing.sm),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = PulseSpacing.md, vertical = PulseSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = AppIcons.ArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(PulseSpacing.xl),
                        tint = c.textSecondary,
                    )
                    if (state.unseenCount > 0) {
                        Spacer(Modifier.width(PulseSpacing.xs))
                        Text(
                            text = state.unseenCount.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = c.textSecondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RowItem(
    row: ConversationRow,
    users: Map<String, User>,
    isGroup: Boolean,
    onRetry: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (row) {
        is ConversationRow.Day -> DateSeparator(
            label = ConversationFormat.dayLabel(row.dateMillis),
            modifier = modifier.padding(vertical = PulseSpacing.xs),
        )
        is ConversationRow.Unread -> UnreadDivider(
            label = pluralStringResource(R.plurals.conversation_unread_messages, row.count, row.count),
            modifier = modifier.padding(vertical = PulseSpacing.xs),
        )
        is ConversationRow.Typing -> {
            val c = PulseTheme.colors
            TypingIndicator(
                senderName = row.senderName,
                senderNameColor = row.senderSeed?.let {
                    senderToneTextColor(AvatarTones.bySeed(it), c.isDark)
                } ?: c.textSecondary,
                modifier = modifier,
            )
        }
        is ConversationRow.MessageItem -> {
            val message = row.message
            if (message.content is MessageContent.System || message.type == MessageType.System) {
                SystemMessageRow(
                    text = message.text,
                    modifier = modifier.padding(vertical = PulseSpacing.xs),
                )
            } else {
                val c = PulseTheme.colors
                val nameColor = row.senderSeed?.let {
                    senderToneTextColor(AvatarTones.bySeed(it), c.isDark)
                }
                MessageBubble(
                    message = message,
                    isFirstInRun = row.isFirstInRun,
                    isLastInRun = row.isLastInRun,
                    senderName = if (isGroup && !message.isOutgoing && row.isFirstInRun) {
                        row.senderName
                    } else {
                        null
                    },
                    senderNameColor = nameColor,
                    avatarSeed = if (isGroup && !message.isOutgoing) row.senderSeed else null,
                    onRetry = if (message.isOutgoing && message.status == MessageStatus.Failed) {
                        { onRetry(message.id) }
                    } else {
                        null
                    },
                    modifier = modifier,
                )
            }
        }
    }
}

/* ---------- Placeholder toasts ---------- */

private fun toastComingSoon(context: Context, labelRes: Int) {
    val label = context.getString(labelRes)
    Toast.makeText(context, context.getString(R.string.conversation_coming_soon, label), Toast.LENGTH_SHORT).show()
}
