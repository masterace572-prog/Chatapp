package com.pulse.messenger.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pulse.messenger.domain.model.Chat
import com.pulse.messenger.domain.model.ChatKind
import com.pulse.messenger.domain.model.ChatRole
import com.pulse.messenger.domain.model.GroupMember
import com.pulse.messenger.domain.model.Message
import com.pulse.messenger.domain.model.MessageContent
import com.pulse.messenger.domain.model.MessageReaction
import com.pulse.messenger.domain.model.MessageStatus
import com.pulse.messenger.domain.model.User
import com.pulse.messenger.ui.screens.conversation.ConversationContent
import com.pulse.messenger.ui.screens.conversation.ConversationUiState
import com.pulse.messenger.ui.screens.conversation.ConversationRow
import com.pulse.messenger.ui.screens.conversation.buildConversationRows
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme

/* Previews for the M4a conversation surface (PRD §7: every component in
 * light AND dark). The rows below reuse the same stateless components the
 * live screen renders. */

private val aria = User("u-aria", "Aria", "Sharma", "aria.sh", avatarSeed = 1, isOnline = true)
private val noah = User("u-noah", "Noah", "Khan", "noahk", avatarSeed = 2)
private val mira = User("u-mira", "Mira", "Patel", "mirap", avatarSeed = 3)
private val me = User("me", "Aarav", "Kapoor", "aaravk", avatarSeed = 3)
private val previewUsers = listOf(aria, noah, mira, me).associateBy { it.id }

private val now = System.currentTimeMillis()
private fun t(minAgo: Long): Long = now - minAgo * 60_000L

private fun bubble(
    id: String,
    sender: String,
    minAgo: Long,
    content: MessageContent = MessageContent.Text(""),
    status: MessageStatus = MessageStatus.Read,
    edited: Boolean = false,
    deleted: Boolean = false,
    reactions: List<MessageReaction> = emptyList(),
): Message = Message(
    id = id,
    chatId = "c-preview",
    senderId = sender,
    content = content,
    sentAtMillis = t(minAgo),
    status = status,
    isOutgoing = sender == "me",
    isEdited = edited,
    isDeleted = deleted,
    reactions = reactions,
)

private fun text(id: String, sender: String, minAgo: Long, text: String, status: MessageStatus = MessageStatus.Read) =
    bubble(id, sender, minAgo, MessageContent.Text(text), status)

@Composable
private fun PreviewHost(content: @Composable () -> Unit) {
    PulseTheme {
        Surface(color = PulseTheme.colors.background, modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PulseSpacing.sm),
            ) {
                content()
            }
        }
    }
}

@Preview(name = "ChatHeader · Online (light)", showBackground = true, widthDp = 400)
@Preview(name = "ChatHeader · Online (dark)", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400)
@Composable
private fun ChatHeaderOnlinePreview() {
    PreviewHost {
        ChatHeader(
            title = "Aria Sharma",
            statusText = "online",
            status = ChatHeaderStatus.Online,
            avatarSeed = 1,
            otherUnread = 3,
        )
    }
}

@Preview(name = "ChatHeader · Typing (light)", showBackground = true, widthDp = 400)
@Preview(name = "ChatHeader · Typing (dark)", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400)
@Composable
private fun ChatHeaderTypingPreview() {
    PreviewHost {
        ChatHeader(
            title = "Noah Khan",
            statusText = "typing…",
            status = ChatHeaderStatus.Typing,
            avatarSeed = 2,
        )
    }
}

@Preview(name = "ChatHeader · Group (light)", showBackground = true, widthDp = 400)
@Preview(name = "ChatHeader · Group (dark)", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400)
@Composable
private fun ChatHeaderGroupPreview() {
    PreviewHost {
        ChatHeader(
            title = "Design Guild",
            statusText = "6 members",
            status = ChatHeaderStatus.Group,
            avatarSeed = 1,
            isGroup = true,
            memberNames = listOf("Aria", "Mira", "Ana", "Sam", "Nina"),
            otherUnread = 5,
        )
    }
}

@Preview(name = "MessageBubble · outgoing variants (light)", showBackground = true, widthDp = 400)
@Preview(name = "MessageBubble · outgoing variants (dark)", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400)
@Composable
private fun BubbleOutgoingPreview() {
    PreviewHost {
        Column(Modifier.padding(vertical = PulseSpacing.sm)) {
            MessageBubble(text("m1", "me", 30, "Sent message", MessageStatus.Sent))
            MessageBubble(text("m2", "me", 29, "Delivered message", MessageStatus.Delivered))
            MessageBubble(text("m3", "me", 28, "Read message with a much longer body so it wraps onto more than one line", MessageStatus.Read))
            MessageBubble(
                text("m4", "me", 27, "Link message https://pulse.example.com/docs", MessageStatus.Read),
            )
            MessageBubble(
                bubble("m5", "me", 26, MessageContent.Text("Edited"), edited = true),
            )
            MessageBubble(
                bubble("m6", "me", 25, MessageContent.Text("Deleted"), deleted = true),
            )
            MessageBubble(
                text("m7", "me", 24, "Failed message", MessageStatus.Failed),
                onRetry = {},
            )
            MessageBubble(
                bubble("m8", "me", 23, MessageContent.Image(listOf("sample://x.png"), caption = "Preview", widthPx = 1200, heightPx = 800)),
            )
        }
    }
}

