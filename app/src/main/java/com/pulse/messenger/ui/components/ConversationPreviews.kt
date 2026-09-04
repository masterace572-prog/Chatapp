package com.pulse.messenger.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pulse.messenger.domain.model.Chat
import com.pulse.messenger.domain.model.ChatPermissions
import com.pulse.messenger.domain.model.ChatSummary
import com.pulse.messenger.domain.model.ChatKind
import com.pulse.messenger.domain.model.ChatRole
import com.pulse.messenger.domain.model.GroupMember
import com.pulse.messenger.domain.model.LinkPreview
import com.pulse.messenger.domain.model.Message
import com.pulse.messenger.domain.model.MessageContent
import com.pulse.messenger.domain.model.MessageReaction
import com.pulse.messenger.domain.model.MessageStatus
import com.pulse.messenger.domain.model.User
import com.pulse.messenger.R
import com.pulse.messenger.ui.icons.AppIcons
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
    linkPreview: LinkPreview? = null,
    upload: Float? = null,
    forwarded: Boolean = false,
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
    linkPreview = linkPreview,
    uploadProgress = upload,
    forwardedFromUserId = if (forwarded) "u-aria" else null,
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
        MessageComposer(state = ComposerUiState.Idle, value = "", onValueChange = {})
        MessageComposer(state = ComposerUiState.Typing, value = "Typing a message…", onValueChange = {})
        MessageComposer(
            state = ComposerUiState.Reply(
                messageId = "r1",
                senderName = "Aria Sharma",
                excerpt = "The new palette landed",
            ),
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

/* =====================================================================
 * M4b preview pairs (PRD §7: every new surface in light AND dark):
 * composer states, long-press actions, reactions, emoji sheet, pinned
 * banner, multi-select bar, forward sheet and conversation states.
 * ===================================================================== */

private fun m4bSummary(
    chatId: String,
    name: String,
    seed: Int,
    lastText: String,
    group: Boolean = false,
    unread: Int = 0,
    muted: Boolean = false,
): ChatSummary {
    val kind = if (group) ChatKind.Group else ChatKind.Direct
    return ChatSummary(
        chatId = chatId,
        kind = kind,
        displayName = name,
        avatarSeed = seed,
        peerFirstName = if (group) "" else name.substringBefore(' '),
        memberNames = if (group) listOf("Aria", "Mira", "Ana") else emptyList(),
        participantCount = if (group) 6 else 2,
        lastMessage = text("$chatId-last", if (group) "u-aria" else "me", 2, lastText),
        unreadCount = unread,
        isMuted = muted,
    )
}

@Preview(name = "Composer · reply & edit bars (light)", showBackground = true, widthDp = 400)
@Preview(name = "Composer · reply & edit bars (dark)", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400)
@Composable
private fun ComposerBarsPreview() {
    PreviewHost {
        Column {
            MessageComposer(
                state = ComposerUiState.Reply(
                    messageId = "r1",
                    senderName = "Aria Sharma",
                    excerpt = "Send over the screenshot when you get a second.",
                ),
                value = "On it - after the review.",
                onValueChange = {},
            )
            Spacer(Modifier.height(PulseSpacing.lg))
            MessageComposer(
                state = ComposerUiState.Edit(
                    messageId = "e1",
                    excerpt = "Check the dark theme contrast on the dividers once.",
                ),
                value = "Check the dark theme contrast on the dividers twice.",
                onValueChange = {},
            )
        }
    }
}

@Preview(name = "Composer · voice recording (light)", showBackground = true, widthDp = 400)
@Preview(name = "Composer · voice recording (dark)", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400)
@Composable
private fun ComposerRecordingPreview() {
    PreviewHost {
        Column {
            MessageComposer(state = ComposerUiState.Recording, value = "", onValueChange = {})
            Spacer(Modifier.height(PulseSpacing.lg))
            MessageComposer(state = ComposerUiState.LockedRecording, value = "", onValueChange = {})
        }
    }
}

@Preview(name = "Composer · blocked & read-only (light)", showBackground = true, widthDp = 400)
@Preview(name = "Composer · blocked & read-only (dark)", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400)
@Composable
private fun ComposerStatusPreview() {
    PreviewHost {
        Column {
            MessageComposer(state = ComposerUiState.Blocked, value = "", onValueChange = {})
            Spacer(Modifier.height(PulseSpacing.lg))
            MessageComposer(state = ComposerUiState.ReadOnly, value = "", onValueChange = {})
        }
    }
}

@Preview(name = "Quick reaction bar (light)", showBackground = true, widthDp = 400)
@Preview(name = "Quick reaction bar (dark)", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400)
@Composable
private fun QuickReactionBarPreview() {
    PreviewHost {
        Column {
            QuickReactionBar(onReact = {}, onMore = {})
            Spacer(Modifier.height(PulseSpacing.lg))
            // The same bar above an outgoing bubble for context.
            MessageBubble(
                bubble("qr1", "me", 5, MessageContent.Text("Could you move the build to noon?")),
            )
        }
    }
}

@Preview(name = "Message action sheet (light)", showBackground = true, widthDp = 400)
@Preview(name = "Message action sheet (dark)", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400)
@Composable
private fun MessageActionSheetPreview() {
    PreviewHost {
        MessageActionSheet(
            items = listOf(
                MessageActionItem(AppIcons.CornerUpLeft, R.string.conversation_action_reply),
                MessageActionItem(AppIcons.Share, R.string.conversation_action_forward),
                MessageActionItem(AppIcons.Copy, R.string.conversation_action_copy),
                MessageActionItem(AppIcons.Pin, R.string.conversation_action_pin),
                MessageActionItem(AppIcons.Star, R.string.conversation_action_star),
                MessageActionItem(AppIcons.Pencil, R.string.conversation_action_edit),
                MessageActionItem(AppIcons.Info, R.string.conversation_action_info),
                MessageActionItem(AppIcons.Trash, R.string.conversation_action_delete, destructive = true),
                MessageActionItem(AppIcons.Check, R.string.conversation_action_select),
            ),
        )
    }
}

@Preview(name = "Reaction pills & reactors (light)", showBackground = true, widthDp = 400)
@Preview(name = "Reaction pills & reactors (dark)", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400)
@Composable
private fun ReactionsPreview() {
    PreviewHost {
        Column {
            ReactionPillRow(
                pills = listOf(
                    ReactionPillUi("👍", 3, includesMe = true),
                    ReactionPillUi("❤️", 2, includesMe = false),
                    ReactionPillUi("🔥", 1, includesMe = true),
                ),
                isOutgoing = true,
            )
            Spacer(Modifier.height(PulseSpacing.sm))
            ReactionPillRow(
                pills = listOf(
                    ReactionPillUi("👏", 5, includesMe = false),
                    ReactionPillUi("😂", 2, includesMe = false),
                ),
                isOutgoing = false,
            )
            Spacer(Modifier.height(PulseSpacing.lg))
            ReactorsSheetContent(
                emoji = "👍",
                reactors = listOf(
                    ReactorUi("Aria Sharma", 1),
                    ReactorUi("Mira Patel", 3),
                    ReactorUi("Aarav Kapoor", 3),
                ),
            )
        }
    }
}

@Preview(name = "Emoji sheet (light)", showBackground = true, widthDp = 400)
@Preview(name = "Emoji sheet (dark)", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400)
@Composable
private fun EmojiSheetPreview() {
    PreviewHost {
        EmojiSheetContent(onEmoji = {})
    }
}

@Preview(name = "Pinned banner (light)", showBackground = true, widthDp = 400)
@Preview(name = "Pinned banner (dark)", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400)
@Composable
private fun PinnedBannerPreview() {
    val items = listOf(
        PinnedBannerData("p1", "The new palette landed - it looks so calm."),
        PinnedBannerData("p2", "Morning! Are you free to review the bubble radii?"),
        PinnedBannerData("p3", "Mockup v3 is up in the shared drive."),
    )
    PreviewHost {
        Column {
            PinnedBanner(items = items.take(2), displayIndex = 0)
            Spacer(Modifier.height(PulseSpacing.xs))
            PinnedBanner(items = items.take(2), displayIndex = 1)
            Spacer(Modifier.height(PulseSpacing.xs))
            PinnedBanner(items = items.take(1), displayIndex = 0)
        }
    }
}

@Preview(name = "Multi-select top bar (light)", showBackground = true, widthDp = 400)
@Preview(name = "Multi-select top bar (dark)", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400)
@Composable
private fun MultiSelectTopBarPreview() {
    PreviewHost {
        Column {
            MultiSelectTopBar(count = 3, canCopy = true)
            Spacer(Modifier.height(PulseSpacing.xs))
            MultiSelectTopBar(count = 2, canCopy = false)
        }
    }
}

@Preview(name = "Forward sheet (light)", showBackground = true, widthDp = 400, heightDp = 820)
@Preview(name = "Forward sheet (dark)", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400, heightDp = 820)
@Composable
private fun ForwardSheetPreview() {
    PulseTheme {
        Surface(
            color = PulseTheme.colors.background,
            modifier = Modifier
                .fillMaxWidth()
                .height(800.dp),
        ) {
            ForwardSheet(
                chats = listOf(
                    m4bSummary("c-noah", "Noah Khan", 2, "Send the debug apk when it is ready."),
                    m4bSummary("c-design", "Design Guild", 1, "Aria: new tokens pushed", group = true),
                    m4bSummary("c-kabir", "Kabir Rao", 6, "Catch you at the evening run?", unread = 4, muted = true),
                    m4bSummary("c-mira", "Mira Patel", 3, "Lunch tomorrow works."),
                    m4bSummary("c-roadtrip", "Roadtrip Crew", 4, "Dev: route finalised for Goa", group = true),
                ),
                messages = listOf(
                    text("fw1", "u-aria", 6, "Check the dark theme contrast on the dividers once."),
                    bubble(
                        "fw2",
                        "u-aria",
                        5,
                        MessageContent.Image(
                            listOf("sample://aria/mockup.png"),
                            caption = "Mockup v3",
                            widthPx = 1200,
                            heightPx = 800,
                        ),
                    ),
                ),
            )
        }
    }
}

@Preview(name = "Conversation · selection mode (light)", showBackground = true, widthDp = 400, heightDp = 800)
@Preview(name = "Conversation · selection mode (dark)", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400, heightDp = 800)
@Composable
private fun ConversationSelectionPreview() {
    val chat = Chat(
        id = "c-sel",
        kind = ChatKind.Direct,
        participantIds = listOf("me", "u-aria"),
        members = listOf(
            GroupMember("me", ChatRole.Owner),
            GroupMember("u-aria", ChatRole.Member),
        ),
        avatarSeed = 1,
    )
    val messages = listOf(
        text("s1", "u-aria", 5, "The new avatar palette landed - it looks so calm."),
        text("s2", "me", 4, "Right? I love the sage tone."),
        text("s3", "u-aria", 3, "Check the dark theme contrast on the dividers once."),
        text("s4", "me", 2, "Will do after the build."),
        text("s5", "u-aria", 1, "Mockup v3 is up."),
    )
    val rows = buildConversationRows(messages, previewUsers, unreadMarker = 0)
    val state = ConversationUiState(
        loading = false,
        chat = chat,
        rows = rows,
        users = previewUsers,
        selectionMode = true,
        selectedMessageIds = setOf("s2", "s5"),
    )
    ConversationContent(state = state)
}

@Preview(name = "Conversation · read-only group (light)", showBackground = true, widthDp = 400, heightDp = 800)
@Preview(name = "Conversation · read-only group (dark)", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400, heightDp = 800)
@Composable
private fun ConversationReadOnlyGroupPreview() {
    val chat = Chat(
        id = "c-ro",
        kind = ChatKind.Group,
        participantIds = listOf("me", "u-aria", "u-mira", "u-noah"),
        members = listOf(
            GroupMember("me", ChatRole.Member),
            GroupMember("u-aria", ChatRole.Admin),
            GroupMember("u-mira", ChatRole.Member),
            GroupMember("u-noah", ChatRole.Member),
        ),
        title = "Morning Runners",
        avatarSeed = 2,
        pinnedMessageIds = listOf("ro2"),
        permissions = ChatPermissions(sendMessages = false),
    )
    val messages = listOf(
        text("ro1", "u-aria", 30, "7am start at the lake tomorrow."),
        text("ro2", "u-mira", 25, "Shoes ready - route is 5k."),
        text("ro3", "u-noah", 10, "Count me in for the late train too."),
    )
    val rows = buildConversationRows(messages, previewUsers, unreadMarker = 0)
    val state = ConversationUiState(
        loading = false,
        chat = chat,
        rows = rows,
        users = previewUsers,
        composer = ComposerUiState.ReadOnly,
        selectableChats = emptyList(),
    )
    ConversationContent(state = state)
}

/* ---------- M4c: media & rich bubble pairs ---------- */

@Preview(name = "M4c · Image bubbles (light)", showBackground = true, widthDp = 400)
@Preview(name = "M4c · Image bubbles (dark)", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400)
@Composable
private fun M4cImageBubblesPreview() {
    PreviewHost {
        Column(Modifier.padding(vertical = PulseSpacing.sm)) {
            MessageBubble(
                bubble("m4c-i1", "me", 10, MessageContent.Image(listOf("sample://photos/photo_palette.jpg"), caption = "Captioned from me", widthPx = 1200, heightPx = 800)),
                onImageTap = {},
            )
            MessageBubble(
                bubble("m4c-i2", "u-aria", 9, MessageContent.Image(listOf("sample://photos/photo_tea.jpg"), widthPx = 900, heightPx = 1200)),
                onImageTap = {},
            )
            MessageBubble(
                bubble("m4c-i3", "u-aria", 8, MessageContent.Image(listOf("sample://photos/photo_city.jpg"), widthPx = 1200, heightPx = 900), status = MessageStatus.Sending, upload = 0.55f),
                onImageTap = {},
            )
        }
    }
}

@Preview(name = "M4c · Image grids 2/3/4+ (light)", showBackground = true, widthDp = 400)
@Preview(name = "M4c · Image grids 2/3/4+ (dark)", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400)
@Composable
private fun M4cImageGridsPreview() {
    PreviewHost {
        Column(Modifier.padding(vertical = PulseSpacing.sm)) {
            MessageBubble(
                bubble("m4c-g1", "me", 12, MessageContent.Image(listOf("sample://photos/photo_books.jpg", "sample://photos/photo_city.jpg"), widthPx = 1200, heightPx = 900)),
                onImageTap = {},
            )
            MessageBubble(
                bubble("m4c-g2", "u-mira", 11, MessageContent.Image(listOf("sample://photos/photo_books.jpg", "sample://photos/photo_city.jpg", "sample://photos/photo_route.jpg"), caption = "Three views", widthPx = 1200, heightPx = 900)),
                onImageTap = {},
            )
            MessageBubble(
                bubble("m4c-g3", "u-sam", 10, MessageContent.Image(listOf("sample://photos/photo_park.jpg", "sample://photos/photo_beach.jpg", "sample://photos/photo_hills.jpg", "sample://photos/photo_route.jpg", "sample://photos/photo_city.jpg", "sample://photos/photo_books.jpg"), widthPx = 1200, heightPx = 900)),
                onImageTap = {},
            )
        }
    }
}

@Preview(name = "M4c · Video bubbles (light)", showBackground = true, widthDp = 400)
@Preview(name = "M4c · Video bubbles (dark)", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400)
@Composable
private fun M4cVideoBubblesPreview() {
    PreviewHost {
        Column(Modifier.padding(vertical = PulseSpacing.sm)) {
            MessageBubble(
                bubble("m4c-v1", "me", 8, MessageContent.Video("sample://videos/sample_video_1.mp4", durationSeconds = 6, widthPx = 320, heightPx = 240)),
                onVideoTap = {},
            )
            MessageBubble(
                bubble("m4c-v2", "u-sam", 7, MessageContent.Video("sample://videos/sample_video_2.mp4", durationSeconds = 9, widthPx = 320, heightPx = 240, caption = "From the fort")),
                onVideoTap = {},
            )
            MessageBubble(
                bubble("m4c-v3", "me", 6, MessageContent.Video("sample://videos/sample_video_1.mp4", durationSeconds = 6, widthPx = 320, heightPx = 240), status = MessageStatus.Sending, upload = 0.3f),
                onVideoTap = {},
            )
        }
    }
}

@Preview(name = "M4c · File bubbles (light)", showBackground = true, widthDp = 400)
@Preview(name = "M4c · File bubbles (dark)", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400)
@Composable
private fun M4cFileBubblesPreview() {
    PreviewHost {
        Column(Modifier.padding(vertical = PulseSpacing.sm)) {
            MessageBubble(
                bubble("m4c-f1", "me", 9, MessageContent.File("pitch-deck-v4.pdf", 24_637, "application/pdf", uri = "sample://files/sample_doc_pdf.pdf")),
                onFileTap = {},
            )
            MessageBubble(
                bubble("m4c-f2", "u-lea", 8, MessageContent.File("component-matrix.docx", 32_472, "application/vnd.openxmlformats-officedocument.wordprocessingml.document", uri = "sample://files/sample_doc_docx.docx")),
                onFileTap = {},
            )
            MessageBubble(
                bubble("m4c-f3", "me", 7, MessageContent.File("pulse-debug-artifacts.zip", 32_499, "application/zip", uri = "sample://files/sample_archive.zip"), status = MessageStatus.Sending, upload = 0.72f),
                onFileTap = {},
            )
        }
    }
}

@Preview(name = "M4c · Location bubbles (light)", showBackground = true, widthDp = 400)
@Preview(name = "M4c · Location bubbles (dark)", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400)
@Composable
private fun M4cLocationBubblesPreview() {
    PreviewHost {
        Column(Modifier.padding(vertical = PulseSpacing.sm)) {
            MessageBubble(
                bubble("m4c-l1", "me", 6, MessageContent.Location(19.0760, 72.8777, "Bandra, Mumbai")),
            )
            MessageBubble(
                bubble("m4c-l2", "u-kabir", 5, MessageContent.Location(18.6046, 73.7600, "Lonavala ghat viewpoint", isLive = true, liveDurationSeconds = 3_600)),
            )
            MessageBubble(
                bubble("m4c-l3", "u-omar", 1, MessageContent.Location(15.2993, 74.0760, "Jetty seafood place, Goa", isLive = true, liveDurationSeconds = 28_800)),
            )
        }
    }
}

@Preview(name = "M4c · Contact bubbles (light)", showBackground = true, widthDp = 400)
@Preview(name = "M4c · Contact bubbles (dark)", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400)
@Composable
private fun M4cContactBubblesPreview() {
    PreviewHost {
        Column(Modifier.padding(vertical = PulseSpacing.sm)) {
            MessageBubble(bubble("m4c-c1", "me", 7, MessageContent.Contact("u-noah", "Noah Khan", "+91 98110 10002", "noahk")))
            MessageBubble(bubble("m4c-c2", "u-aria", 6, MessageContent.Contact("u-iva", "Iva Nair", "+91 98110 10007", "ivanair")))
        }
    }
}

@Preview(name = "M4c · Poll bubbles (light)", showBackground = true, widthDp = 400)
@Preview(name = "M4c · Poll bubbles (dark)", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400)
@Composable
private fun M4cPollBubblesPreview() {
    PreviewHost {
        Column(Modifier.padding(vertical = PulseSpacing.sm)) {
            MessageBubble(
                bubble("m4c-p1", "u-nina", 8, MessageContent.Poll(
                    question = "Saturday plan?",
                    options = listOf("Dinner at mine", "Board games", "Movie night", "Farmers' market"),
                    votes = mapOf(0 to listOf("me", "u-nina"), 1 to listOf("u-rohan"), 2 to listOf("u-ana", "u-mira")),
                )),
                onPollVote = {},
                onPollRetract = {},
            )
            MessageBubble(
                bubble("m4c-p2", "u-aria", 7, MessageContent.Poll(
                    question = "Which radius is the new sheet token?",
                    options = listOf("16dp", "20dp", "24dp", "28dp"),
                    votes = mapOf(1 to listOf("u-sam"), 2 to listOf("me", "u-aria")),
                    isAnonymous = true,
                    isQuiz = true,
                    correctOptionIndex = 2,
                )),
                onPollVote = {},
                onPollRetract = {},
            )
            MessageBubble(
                bubble("m4c-p3", "me", 6, MessageContent.Poll(
                    question = "Dinner tonight?",
                    options = listOf("Yes", "Order in"),
                    multipleAnswers = true,
                )),
                onPollVote = {},
                onPollRetract = {},
            )
        }
    }
}

@Preview(name = "M4c · Stickers (light)", showBackground = true, widthDp = 400)
@Preview(name = "M4c · Stickers (dark)", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400)
@Composable
private fun M4cStickerPreview() {
    PreviewHost {
        Column(Modifier.padding(vertical = PulseSpacing.sm)) {
            MessageBubble(bubble("m4c-s1", "me", 5, MessageContent.Sticker("sticker_pulse")))
            MessageBubble(bubble("m4c-s2", "u-pulse", 4, MessageContent.Sticker("sticker_sun")))
            MessageBubble(bubble("m4c-s3", "u-iva", 3, MessageContent.Sticker("sticker_run")))
        }
    }
}

@Preview(name = "M4c · Link preview cards (light)", showBackground = true, widthDp = 400)
@Preview(name = "M4c · Link preview cards (dark)", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400)
@Composable
private fun M4cLinkPreviewPreview() {
    PreviewHost {
        Column(Modifier.padding(vertical = PulseSpacing.sm)) {
            MessageBubble(
                bubble(
                    "m4c-x1", "me", 8, MessageContent.Text("Check the dark theme guide"),
                    linkPreview = LinkPreview("https://material.io/design/color/dark-theme.html", "Material Design dark theme", "Recommended color tokens and surface overlays for dark UIs."),
                ),
            )
            MessageBubble(
                bubble(
                    "m4c-x2", "u-aria", 7, MessageContent.Text("Spec is here"),
                    linkPreview = LinkPreview("https://pulse.example.com/spec/shapes", "Pulse design spec - Shapes", "Corner radii tokens for bubbles, sheets and inputs."),
                ),
            )
        }
    }
}
