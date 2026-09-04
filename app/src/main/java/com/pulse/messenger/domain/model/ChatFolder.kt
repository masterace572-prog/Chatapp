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

/** Rule helpers used by folder editing UI and matching logic. */
object ChatFolderRules {
    /** Readable one-line summary of a folder's rules, e.g. "Groups · Unread". */
    fun describe(folder: ChatFolder): String = buildList {
        if (ChatKind.Group in folder.includeKinds) add("Groups")
        if (ChatKind.Direct in folder.includeKinds) add("Personal")
        if (folder.onlyUnread) add("Unread")
    }.joinToString(" · ").ifEmpty { "Empty folder" }
}
