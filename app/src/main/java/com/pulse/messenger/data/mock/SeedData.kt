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
 * M3 volume: 16 contacts, 14 chats (direct + groups; pinned, muted, archived,
 * unread states), per-chat messages driving previews and search, drafts and a
 * typing simulation target. M4 grows messages towards ~400 with media payloads,
 * replies, reactions and system messages.
 *
 * Timestamps are anchored to [BaseMillis] so ordering is deterministic; the
 * mock repository offsets them relative to "now" at first use.
 */
object SeedData {

    /** Anchor for seed timestamps (2026-08-30T00:00:00Z); repo offsets to now. */
    const val BaseMillis = 1_787_529_600_000L
    private val day: Long get() = 86_400_000L
    private val hour: Long get() = 3_600_000L

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
        User("u-sam", "Sam", "Verma", "samv", "+91 98110 10011", "Mountain time.", 1),
        User("u-lea", "Lea", "Gomes", "leag", "+91 98110 10012", null, 4),
        User("u-rohan", "Rohan", "Bose", "rohanb", "+91 98110 10013", "Building things.", 6),
        User("u-nina", "Nina", "Kapoor", "ninak", "+91 98110 10014", "Plants > people.", 7),
        User("u-omar", "Omar", "Farooq", "omarf", "+91 98110 10015", null, 2),
        User("u-tara", "Tara", "Iyengar", "tarai", "+91 98110 10016", "Let's hike.", 3),
    )

    /** "Me" - filled by profile setup (M2). */
    val me: User = User("me", "Aarav", "Kapoor", "aaravk", "+91 98110 90000", "Building Pulse.", 3)

    val usersById: Map<String, User> = (contacts + me).associateBy { it.id }

    fun user(id: String): User = usersById[id] ?: User(id, id, username = id.lowercase())

    /* ---------- Google picker (S05) ---------- */

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

    /* ---------- Chats (S19 list; states across the matrix) ---------- */

    private fun chat(
        id: String,
        kind: ChatKind,
        participantIds: List<String>,
        title: String? = null,
        avatarSeed: Int = 0,
        archived: Boolean = false,
        muted: Boolean = false,
        pinned: Boolean = false,
        ageDays: Long,
    ) = Chat(
        id = id,
        kind = kind,
        title = title,
        participantIds = participantIds,
        avatarSeed = avatarSeed,
        isArchived = archived,
        isMuted = muted,
        isPinned = pinned,
        updatedAtMillis = BaseMillis + ageDays * day,
    )

    /** ids of chats which have a draft from me (order matters, by chat). */
    val drafts: Map<String, String> = mapOf(
        "c-noah" to "Send the debug apk when it is ready.",
    )

    /** id of the chat used for the typing simulation. */
    const val typingChatId = "c-aria"

    /** Contacts the user is "chatting with" even if not in the contacts list. */
    val chats: List<Chat> = listOf(
        // 1:1s
        chat("c-aria", ChatKind.Direct, listOf("me", "u-aria"), ageDays = 0, avatarSeed = 1),
        chat("c-noah", ChatKind.Direct, listOf("me", "u-noah"), pinned = true, ageDays = 0, avatarSeed = 2),
        chat("c-iva", ChatKind.Direct, listOf("me", "u-iva"), pinned = true, ageDays = 1, avatarSeed = 7),
        chat("c-dev", ChatKind.Direct, listOf("me", "u-dev"), ageDays = 1, avatarSeed = 4),
        chat("c-zara", ChatKind.Direct, listOf("me", "u-zara"), archived = true, ageDays = 3, avatarSeed = 5),
        chat("c-kabir", ChatKind.Direct, listOf("me", "u-kabir"), muted = true, ageDays = 2, avatarSeed = 6),
        chat("c-mira", ChatKind.Direct, listOf("me", "u-mira"), ageDays = 2, avatarSeed = 3),
        chat("c-sam", ChatKind.Direct, listOf("me", "u-sam"), ageDays = 0, avatarSeed = 1),
        chat("c-lea", ChatKind.Direct, listOf("me", "u-lea"), muted = true, ageDays = 1, avatarSeed = 4),
        chat("c-rohan", ChatKind.Direct, listOf("me", "u-rohan"), ageDays = 3, avatarSeed = 6),
        chat("c-tara", ChatKind.Direct, listOf("me", "u-tara"), archived = true, ageDays = 4, avatarSeed = 3),
        // Groups
        chat(
            "c-design", ChatKind.Group, listOf("me", "u-aria", "u-mira", "u-ana", "u-sam", "u-nina"),
            title = "Design Guild", ageDays = 0, avatarSeed = 1,
        ),
        chat(
            "c-roadtrip", ChatKind.Group, listOf("me", "u-dev", "u-zara", "u-kabir", "u-rao", "u-omar"),
            title = "Roadtrip Crew", muted = true, ageDays = 1, avatarSeed = 4,
        ),
        chat(
            "c-fam", ChatKind.Group, listOf("me", "u-nina", "u-rohan", "u-ana", "u-mira"),
            title = "Weekend Plans", ageDays = 2, avatarSeed = 5,
        ),
        chat(
            "c-run", ChatKind.Group, listOf("me", "u-iva", "u-tara", "u-sam"),
            title = "Morning Runners", ageDays = 0, avatarSeed = 2,
        ),
    )

    /* ---------- Messages (per chat; drives previews + S20 search) ---------- */

    private fun msg(
        id: String,
        chatId: String,
        sender: String,
        text: String,
        age: Long,
        type: MessageType = MessageType.Text,
        status: MessageStatus = MessageStatus.Read,
    ) = Message(
        id = id,
        chatId = chatId,
        senderId = sender,
        type = type,
        text = text,
        sentAtMillis = BaseMillis + age,
        status = status,
        isOutgoing = sender == "me",
    )

    val messagesByChatId: Map<String, List<Message>> = mapOf(
        "c-aria" to listOf(
            msg("m-101", "c-aria", "u-aria", "The new avatar palette landed - looks so calm.", age = 2 * hour),
            msg("m-102", "c-aria", "me", "Right? I love the sage tone.", age = 2 * hour + 3 * 60_000L, status = MessageStatus.Read),
            msg("m-103", "c-aria", "u-aria", "Check the dark theme contrast on the dividers once.", age = hour),
            msg("m-104", "c-aria", "u-aria", "https://material.io/design/color/dark-theme.html", age = hour, type = MessageType.Text),
        ),
        "c-noah" to listOf(
            msg("m-201", "c-noah", "me", "Build is green, uploading now.", age = 5 * hour, status = MessageStatus.Delivered),
            msg("m-202", "c-noah", "u-noah", "Perfect. Ping me when the artifact is up.", age = 5 * hour + 2 * 60_000L),
        ),
        "c-iva" to listOf(
            msg("m-301", "c-iva", "me", "Training at 6 tomorrow?", age = day + 2 * hour, status = MessageStatus.Read),
            msg("m-302", "c-iva", "u-iva", "6 works. Bringing the new shoes.", age = day + 2 * hour + 5 * 60_000L),
            msg("m-303", "c-iva", "u-iva", "track_tempo.mp3", age = day + 2 * hour + 6 * 60_000L, type = MessageType.File),
        ),
        "c-dev" to listOf(
            msg("m-401", "c-dev", "u-dev", "Found a bug in the sign-up flow - email chip overflow.", age = day + 4 * hour),
            msg("m-402", "c-dev", "me", "Nice catch. On it.", age = day + 4 * hour + 10 * 60_000L, status = MessageStatus.Read),
        ),
        "c-zara" to listOf(
            msg("m-501", "c-zara", "u-zara", "The book was incredible, you have to read it.", age = 3 * day),
            msg("m-502", "c-zara", "me", "Adding it to the list!", age = 3 * day + hour, status = MessageStatus.Read),
        ),
        "c-kabir" to listOf(
            msg("m-601", "c-kabir", "u-kabir", "Match on Sunday at 7?", age = 2 * day + hour),
        ),
        "c-mira" to listOf(
            msg("m-701", "c-mira", "me", "Tea tomorrow?", age = 2 * day + 3 * hour, status = MessageStatus.Read),
            msg("m-702", "c-mira", "u-mira", "Always. Same place, 5pm.", age = 2 * day + 3 * hour + 15 * 60_000L),
        ),
        "c-sam" to listOf(
            msg("m-801", "c-sam", "u-sam", "sunset_pano.jpg", age = 6 * hour, type = MessageType.Image),
            msg("m-802", "c-sam", "u-sam", "From the ridge this morning.", age = 6 * hour + 60_000L),
        ),
        "c-lea" to listOf(
            msg("m-901", "c-lea", "u-lea", "notes_on_launch.pdf", age = day + 6 * hour, type = MessageType.File),
            msg("m-902", "c-lea", "me", "Great doc, thanks!", age = day + 6 * hour + hour, status = MessageStatus.Read),
        ),
        "c-rohan" to listOf(
            msg("m-1001", "c-rohan", "u-rohan", "Voice note · 0:34", age = 3 * day + hour, type = MessageType.Voice),
        ),
        "c-tara" to listOf(
            msg("m-1101", "c-tara", "u-tara", "Trail run this weekend?", age = 4 * day),
        ),
        "c-design" to listOf(
            msg("m-1201", "c-design", "u-aria", "Shipping the chip row today - filters in place.", age = 3 * hour),
            msg("m-1202", "c-design", "u-mira", "Love the accent container on selected.", age = 3 * hour + 8 * 60_000L),
            msg("m-1203", "c-design", "me", "I will review the folder editor after lunch.", age = 2 * hour, status = MessageStatus.Read),
            msg("m-1204", "c-design", "u-ana", "wallpaper_concepts.png", age = hour, type = MessageType.Image),
        ),
        "c-roadtrip" to listOf(
            msg("m-1301", "c-roadtrip", "u-dev", "Route planned - 4 stops, one waterfall.", age = day + 8 * hour),
            msg("m-1302", "c-roadtrip", "u-zara", "Adding snacks duty roster.", age = day + 9 * hour),
        ),
        "c-fam" to listOf(
            msg("m-1401", "c-fam", "u-nina", "Dinner at my place Saturday?", age = 2 * day + 5 * hour),
            msg("m-1402", "c-fam", "me", "Count me in.", age = 2 * day + 5 * hour + 20 * 60_000L, status = MessageStatus.Read),
            msg("m-1403", "c-fam", "u-rohan", "Bringing the board games.", age = 2 * day + 6 * hour),
        ),
        "c-run" to listOf(
            msg("m-1501", "c-run", "u-iva", "5k easy pace tomorrow 6am.", age = hour + 30 * 60_000L),
            msg("m-1502", "c-run", "u-sam", "In.", age = hour + 40 * 60_000L),
        ),
    )

    /** Initial unread counts per chat (muted chats grey out the pill). */
    val unreadCounts: Map<String, Int> = mapOf(
        "c-aria" to 3,
        "c-design" to 2,
        "c-roadtrip" to 5,
        "c-sam" to 1,
        "c-run" to 2,
        "c-kabir" to 4,
    )

    /** Message classification for S20 sections. */
    fun isLink(text: String) = text.contains("http://") || text.contains("https://")
}
