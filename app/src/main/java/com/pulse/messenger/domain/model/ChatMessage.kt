package com.pulse.messenger.domain.model

/** Message delivery status (PRD §5 - mock progression: sending → sent → delivered → read). */
enum class MessageStatus { Sending, Sent, Delivered, Read, Failed }

/**
 * Discriminant of [MessageContent]. Kept as a plain enum so list rows, search
 * sections and previews can switch on it without pattern-matching payloads.
 * Order matches the PRD §6.5 message-type list (S23).
 */
enum class MessageType { Text, Image, Video, Voice, File, Location, Contact, Poll, Sticker, System }

/**
 * Message payload (model v2, M4a). A sealed hierarchy so every PRD content
 * type exists as a real data type with realistic fields today; only [Text]
 * and [System] render fully in M4a - the other kinds carry their metadata now
 * and gain real rendering in M4b/M4c (media bubbles) without a model change.
 */
sealed interface MessageContent {
    val type: MessageType

    /** Plain-text surface for previews/search (caption/text/name/question). */
    val text: String

    data class Text(override val text: String) : MessageContent {
        override val type: MessageType = MessageType.Text
    }

    data class System(override val text: String) : MessageContent {
        override val type: MessageType = MessageType.System
    }

    /** Single image or a grid batch (2-4 uris); optional caption. */
    data class Image(
        val uris: List<String>,
        val caption: String = "",
        val widthPx: Int = 0,
        val heightPx: Int = 0,
    ) : MessageContent {
        override val type: MessageType = MessageType.Image
        override val text: String get() = caption
    }

    data class Video(
        val uri: String,
        val durationSeconds: Int = 0,
        val widthPx: Int = 0,
        val heightPx: Int = 0,
        val caption: String = "",
    ) : MessageContent {
        override val type: MessageType = MessageType.Video
        override val text: String get() = caption
    }

    /** Voice note: duration + waveform bar samples (0..100) for M4b rendering. */
    data class Voice(
        val durationSeconds: Int,
        val waveformSamples: List<Int> = emptyList(),
    ) : MessageContent {
        override val type: MessageType = MessageType.Voice
        override val text: String = ""
    }

    data class File(
        val name: String,
        val sizeBytes: Long,
        val mimeType: String = "application/octet-stream",
        /** Source uri for opening/downloading the file (bundled sample or SAF pick). */
        val uri: String = "",
    ) : MessageContent {
        override val type: MessageType = MessageType.File
        override val text: String get() = name
    }

    data class Location(
        val latitude: Double,
        val longitude: Double,
        val address: String = "",
        /** Live-location share: true while the share window is running. */
        val isLive: Boolean = false,
        /** Seconds the live share runs for (null when [isLive] is false). */
        val liveDurationSeconds: Int? = null,
    ) : MessageContent {
        override val type: MessageType = MessageType.Location
        override val text: String get() = address
    }

    data class Contact(
        val userId: String,
        val displayName: String,
        val phone: String? = null,
        val username: String? = null,
    ) : MessageContent {
        override val type: MessageType = MessageType.Contact
        override val text: String get() = displayName
    }

    data class Poll(
        val question: String,
        val options: List<String>,
        /** optionIndex -> user ids that voted (data only; voting UI is M4e/S39). */
        val votes: Map<Int, List<String>> = emptyMap(),
        val multipleAnswers: Boolean = false,
        val isAnonymous: Boolean = false,
        val isQuiz: Boolean = false,
        /** Quiz mode: index of the correct option (revealed after voting). */
        val correctOptionIndex: Int? = null,
    ) : MessageContent {
        override val type: MessageType = MessageType.Poll
        override val text: String get() = question
    }

    data class Sticker(
        /** Asset key; bundled stickers arrive with the sticker panel milestone. */
        val assetKey: String,
    ) : MessageContent {
        override val type: MessageType = MessageType.Sticker
        override val text: String = ""
    }
}

