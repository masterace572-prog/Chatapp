package com.pulse.messenger.ui.screens.chats

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pulse.messenger.domain.model.ChatKind
import com.pulse.messenger.ui.components.AppDivider
import com.pulse.messenger.ui.components.ChatListItem
import com.pulse.messenger.domain.model.ChatSummary
import com.pulse.messenger.domain.model.Message
import com.pulse.messenger.domain.model.MessageContent
import com.pulse.messenger.domain.model.MessageStatus
import com.pulse.messenger.domain.model.MessageType
import com.pulse.messenger.ui.theme.PulseTheme

/*
 * S19/S21 list-row previews: light AND dark, key visual states of the chat
 * list row and its companions (row is composable-driven, no ViewModel).
 */

@Composable
private fun RowShowcase(content: @Composable () -> Unit) {
    PulseTheme {
        Surface(color = PulseTheme.colors.background) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PulseTheme.colors.background),
            ) { content() }
        }
    }
}

private fun sampleMessage(
    id: String,
    text: String,
    minutesAgo: Long,
    isOutgoing: Boolean = false,
    status: MessageStatus = MessageStatus.Read,
    type: MessageType = MessageType.Text,
): Message {
    val content: MessageContent = when (type) {
        MessageType.Text -> MessageContent.Text(text)
        MessageType.Image -> MessageContent.Image(listOf("sample://preview/image.png"), caption = text)
        MessageType.System -> MessageContent.System(text)
        MessageType.Video -> MessageContent.Video("sample://preview/video.mp4", durationSeconds = 8, caption = text)
        MessageType.Voice -> MessageContent.Voice(18, List(14) { 30 + it * 4 })
        MessageType.File -> MessageContent.File(text, 2048)
        MessageType.Location -> MessageContent.Location(0.0, 0.0, text)
        MessageType.Contact -> MessageContent.Contact("u-x", text)
        MessageType.Poll -> MessageContent.Poll(text, listOf("Option A", "Option B"))
        MessageType.Sticker -> MessageContent.Sticker(text)
    }
    return Message(
        id = id,
        chatId = "c-preview",
        senderId = if (isOutgoing) "me" else "u-1",
        content = content,
        sentAtMillis = System.currentTimeMillis() - minutesAgo * 60_000L,
        status = status,
        isOutgoing = isOutgoing,
    )
}

private fun summary(
    name: String,
    seed: Int,
    kind: ChatKind = ChatKind.Direct,
    last: Message? = sampleMessage("m", "Got the files — thanks!", 3),
    unread: Int = 0,
    pinned: Boolean = false,
    muted: Boolean = false,
    typing: Boolean = false,
    draft: String? = null,
    verified: Boolean = false,
    sender: String? = null,
    members: List<String> = emptyList(),
): ChatSummary = ChatSummary(
    chatId = "c-$name",
    kind = kind,
    displayName = name,
    avatarSeed = seed,
    memberNames = members,
    participantCount = members.size + 1,
    lastMessage = last,
    lastSenderFirstName = sender,
    unreadCount = unread,
    isMuted = muted,
    isPinned = pinned,
    isTyping = typing,
    draft = draft,
    isVerified = verified,
)

@Preview(name = "Chat rows · Light", showBackground = true, widthDp = 400)
@Composable
private fun ChatRowsLightPreview() = ChatRowsDemo()

@Preview(name = "Chat rows · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400)
@Composable
private fun ChatRowsDarkPreview() = ChatRowsDemo()

@Composable
private fun ChatRowsDemo() {
    RowShowcase {
        ChatListItem(
            summary = summary("Aria Kapoor", 3, unread = 3, verified = true),
            onClick = {},
        )
        AppDivider(insetStart = com.pulse.messenger.ui.theme.PulseSizes.chatRowDividerInset)
        ChatListItem(
            summary = summary("Design Team", 11, kind = ChatKind.Group, pinned = true, unread = 2, sender = "Kabir", members = listOf("Iva", "Dev", "Kabir"), last = sampleMessage("m2", "New spec is up now", 21)),
            onClick = {},
        )
        AppDivider(insetStart = com.pulse.messenger.ui.theme.PulseSizes.chatRowDividerInset)
        ChatListItem(
            summary = summary("Noah", 7, typing = true),
            onClick = {},
        )
        AppDivider(insetStart = com.pulse.messenger.ui.theme.PulseSizes.chatRowDividerInset)
        ChatListItem(
            summary = summary("Sam", 15, muted = true, unread = 4, last = sampleMessage("m3", "On my way over", 120)),
            onClick = {},
        )
        AppDivider(insetStart = com.pulse.messenger.ui.theme.PulseSizes.chatRowDividerInset)
        ChatListItem(
            summary = summary("Lea", 2, draft = "Call me back when you're free", last = sampleMessage("m4", "See you at 6", 300)),
            onClick = {},
        )
        AppDivider(insetStart = com.pulse.messenger.ui.theme.PulseSizes.chatRowDividerInset)
        ChatListItem(
            summary = summary("Me · outgoing read ticks", 5, last = sampleMessage("m5", "Sounds great!", 8, isOutgoing = true), sender = null),
            onClick = {},
        )
        ChatListItem(
            summary = summary("Photo share", 9, kind = ChatKind.Group, members = listOf("Aria", "Noah"), last = sampleMessage("m6", "IMG_2041.jpg", 45, type = MessageType.Image)),
            onClick = {},
        )
    }
}

@Preview(name = "Archived row · Light", showBackground = true, widthDp = 400)
@Preview(name = "Archived row · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400)
@Composable
private fun ArchivedRowPreview() {
    RowShowcase {
        ArchivedRow(count = 2, onClick = {})
        AppDivider()
    }
}

@Preview(name = "Multi-select bar · Light", showBackground = true, widthDp = 400)
@Preview(name = "Multi-select bar · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400)
@Composable
private fun SelectionTopBarPreview() {
    RowShowcase {
        SelectionTopBar(
            count = 3,
            onClose = {},
            onMarkRead = {},
            onArchive = {},
            onPin = {},
            onMute = {},
            onDelete = {},
        )
        AppDivider()
    }
}

@Preview(name = "Chat skeleton · Light", showBackground = true, widthDp = 400)
@Preview(name = "Chat skeleton · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400)
@Composable
private fun ChatSkeletonPreview() {
    RowShowcase {
        ChatListSkeleton(Modifier.padding(top = com.pulse.messenger.ui.theme.PulseSpacing.sm))
    }
}
