package com.pulse.messenger.domain.model

/**
 * Aggregate row model for chat lists (S19/S21/S20): everything the list row
 * needs, resolved by the repository from Chat + Message + participant data.
 * The UI layer never joins chat/user data itself.
 */
data class ChatSummary(
    val chatId: String,
    val kind: ChatKind,
    /** Display title: peer display name for directs, group title otherwise. */
    val displayName: String,
    /** Stable seed for the avatar tone / initials of the row. */
    val avatarSeed: Int,
    /** Short first-name of the peer (direct chats) used as sender prefix in groups. */
    val peerFirstName: String = "",
    /** Member display names for the composite group avatar (first two used). */
    val memberNames: List<String> = emptyList(),
    val participantCount: Int = 0,
    val lastMessage: Message? = null,
    /** Sender's first name for the "sender prefix" line of groups. */
    val lastSenderFirstName: String? = null,
    val unreadCount: Int = 0,
    val isArchived: Boolean = false,
    val isMuted: Boolean = false,
    val isPinned: Boolean = false,
    /** True while the peer/group is "typing…" (mock simulation). */
    val isTyping: Boolean = false,
    /** Non-null when the user has an unsent draft in this chat. */
    val draft: String? = null,
    /** Members still present (used by group summaries) - informational. */
    val isVerified: Boolean = false,
) {
    val isGroup: Boolean get() = kind == ChatKind.Group
}