/** One reaction on a message: emoji + the user ids who sent it (data only; UI in M4b). */
data class MessageReaction(
    val emoji: String,
    val userIds: List<String> = emptyList(),
)

/** Optional URL preview attached to a message (S23 link preview card; M4b). */
data class LinkPreview(
    val url: String,
    val title: String? = null,
    val description: String? = null,
)

/**
 * One message inside a chat (model v2, M4a). Every later M4 sub-milestone
 * builds on this shape - no further migration is planned before Phase 2.
 *
 * [type] and [text] are convenience projections of [content] kept so list
 * rows, search and previews can read messages uniformly.
 */
data class Message(
    val id: String,
    val chatId: String,
    val senderId: String,
    val content: MessageContent = MessageContent.Text(""),
    val sentAtMillis: Long,
    val status: MessageStatus = MessageStatus.Sent,
    val isOutgoing: Boolean,
    val replyToMessageId: String? = null,
    val forwardedFromUserId: String? = null,
    val isEdited: Boolean = false,
    val isDeleted: Boolean = false,
    val isStarred: Boolean = false,
    val isPinned: Boolean = false,
    val reactions: List<MessageReaction> = emptyList(),
    /** 0f..1f while the mock is uploading media; null once stored/delivered. */
    val uploadProgress: Float? = null,
    val linkPreview: LinkPreview? = null,
) {
    val type: MessageType get() = content.type
    val text: String get() = content.text
    val isSystem: Boolean get() = content is MessageContent.System
}

/**
 * Member role inside a group chat (PRD §6.5 S28/S30). Role-management UI is
 * M4d; the model carries roles from M4a so group rendering stays honest.
 */
enum class ChatRole { Owner, Admin, Member }

data class GroupMember(
    val userId: String,
    val role: ChatRole = ChatRole.Member,
    val joinedAtMillis: Long = 0L,
)

/**
 * Group permission switches (PRD S30). Data only in M4a; enforced when
 * group-admin flows land (M4d).
 */
data class ChatPermissions(
    val sendMessages: Boolean = true,
    val sendMedia: Boolean = true,
    val addMembers: Boolean = true,
    val pinMessages: Boolean = true,
    val editInfo: Boolean = true,
    val approveNewMembers: Boolean = false,
)

/**
 * A conversation: direct or group (PRD §5). Model v2 (M4a) adds group
 * membership/roles, group metadata, pinned messages, disappearing-message
 * timer, wallpaper and block state - all still surfaced as mocks.
 */
enum class ChatKind { Direct, Group }

data class Chat(
    val id: String,
    val kind: ChatKind,
    val title: String? = null,
    /** All member ids incl. "me" (kept from M3 for list math). */
    val participantIds: List<String> = emptyList(),
    /** Full membership with roles (groups; data only in M4a). */
    val members: List<GroupMember> = emptyList(),
    val description: String? = null,
    val createdBy: String? = null,
    val createdAtMillis: Long = 0L,
    val avatarSeed: Int = 0,
    val isArchived: Boolean = false,
    val isMuted: Boolean = false,
    val isPinned: Boolean = false,
    val updatedAtMillis: Long = 0L,
    /** Ids of messages pinned inside the conversation (S23 pinned banner, M4b). */
    val pinnedMessageIds: List<String> = emptyList(),
    /** Seconds; null = off (S38 disappearing messages). */
    val disappearingMessagesDurationSeconds: Int? = null,
    /** Per-chat wallpaper/accent override (S37). Null = default theme. */
    val wallpaperId: String? = null,
    /** True when this conversation is blocked (composer state replaces UI in M4b). */
    val isBlocked: Boolean = false,
    /** Group permissions (S30) - data only until M4d. */
    val permissions: ChatPermissions = ChatPermissions(),
) {
    val isGroup: Boolean get() = kind == ChatKind.Group

    /** Sorted member list for group screens; "me" first is NOT assumed. */
    fun member(userId: String): GroupMember? = members.firstOrNull { it.userId == userId }
}