@Preview(name = "MessageBubble · incoming run + system (light)", showBackground = true, widthDp = 400)
@Preview(name = "MessageBubble · incoming run + system (dark)", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400)
@Composable
private fun BubbleIncomingPreview() {
    PreviewHost {
        Column(Modifier.padding(vertical = PulseSpacing.sm)) {
            SystemMessageRow(text = "You created the group")
            // Aria run of 3 (first has name, last carries the avatar).
            MessageBubble(
                text("m1", "u-aria", 14, "The new palette landed"),
                isFirstInRun = true, isLastInRun = false,
                senderName = "Aria Sharma", avatarSeed = 1,
            )
            MessageBubble(
                text("m2", "u-aria", 13, "Check the dividers once"),
                isFirstInRun = false, isLastInRun = false,
                senderName = null, avatarSeed = 1,
            )
            MessageBubble(
                text("m3", "u-aria", 12, "I pushed the tokens"),
                isFirstInRun = false, isLastInRun = true,
                senderName = null, avatarSeed = 1,
            )
            // Single incoming bubble with voice placeholder.
            MessageBubble(
                bubble("m4", "u-aria", 11, MessageContent.Voice(21, List(16) { 20 + it * 5 })),
            )
            // Incoming photo placeholder with caption.
            MessageBubble(
                bubble("m5", "u-aria", 10, MessageContent.Image(listOf("sample://aria/art.png"), caption = "Sketch for the empty state", widthPx = 900, heightPx = 900)),
            )
        }
    }
}

@Preview(name = "Date & unread & typing rows (light)", showBackground = true, widthDp = 400)
@Preview(name = "Date & unread & typing rows (dark)", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400)
@Composable
private fun ChatRowsChromePreview() {
    PreviewHost {
        Column(Modifier.padding(vertical = PulseSpacing.sm)) {
            DateSeparator(label = "Today", modifier = Modifier.padding(vertical = PulseSpacing.xs))
            UnreadDivider(label = "3 unread messages", modifier = Modifier.padding(vertical = PulseSpacing.xs))
            TypingIndicator(modifier = Modifier.padding(vertical = PulseSpacing.xs))
            TypingIndicator(senderName = "Noah Khan", modifier = Modifier.padding(vertical = PulseSpacing.xs))
        }
    }
}

@Preview(name = "Composer (light)", showBackground = true, widthDp = 400)
@Preview(name = "Composer (dark)", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400)
@Composable
private fun ComposerPreview() {
    PreviewHost {
        MessageComposer(value = "", onValueChange = {})
        MessageComposer(value = "Typing a message…", onValueChange = {})
        MessageComposer(
            value = "A much longer draft line that should wrap across several lines inside the growing composer field, up to six lines maximum height for the demo",
            onValueChange = {},
        )
    }
}

@Preview(name = "Conversation screen (light)", showBackground = true, widthDp = 400, heightDp = 800)
@Preview(name = "Conversation screen (dark)", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400, heightDp = 800)
@Composable
private fun ConversationScreenPreview() {
    PulseTheme {
        val chat = Chat(
            id = "c-aria",
            kind = ChatKind.Direct,
            participantIds = listOf("me", "u-aria"),
            members = listOf(
                GroupMember("me", ChatRole.Owner),
                GroupMember("u-aria", ChatRole.Member),
            ),
            avatarSeed = 1,
        )
        val messages = listOf(
            text("a1", "u-aria", 3 * 24 * 60 + 20, "The new avatar palette landed - it looks so calm."),
            text("a2", "me", 3 * 24 * 60 + 19, "Right? I love the sage tone."),
            text("a3", "u-aria", 2 * 24 * 60, "Check the dark theme contrast on the dividers once."),
            text("a4", "me", 2 * 24 * 60 - 1, "Will do after the build."),
            text("a5", "u-aria", 26 * 60, "Mockup v3 is up."),
            bubble("a6", "u-aria", 25 * 60, MessageContent.Image(listOf("sample://x.png"), caption = "Mockup v3", widthPx = 1200, heightPx = 800)),
            text("a7", "u-aria", 24 * 60, "Send over the screenshot when you get a second."),
            text("a8", "me", 23 * 60, "On it - after the review."),
            text("a9", "u-aria", 8, "Morning! Are you free to review the bubble radii?"),
            text("a10", "u-aria", 7, "I pushed the spec link: https://pulse.example.com/spec/shapes"),
            text("a11", "u-aria", 6, "And the tokens file is in the design branch."),
        )
        val rows: List<ConversationRow> = buildConversationRows(
            messages = messages,
            users = previewUsers,
            unreadMarker = 3,
            typingIds = emptySet(),
        )
        val state = ConversationUiState(
            loading = false,
            chat = chat,
            rows = rows,
            users = previewUsers,
            otherUnread = 2,
            draftText = "",
        )
        Surface(color = PulseTheme.colors.background) {
            ConversationContent(state = state)
        }
    }
}
