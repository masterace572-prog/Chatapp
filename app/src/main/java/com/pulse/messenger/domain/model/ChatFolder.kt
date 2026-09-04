package com.pulse.messenger.domain.model

/**
 * A user-created chat folder (PRD S22). Folders filter the Chats list by
 * matching rules; every rule is an OR - a chat is included when it matches
 * any selected rule.
 */
data class ChatFolder(
    val id: String,
    val name: String,
    /** Matched against [ChatKind]: contains ChatKind.Group / ChatKind.Direct. */
    val includeKinds: Set<ChatKind>,
    /** Folders matching chats with unread messages only. */
    val onlyUnread: Boolean = false,
)

