package com.pulse.messenger.domain.model

/** Message delivery status (PRD §5 - mock progression: sending → sent → delivered → read). */
enum class MessageStatus { Sending, Sent, Delivered, Read, Failed }

/** The typed content a message can carry (S23). Grows with the message milestones. */
enum class MessageType { Text, Image, Video, Voice, File, Location, Contact, Poll, System }

/**
 * One message inside a chat. Phase 1 keeps a slim shape; media payloads,
 * replies, reactions and edit/forward markers arrive with the M4 milestone.
 */
data class Message(
    val id: String,
    val chatId: String,
    val senderId: String,
    val type: MessageType = MessageType.Text,
    val text: String = "",
    val sentAtMillis: Long,
    val status: MessageStatus = MessageStatus.Sent,
    val isOutgoing: Boolean,
)

/**
 * A conversation: direct or group (PRD §5). Surface used by the chat list;
 * messages arrive with M4.
 */
enum class ChatKind { Direct, Group }

data class Chat(
    val id: String,
    val kind: ChatKind,
    val title: String? = null,
    val participantIds: List<String> = emptyList(),
    val avatarSeed: Int = 0,
    val isArchived: Boolean = false,
    val isMuted: Boolean = false,
    val isPinned: Boolean = false,
    val updatedAtMillis: Long = 0L,
) {
    val isGroup: Boolean get() = kind == ChatKind.Group
}
