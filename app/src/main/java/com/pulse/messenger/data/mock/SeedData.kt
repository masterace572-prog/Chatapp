package com.pulse.messenger.data.mock

import com.pulse.messenger.domain.model.Chat
import com.pulse.messenger.domain.model.ChatKind
import com.pulse.messenger.domain.model.GoogleAccount
import com.pulse.messenger.domain.model.Message
import com.pulse.messenger.domain.model.MessageStatus
import com.pulse.messenger.domain.model.MessageType
import com.pulse.messenger.domain.model.User

/**
 * Seed data foundation (PRD §5 + §9).
 *
 * M1 provides the canonical starter set that validates the domain model shape
 * and gives M3/M4 one file to grow:
 *   - M3 grows [contacts] to ~25 and [chats] to ~15 (1:1 + groups, archived,
 *     muted, pinned) and wires them into MockChatRepository;
 *   - M4 grows [messagesByChatId] to ~400 messages with media types, replies,
 *     reactions and system messages, plus auto-reply & typing behaviours.
 *
 * Timestamps use a fixed epoch so ordering is deterministic across launches.
 */
object SeedData {

    private const val BASE = 1_752_998_400_000L // 2026-07-01T00:00:00Z
    private val day: Long get() = 86_400_000L

    /* ---------- People ---------- */

    val contacts: List<User> = listOf(
        User("u-aria", "Aria", "Sharma", "aria.sh", "+91 98110 10001", "Designing calm apps.", 1),
        User("u-noah", "Noah", "Khan", "noahk", "+91 98110 10002", null, 2),
        User("u-mira", "Mira", "Patel", "mirap", "+91 98110 10003", "Tea > coffee. Mostly.", 3),
        User("u-dev", "Dev", "Singh", "devsingh", "+91 98110 10004", null, 4),
        User("u-zara", "Zara", "Ali", "zarali", "+91 98110 10005", "Currently reading 4 books.", 5),
        User("u-kabir", "Kabir", "Mehta", "kabir.m", "+91 98110 10006", null, 6),
        User("u-iva", "Iva", "Nair", "ivanair", "+91 98110 10007", "Runner.", 7),
        User("u-rao", "Reyansh", "Rao", "reyrao", "+91 98110 10008", null, 0),
        User("u-ana", "Ana", "D'Souza", "anads", "+91 98110 10009", "Photographer.", 2),
        User("u-eli", "Eli", "Kaur", "elik", "+91 98110 10010", null, 5),
    )

    /** "Me" once profile setup exists (S12-S15); M2 replaces the placeholder. */
    val me: User = User("me", "Aarav", "Kapoor", "aaravk", "+91 98110 90000", "Building Pulse.", 3)

    /**
     * Mock Google accounts shown by the S05 account-picker sheet.
     * The first email maps to an existing Pulse profile (routes straight to
     * Main); the second is new to Pulse (routes to profile setup S12-S17).
     */
    val googleAccounts: List<GoogleAccount> = listOf(
        GoogleAccount("g-existing", "aarav.kapoor@gmail.com", "Aarav Kapoor", 3),
        GoogleAccount("g-new", "jordan.lee@gmail.com", "Jordan Lee", 5),
    )

    /** S13 username suggestions derived from the chosen display name. */
    fun usernameSuggestions(firstName: String, lastName: String): List<String> {
        val f = firstName.lowercase().filter { it.isLetter() }
        val l = lastName.lowercase().filter { it.isLetter() }
        if (f.isEmpty()) return emptyList()
        val options = buildList {
            add(f)
            if (l.isNotEmpty()) add("$f.$l")
            if (l.isNotEmpty()) add("$f$l")
            add("$f.${f.length + 7}")
        }
        return options.distinct().take(3)
    }

    val usersById: Map<String, User> =
        (contacts + me).associateBy { it.id }

    /* ---------- Chats (summaries for S19; kind/title/participants/state) ---------- */

    val chats: List<Chat> = listOf(
        Chat("c-aria", ChatKind.Direct, participantIds = listOf("me", "u-aria"), updatedAtMillis = BASE + 0 * day),
        Chat("c-design", ChatKind.Group, title = "Design Guild", participantIds = listOf("me", "u-aria", "u-mira", "u-ana"), avatarSeed = 1, updatedAtMillis = BASE + 1 * day),
        Chat("c-noah", ChatKind.Direct, participantIds = listOf("me", "u-noah"), isPinned = true, updatedAtMillis = BASE + 2 * day),
        Chat("c-roadtrip", ChatKind.Group, title = "Roadtrip Crew", participantIds = listOf("me", "u-dev", "u-zara", "u-kabir", "u-rao"), avatarSeed = 2, isMuted = true, updatedAtMillis = BASE + 3 * day),
        Chat("c-zara", ChatKind.Direct, participantIds = listOf("me", "u-zara"), isArchived = true, updatedAtMillis = BASE + 4 * day),
        Chat("c-iva", ChatKind.Direct, participantIds = listOf("me", "u-iva"), isPinned = true, updatedAtMillis = BASE + 5 * day),
    )

    /* ---------- Messages (shape validation; M4 grows to full volume) ---------- */

    val messagesByChatId: Map<String, List<Message>> = mapOf(
        "c-aria" to listOf(
            Message("m-1", "c-aria", "me", type = MessageType.Text, text = "Hey Aria! Have you seen the new design tokens?", sentAtMillis = BASE + 0 * day + 3600_000, status = MessageStatus.Read, isOutgoing = true),
            Message("m-2", "c-aria", "u-aria", type = MessageType.Text, text = "Yes! The muted avatar palette is so good.", sentAtMillis = BASE + 0 * day + 3610_000, status = MessageStatus.Read, isOutgoing = false),
            Message("m-3", "c-aria", "u-aria", type = MessageType.Image, text = "Screenshot from the chat list:", sentAtMillis = BASE + 0 * day + 3620_000, status = MessageStatus.Read, isOutgoing = false),
        ),
        "c-noah" to listOf(
            Message("m-4", "c-noah", "me", type = MessageType.Text, text = "Sending the build over in a minute.", sentAtMillis = BASE + 2 * day + 7200_000, status = MessageStatus.Delivered, isOutgoing = true),
            Message("m-5", "c-noah", "u-noah", type = MessageType.File, text = "pulse-debug.apk", sentAtMillis = BASE + 2 * day + 7220_000, status = MessageStatus.Read, isOutgoing = false),
        ),
    )

    /** One-line previews for the M3 chat list can read the last message cheaply. */
    fun lastMessage(chatId: String): Message? =
        messagesByChatId[chatId]?.lastOrNull()
}
