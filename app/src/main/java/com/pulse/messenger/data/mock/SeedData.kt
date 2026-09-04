package com.pulse.messenger.data.mock

import com.pulse.messenger.domain.model.Chat
import com.pulse.messenger.domain.model.ChatKind
import com.pulse.messenger.domain.model.LinkPreview
import com.pulse.messenger.domain.model.ChatPermissions
import com.pulse.messenger.domain.model.ChatRole
import com.pulse.messenger.domain.model.GoogleAccount
import com.pulse.messenger.domain.model.GroupMember
import com.pulse.messenger.domain.model.Message
import com.pulse.messenger.domain.model.MessageContent
import com.pulse.messenger.domain.model.MessageReaction
import com.pulse.messenger.domain.model.MessageStatus
import com.pulse.messenger.domain.model.User
import kotlin.random.Random

/**
 * Seed data foundation (PRD §5 + §9).
 *
 * M4a volume: 17 contacts (16 people + Pulse Assistant), 16 chats
 * (12 direct + 4 groups; pinned, muted, archived, unread states), and a real
 * generated message history (~440 messages, 20-45 per chat) spanning several
 * days so conversation date separators show "Today / Yesterday / weekday /
 * full date". Histories include grouped sender runs, system events ("X joined",
 * "You created the group"), content-type placeholders (image/video/voice/file/
 * location/contact/poll/sticker carry payload data; only text/system render in
 * M4a), links, edited + deleted examples, reactions data and unread tails.
 *
 * M4b additions: pinned messages (every group carries at least one pin;
 * direct chats demo pin cycling), starred messages, reply quotes (including
 * replies to non-text content), richer reaction seeds, a read-only group
 * (permissions.sendMessages = false) and deep histories: five chats reach
 * back 7-12 days so weekday and full-date separators render while recent
 * chats keep Today/Yesterday activity.
 *
 * Generation is deterministic: a fixed seed per chat reproduces the same
 * conversation on every launch, while timestamps are anchored to the moment
 * the object first loads (recent chats end minutes before "now"; archived
 * chats end days ago). No future-dated messages are ever produced.
 */
object SeedData {

    private val MINUTE = 60_000L
    private val HOUR = 3_600_000L
    private val DAY = 86_400_000L

    /* ---------- People ---------- */

    val contacts: List<User> = listOf(
        User("u-aria", "Aria", "Sharma", "aria.sh", "+91 98110 10001", "Designing calm apps.", 1, isOnline = true, lastSeenAtMillis = 0L),
        User("u-noah", "Noah", "Khan", "noahk", "+91 98110 10002", null, 2, isOnline = false, lastSeenAtMillis = 0L),
        User("u-mira", "Mira", "Patel", "mirap", "+91 98110 10003", "Tea > coffee. Mostly.", 3, isOnline = false, lastSeenAtMillis = 0L),
        User("u-dev", "Dev", "Singh", "devsingh", "+91 98110 10004", null, 4, isOnline = false, lastSeenAtMillis = 0L),
        User("u-zara", "Zara", "Ali", "zarali", "+91 98110 10005", "Currently reading 4 books.", 5, isOnline = false, lastSeenAtMillis = 0L),
        User("u-kabir", "Kabir", "Mehta", "kabir.m", "+91 98110 10006", null, 6, isOnline = false, lastSeenAtMillis = 0L),
        User("u-iva", "Iva", "Nair", "ivanair", "+91 98110 10007", "Runner.", 7, isOnline = false, lastSeenAtMillis = 0L),
        User("u-rao", "Reyansh", "Rao", "reyrao", "+91 98110 10008", null, 0, isOnline = false, lastSeenAtMillis = 0L),
        User("u-ana", "Ana", "D'Souza", "anads", "+91 98110 10009", "Photographer.", 2, isOnline = false, lastSeenAtMillis = 0L),
        User("u-eli", "Eli", "Kaur", "elik", "+91 98110 10010", null, 5, isOnline = false, lastSeenAtMillis = 0L),
        User("u-sam", "Sam", "Verma", "samv", "+91 98110 10011", "Mountain time.", 1, isOnline = false, lastSeenAtMillis = 0L),
        User("u-lea", "Lea", "Gomes", "leag", "+91 98110 10012", null, 4, isOnline = false, lastSeenAtMillis = 0L),
        User("u-rohan", "Rohan", "Bose", "rohanb", "+91 98110 10013", "Building things.", 6, isOnline = false, lastSeenAtMillis = 0L),
        User("u-nina", "Nina", "Kapoor", "ninak", "+91 98110 10014", "Plants > people.", 7, isOnline = false, lastSeenAtMillis = 0L),
        User("u-omar", "Omar", "Farooq", "omarf", "+91 98110 10015", null, 2, isOnline = false, lastSeenAtMillis = 0L),
        User("u-tara", "Tara", "Iyengar", "tarai", "+91 98110 10016", "Let's hike.", 3, isOnline = false, lastSeenAtMillis = 0L),
        // Pulse Assistant (D2): deterministic auto-reply demo contact, always online.
        User("u-pulse", "Pulse", "Assistant", "pulse", null, "Your helper for demoing Pulse.", 8, isOnline = true, lastSeenAtMillis = 0L, isVerified = true),
    )

    /** "Me" - filled by profile setup (M2). */
    val me: User = User("me", "Aarav", "Kapoor", "aaravk", "+91 98110 90000", "Building Pulse.", 3, isOnline = false, lastSeenAtMillis = 0L)

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

    /* ---------- Chats (list + conversation header state) ---------- */

    /** ids of chats which have a draft from me (order matters, by chat). */
    val drafts: Map<String, String> = mapOf(
        "c-noah" to "Send the debug apk when it is ready.",
    )

    /** id of the chat used for the idle typing simulation. */
    const val typingChatId = "c-aria"

    /** Initial unread counts per chat (muted chats grey out the pill). */
    val unreadCounts: Map<String, Int> = mapOf(
        "c-aria" to 3,
        "c-design" to 2,
        "c-roadtrip" to 5,
        "c-sam" to 1,
        "c-run" to 2,
        "c-kabir" to 4,
    )

    private fun now() = System.currentTimeMillis()

    private fun groupMember(id: String, role: ChatRole, joinedAgoMillis: Long): GroupMember =
        GroupMember(userId = id, role = role, joinedAtMillis = now() - joinedAgoMillis)

    /** Chats, built with end-anchored metadata (times relative to load). */
    private val chatSpecs: List<Pair<Chat, Long /* endAgoMinutes */>> = listOf(
        // 1:1 chats (kept from M3; order = list sort baseline by recency).
        chat(
            "c-aria", ChatKind.Direct, listOf("me", "u-aria"),
            avatarSeed = 1, endAgoMin = 4,
            pinnedMessageIds = listOf("sc-aria-013", "sc-aria-034"),
        ),
        chat("c-noah", ChatKind.Direct, listOf("me", "u-noah"), avatarSeed = 2, endAgoMin = 75),
        chat("c-iva", ChatKind.Direct, listOf("me", "u-iva"), pinned = true, avatarSeed = 7, endAgoMin = 310),
        chat("c-dev", ChatKind.Direct, listOf("me", "u-dev"), avatarSeed = 4, endAgoMin = 95),
        chat("c-zara", ChatKind.Direct, listOf("me", "u-zara"), archived = true, avatarSeed = 5, endAgoMin = 3 * 24 * 60),
        chat("c-kabir", ChatKind.Direct, listOf("me", "u-kabir"), muted = true, avatarSeed = 6, endAgoMin = 40),
        chat("c-mira", ChatKind.Direct, listOf("me", "u-mira"), avatarSeed = 3, endAgoMin = 145),
        chat("c-sam", ChatKind.Direct, listOf("me", "u-sam"), avatarSeed = 1, endAgoMin = 14),
        chat("c-lea", ChatKind.Direct, listOf("me", "u-lea"), muted = true, avatarSeed = 4, endAgoMin = 190),
        chat("c-rohan", ChatKind.Direct, listOf("me", "u-rohan"), avatarSeed = 6, endAgoMin = 160),
        chat("c-tara", ChatKind.Direct, listOf("me", "u-tara"), archived = true, avatarSeed = 3, endAgoMin = 4 * 24 * 60),
        chat(
            "c-assistant", ChatKind.Direct, listOf("me", "u-pulse"),
            avatarSeed = 8, endAgoMin = 90,
        ),
        // Groups (roles + metadata from M4a model v2; owner/join events seeded).
        chat(
            "c-design", ChatKind.Group, listOf("me", "u-aria", "u-mira", "u-ana", "u-sam", "u-nina"),
            title = "Design Guild", avatarSeed = 1, endAgoMin = 8,
            description = "Weekly crits, tokens and UI reviews.",
            createdBy = "me",
            members = mapOf(
                "me" to ChatRole.Owner,
                "u-aria" to ChatRole.Admin,
                "u-mira" to ChatRole.Member,
                "u-ana" to ChatRole.Member,
                "u-sam" to ChatRole.Member,
                "u-nina" to ChatRole.Member,
            ),
            pinnedMessageIds = listOf("sc-design-025"),
        ),
        chat(
            "c-roadtrip", ChatKind.Group, listOf("me", "u-dev", "u-zara", "u-kabir", "u-rao", "u-omar"),
            title = "Roadtrip Crew", avatarSeed = 4, endAgoMin = 2,
            description = "Goa run - October 12-16.",
            createdBy = "u-dev",
            members = mapOf(
                "me" to ChatRole.Member,
                "u-dev" to ChatRole.Owner,
                "u-zara" to ChatRole.Admin,
                "u-kabir" to ChatRole.Member,
                "u-rao" to ChatRole.Member,
                "u-omar" to ChatRole.Member,
            ),
            pinnedMessageIds = listOf("sc-roadtrip-027"),
        ),
        chat(
            "c-fam", ChatKind.Group, listOf("me", "u-nina", "u-rohan", "u-ana", "u-mira"),
            title = "Weekend Plans", avatarSeed = 5, endAgoMin = 260,
            description = "Family weekend - ideas & polls.",
            createdBy = "me",
            members = mapOf(
                "me" to ChatRole.Owner,
                "u-nina" to ChatRole.Admin,
                "u-rohan" to ChatRole.Member,
                "u-ana" to ChatRole.Member,
                "u-mira" to ChatRole.Member,
            ),
            pinnedMessageIds = listOf("sc-fam-008"),
        ),
        chat(
            "c-run", ChatKind.Group, listOf("me", "u-iva", "u-tara", "u-sam"),
            title = "Morning Runners", avatarSeed = 2, endAgoMin = 25,
            description = "5k club, 7am start.",
            createdBy = "u-iva",
            members = mapOf(
                "me" to ChatRole.Member,
                "u-iva" to ChatRole.Owner,
                "u-tara" to ChatRole.Admin,
                "u-sam" to ChatRole.Member,
            ),
            pinnedMessageIds = listOf("sc-run-023"),
            // Read-only demo (M4b): only admins can send here (me is a Member).
            permissions = ChatPermissions(sendMessages = false),
        ),
    )

    val chats: List<Chat> = chatSpecs.map { (chat, _) -> chat }

    private fun chat(
        id: String,
        kind: ChatKind,
        participantIds: List<String>,
        title: String? = null,
        avatarSeed: Int = 0,
        archived: Boolean = false,
        muted: Boolean = false,
        pinned: Boolean = false,
        endAgoMin: Long,
        description: String? = null,
        createdBy: String? = null,
        members: Map<String, ChatRole> = emptyMap(),
        pinnedMessageIds: List<String> = emptyList(),
        permissions: ChatPermissions = ChatPermissions(),
    ) = Chat(
        id = id,
        kind = kind,
        title = title,
        participantIds = participantIds,
        avatarSeed = avatarSeed,
        isArchived = archived,
        isMuted = muted,
        isPinned = pinned,
        updatedAtMillis = now() - endAgoMin * MINUTE,
        description = description,
        createdBy = createdBy,
        createdAtMillis = now() - (endAgoMin + 3 * 24 * 60) * MINUTE,
        members = if (kind == ChatKind.Group) {
            participantIds.map { pid ->
                groupMember(pid, members[pid] ?: ChatRole.Member, joinedAgoMillis = endAgoMin * MINUTE + participantIds.indexOf(pid) * 5 * MINUTE)
            }
        } else {
            emptyList()
        },
        pinnedMessageIds = pinnedMessageIds,
        permissions = permissions,
    ) to endAgoMin

    /* ---------- Message history generator (M4a) ---------- */

    /** One generated message before timestamping. */
    private data class Draft(
        val senderId: String,
        val content: MessageContent,
        val status: MessageStatus = MessageStatus.Read,
        val replyToMessageId: String? = null,
        val isEdited: Boolean = false,
        val isDeleted: Boolean = false,
        val isStarred: Boolean = false,
        val reactions: List<MessageReaction> = emptyList(),
        val linkPreview: LinkPreview? = null,
    )

    /** Content override injected at an absolute index of a conversation. */
    private data class RichSpec(
        val atIndex: Int,
        val content: MessageContent,
        /** Actor id; defaults to the slot's sender. Used for system rows. */
        val actor: String? = null,
        val status: MessageStatus = MessageStatus.Read,
        val isEdited: Boolean = false,
        val isDeleted: Boolean = false,
        val isStarred: Boolean = false,
        val reactions: List<MessageReaction> = emptyList(),
        val replyToPrevious: Boolean = false,
        val linkPreview: LinkPreview? = null,
    )

    /** Wall-clock layout: minutes between messages + optional night breaks. */
    private fun gapMinutes(rng: Random, nightBias: Boolean): Long {
        val gap = rng.nextLong(3, 26)
        return if (nightBias) gap + rng.nextLong(540, 660) else gap
    }

    private fun messagePool(chatId: String): List<String> = when (chatId) {
        "c-aria" -> listOf(
            "The new avatar palette landed - it looks so calm.",
            "Check the dark theme contrast on the dividers once.",
            "I pushed the spacing tokens to the design branch.",
            "Could you review the empty states before the demo?",
            "The muted tones read much better on the list now.",
            "I tweaked the hero illustration to feel less boxy.",
            "Left you a note in the mockup file.",
            "Fonts render crisply at 11sp in the previews.",
            "Should the typing indicator sit inside the header?",
            "Yes - and keep the pill on the surface colour.",
            "The sheet drag handle is too heavy, I lightened it.",
            "Send over the screenshot when you get a second.",
        )
        "c-noah" -> listOf(
            "Build is green, uploading the artifact now.",
            "The APK is ~12 MB this time - much leaner.",
            "Crash on the OTP screen is fixed on the new build.",
            "Can you test the session restore on your device?",
            "I added the missing strings for the folders screen.",
            "Release notes are drafted for this batch.",
            "The signing key change is in the README.",
            "Retest the swipe actions after the latest change.",
            "Dark theme contrast passes on the bubble preview.",
            "CI picked up the new workflow config fine.",
            "Cache was stale - clean build fixed the warnings.",
            "Yes, ship it once the artifact is verified.",
        )
        "c-iva" -> listOf(
            "Morning run at 6:45 - you in?",
            "My pace finally dropped under 5:40.",
            "The trail near the lake is perfect this week.",
            "New shoes arrived, breaking them in slowly.",
            "Stretching made a huge difference for my knees.",
            "Signing up for the 10k in November?",
            "Rest day today, legs feel heavy.",
            "Saw you logged 8k yesterday - nice pace.",
            "I'll join the 7am run on Saturday.",
            "Hydration and early dinner worked wonders.",
        )
        "c-dev" -> listOf(
            "Merged the repo changes into main.",
            "The build cache is behaving again.",
            "Tried the new previews - super quick feedback loop.",
            "Do you want the logs before or after the fix?",
            "That edge case only fails on API 34.",
            "I can pair on it after lunch.",
            "The emulator snapshot finally works.",
            "Unit tests are passing on CI again.",
            "Let's keep the module split as-is for now.",
            "The gradle cache cleanup saved three minutes.",
        )
        "c-zara" -> listOf(
            "Finished the third book this month.",
            "Sending you the reading list I promised.",
            "The cafe by the library has the best chai.",
            "Marked two chapters - you'd like the second one.",
            "Rainy day reading session was perfect.",
            "Found a first edition in the flea market!",
            "Save me a copy when you're done.",
            "That plot twist - I did not see it coming.",
            "Book club moved to Thursday.",
            "The bookmark you gave me is still going strong.",
        )
        "c-kabir" -> listOf(
            "Cricket match this Sunday, same ground?",
            "I'm opening the batting this time.",
            "Bring the extra stumps - ours cracked.",
            "Rain forecast for Sunday evening though.",
            "We can shift it to Saturday morning.",
            "My cover drive is finally working again.",
            "Who's bringing the snacks?",
            "The last over was chaos, loved it.",
            "Coach said I should bat at three.",
            "Same time, same place - don't be late.",
        )
        "c-mira" -> listOf(
            "Tried that new tea place near the park.",
            "Their jasmine green tea is unreal.",
            "I'm halfway through the book you lent me.",
            "The garden nursery has new succulents.",
            "Made the pasta recipe - it worked!",
            "Photos from the pottery class are in the album.",
            "Coffee was a mistake at 9pm, who does that.",
            "Found a quiet corner for our catch-up.",
            "The playlist you shared is on repeat.",
            "Let's do the farmer's market on Sunday.",
        )
        "c-sam" -> listOf(
            "The trek photos are finally edited.",
            "Sunrise from the ridge was worth every step.",
            "Snowline at 4,200m this weekend.",
            "My knees recovered just in time.",
            "Camping gear checklist is ready if you need it.",
            "The stream crossing was icy but fun.",
            "Next trip: the valley route, three days.",
            "Windproof layer is a must above 3,000m.",
            "Sent the route map to your email.",
            "Altitude sickness hit me at camp two - lesson learned.",
        )
        "c-lea" -> listOf(
            "The startup pitch deck is looking sharp.",
            "Investors loved the demo yesterday.",
            "Can you review the onboarding copy?",
            "We are down to the last two candidates.",
            "The analytics dashboard shipped on time.",
            "Churn dipped after the new activation flow.",
            "Funding round closes next week - fingers crossed.",
            "The press kit needs one more pass.",
            "Beta users are loving the dark theme.",
            "Meeting moved to 4pm, notes are in the doc.",
        )
        "c-rohan" -> listOf(
            "The bookshelf build is done - it looks great.",
            "Router finally set up, whole flat has wifi.",
            "Tried that new burger place, 8/10.",
            "The plant shelf brackets arrived.",
            "Painted the accent wall over the weekend.",
            "You have to see the sunset from the balcony.",
            "My keyboard build sounds incredible now.",
            "Sunday brunch place is booked for noon.",
            "The flat files needed a second coat.",
            "Meal prep worked - saved so much time.",
        )
        "c-tara" -> listOf(
            "Trail map for the weekend hike is ready.",
            "The viewpoint at the top is unreal.",
            "Packed the gear - tent, stove, extra socks.",
            "Rain chances are low on Saturday.",
            "I marked the water refill points on the map.",
            "That ridge descent was steep but worth it.",
            "Campfire dinner is a must, I'll bring the grill.",
            "Reach the trailhead before 7 to beat the crowd.",
            "My boots are broken in finally.",
            "Same meeting point as last time?",
        )
        "c-assistant" -> listOf(
            "Welcome! I can help you explore Pulse.",
            "Try opening a chat and sending a message - I reply instantly.",
            "Swipe a conversation to archive or pin it.",
            "Use the search tab to find chats, people and messages.",
            "Folders let you group chats - try the editor.",
            "The unread badge on back takes you home when you're done.",
            "Long-press a chat for multi-select actions.",
            "Your theme and accent colour live in Settings.",
            "Type a link in any chat - previews are on the way.",
            "Auto-replies happen in about 40% of chats. I am the other 60%.",
            "Everything you see here is seeded mock data.",
            "Send 'help' anytime and I'll point you around.",
        )
        "c-design" -> listOf(
            "The new spacing scale covers the empty states.",
            "I moved the dividers to the token file.",
            "Can we revisit the bubble corner radii?",
            "The 18dp bubble reads softer than the old one.",
            "Avatar tones look balanced in both themes.",
            "Updated the component matrix for the review.",
            "The sheet handles match the PRD now.",
            "Search highlight colour needs one more pass.",
            "Icons are consistent at 20/24/28.",
            "The chip focus ring is visible in dark mode.",
            "Keyboard padding is handled on the composer.",
            "Let's lock the typography scale today.",
            "The empty state illustration works without gradients.",
            "Previews look sharp on the small device too.",
        )
        "c-roadtrip" -> listOf(
            "Stop one: the lakeside cafe near Lonavala.",
            "Fuel up at the highway plaza before the ghats.",
            "Who's driving the first leg?",
            "I vote Dev - he knows the twisty bits.",
            "Booking the beach house for two nights.",
            "The seafood place on the jetty is a must.",
            "Rain cover for the bikes, just in case.",
            "Route map is in the chat - check the link.",
            "Sunset point is 20 minutes off the highway.",
            "Everyone chips in for the fuel kitty.",
            "The homestay owner sent the menu - looks great.",
            "Leave by 6am to skip the weekend traffic.",
            "Power banks, chargers, aux cable - sorted.",
            "Last stop before Goa: the old fort viewpoint.",
        )
        "c-fam" -> listOf(
            "Weekend plans - vote in the poll!",
            "I can host dinner on Saturday.",
            "The new board game arrived, it's brilliant.",
            "Mom's biryani is happening on Sunday.",
            "Who is bringing the projector for movie night?",
            "The park has a new walking track.",
            "Sunday brunch at the usual place?",
            "Nina found a farm stay for next month.",
            "Card games after dinner, always.",
            "Someone bring the good chai, please.",
            "Photos from last weekend are up.",
            "The rooftop cafe has a great view for sunset.",
        )
        "c-run" -> listOf(
            "5k at 7am - weather is perfect.",
            "New route along the river is flatter.",
            "I'm doing intervals today, join if you want.",
            "Pace group split at the bridge as usual.",
            "Water stop at the fountain, bring your bottle.",
            "Evening run today instead - it's cooler.",
            "Strava segment record fell this morning!",
            "Shoes update: the new pair feels great.",
            "Hill repeats on Thursday, who's in?",
            "Easy recovery run, 6k at conversational pace.",
            "The sunrise run crew is growing.",
            "Don't forget the stretch circle after.",
        )
        else -> listOf(
            "Hey, quick check - all good?",
            "Sounds good to me.",
            "Let's sync later today.",
            "Got it, thanks!",
            "On it.",
            "Can we catch up over coffee?",
            "I'll send the details in a bit.",
            "Perfect, see you then.",
        )
    }

    private fun wave(samples: Int, seed: Int): List<Int> {
        val rng = Random(seed)
        return List(samples) { rng.nextInt(12, 96) }
    }

    /** Rich payload slots per chat (media/voice/file/location/contact/poll/sticker/system). */
    private fun richSpecs(chatId: String, count: Int): List<RichSpec> = when (chatId) {
        "c-aria" -> listOf(
            RichSpec(6, MessageContent.Image(listOf("sample://photos/photo_palette.jpg"), caption = "New palette preview", widthPx = 1200, heightPx = 800)),
            RichSpec(12, MessageContent.Voice(15, wave(24, 101)), reactions = listOf(MessageReaction("❤️", listOf("u-aria", "me")))),
            RichSpec(13, MessageContent.Text("Love this voice note - keeping it for the review."), replyToPrevious = true),
            RichSpec(19, MessageContent.Text("https://material.io/design/color/dark-theme.html"),
                linkPreview = LinkPreview("https://material.io/design/color/dark-theme.html", "Material Design dark theme", "Recommended color tokens and surface overlays for dark UIs.")),
            RichSpec(24, MessageContent.Image(listOf("sample://photos/photo_city.jpg"), widthPx = 1080, heightPx = 1080)),
            RichSpec(28, MessageContent.Contact("u-noah", "Noah Khan", "+91 98110 10002", "noahk")),
            RichSpec(33, MessageContent.Text("Check the divider contrast in dark mode."), isEdited = true, isStarred = true),
            RichSpec(35, MessageContent.Text("This thread keeps a deleted note below.")),
            RichSpec(36, MessageContent.Text("Old note"), isDeleted = true),
        )
        "c-noah" -> listOf(
            RichSpec(4, MessageContent.Text("https://github.com/pulse-app/releases/releases/tag/debug-0.1.0"),
                linkPreview = LinkPreview("https://github.com/pulse-app/releases/releases/tag/debug-0.1.0", "Pulse releases - debug 0.1.0", "Latest debug build notes and download link.")),
            RichSpec(15, MessageContent.File("pulse-debug-artifacts.zip", 32_499, "application/zip", uri = "sample://files/sample_archive.zip")),
            RichSpec(21, MessageContent.Text("APK is in the artifact, grab it."), replyToPrevious = true, isStarred = true),
            RichSpec(26, MessageContent.Text("Fixing the icon overlap now."), isEdited = true),
        )
        "c-iva" -> listOf(
            RichSpec(8, MessageContent.Text("https://example.com/runs/route-5k-river")),
            RichSpec(17, MessageContent.Voice(15, wave(16, 202)), reactions = listOf(MessageReaction("🔥", listOf("me")))),
            RichSpec(21, MessageContent.Contact("u-tara", "Tara Iyengar", "+91 98110 10016", "tarai")),
        )
        "c-dev" -> listOf(
            RichSpec(6, MessageContent.Text("https://developer.android.com/studio/build/dependencies")),
            RichSpec(14, MessageContent.Voice(28, wave(22, 303))),
        )
        "c-kabir" -> listOf(
            RichSpec(10, MessageContent.Text("https://example.com/grounds/hill-park-cricket")),
            RichSpec(20, MessageContent.Image(listOf("sample://photos/photo_park.jpg"), caption = "The ground this Sunday", widthPx = 1600, heightPx = 900)),
        )
        "c-mira" -> listOf(
            RichSpec(7, MessageContent.Image(listOf("sample://photos/photo_tea.jpg"), caption = "The jasmine green tea", widthPx = 900, heightPx = 1200)),
            RichSpec(13, MessageContent.Image(listOf("sample://photos/photo_books.jpg", "sample://photos/photo_city.jpg"), caption = "New reads + the city corner", widthPx = 1200, heightPx = 900)),
        )
        "c-sam" -> listOf(
            RichSpec(9, MessageContent.Image(listOf("sample://photos/photo_hills.jpg"), caption = "Sunrise from the ridge", widthPx = 1600, heightPx = 1066)),
            RichSpec(18, MessageContent.Video("sample://videos/sample_video_1.mp4", durationSeconds = 6, widthPx = 320, heightPx = 240)),
        )
        "c-lea" -> listOf(
            RichSpec(12, MessageContent.File("pitch-deck-v4.pdf", 24_637, "application/pdf", uri = "sample://files/sample_doc_pdf.pdf"), reactions = listOf(MessageReaction("👏", listOf("me", "u-lea")))),
            RichSpec(22, MessageContent.Location(19.0760, 72.8777, "Bandra, Mumbai")),
        )
        "c-rohan" -> listOf(
            RichSpec(5, MessageContent.Image(listOf("sample://photos/photo_books.jpg"), caption = "Bookshelf build done", widthPx = 1200, heightPx = 1600)),
            RichSpec(16, MessageContent.Voice(7, wave(12, 404))),
        )
        "c-assistant" -> listOf(
            RichSpec(3, MessageContent.Text("Psst - this chat always replies. Try sending anything."), isStarred = true, reactions = listOf(MessageReaction("👋", listOf("u-pulse", "me")))),
            RichSpec(11, MessageContent.Text("Here is a link to try: https://pulse.example.com/guide")),
            RichSpec(18, MessageContent.Sticker("sticker_sun")),
        )
        "c-design" -> listOf(
            RichSpec(0, MessageContent.System("You created the group"), actor = "me"),
            RichSpec(1, MessageContent.System("Aria joined"), actor = "u-aria"),
            RichSpec(2, MessageContent.System("Mira joined"), actor = "u-mira"),
            RichSpec(3, MessageContent.System("Ana joined"), actor = "u-ana"),
            RichSpec(4, MessageContent.System("Sam joined"), actor = "u-sam"),
            RichSpec(5, MessageContent.System("Nina joined"), actor = "u-nina"),
            RichSpec(9, MessageContent.Image(listOf("sample://photos/photo_palette.jpg"), caption = "Token sheet v3", widthPx = 1400, heightPx = 900)),
            RichSpec(16, MessageContent.Voice(28, wave(20, 505))),
            RichSpec(21, MessageContent.Poll(
                question = "Which radius is the new sheet token?",
                options = listOf("16dp", "20dp", "24dp", "28dp"),
                votes = mapOf(1 to listOf("u-sam"), 2 to listOf("me", "u-aria")),
                multipleAnswers = false,
                isAnonymous = true,
                isQuiz = true,
                correctOptionIndex = 2,
            )),
            RichSpec(24, MessageContent.File("component-matrix.docx", 32_472, "application/vnd.openxmlformats-officedocument.wordprocessingml.document", uri = "sample://files/sample_doc_docx.docx")),
            RichSpec(25, MessageContent.Text("Thanks for the matrix!"), replyToPrevious = true),
            RichSpec(31, MessageContent.Text("See the radii in the spec: https://pulse.example.com/spec/shapes"),
                linkPreview = LinkPreview("https://pulse.example.com/spec/shapes", "Pulse design spec - Shapes", "Corner radii tokens for bubbles, sheets and inputs.")),
            RichSpec(38, MessageContent.Text("Bubble radii updated"), isEdited = true),
            RichSpec(40, MessageContent.Text("Closing this thread for the demo"), reactions = listOf(MessageReaction("👍", listOf("u-aria", "u-sam", "me")))),
        )
        "c-roadtrip" -> listOf(
            RichSpec(0, MessageContent.System("Dev created the group"), actor = "u-dev"),
            RichSpec(1, MessageContent.System("You joined"), actor = "me"),
            RichSpec(2, MessageContent.System("Zara joined"), actor = "u-zara"),
            RichSpec(3, MessageContent.System("Kabir joined"), actor = "u-kabir"),
            RichSpec(4, MessageContent.System("Reyansh joined"), actor = "u-rao"),
            RichSpec(5, MessageContent.System("Omar joined"), actor = "u-omar"),
            RichSpec(10, MessageContent.Text("https://example.com/roadtrip/route-map-goa")),
            RichSpec(17, MessageContent.Location(18.6046, 73.7600, "Lonavala ghat viewpoint", isLive = true, liveDurationSeconds = 3_600)),
            RichSpec(20, MessageContent.Sticker("sticker_pulse")),
            RichSpec(26, MessageContent.Image(listOf("sample://photos/photo_beach.jpg"), caption = "The beach house", widthPx = 1600, heightPx = 900)),
            RichSpec(31, MessageContent.Image(listOf("sample://photos/photo_route.jpg", "sample://photos/photo_city.jpg", "sample://photos/photo_hills.jpg"), caption = "Views from the drive", widthPx = 1200, heightPx = 900)),
            RichSpec(34, MessageContent.Voice(28, wave(18, 606)), reactions = listOf(MessageReaction("👍", listOf("u-dev", "u-kabir")))),
            RichSpec(36, MessageContent.Text("Count me in for the late train too."), reactions = listOf(MessageReaction("😮", listOf("u-omar")), MessageReaction("🔥", listOf("me")))),
            RichSpec(37, MessageContent.Video("sample://videos/sample_video_2.mp4", durationSeconds = 9, widthPx = 320, heightPx = 240, caption = "The fort viewpoint")),
            RichSpec(39, MessageContent.Location(15.2993, 74.0760, "Jetty seafood place, Goa", isLive = true, liveDurationSeconds = 28_800)),
        )
        "c-fam" -> listOf(
            RichSpec(0, MessageContent.System("You created the group"), actor = "me"),
            RichSpec(1, MessageContent.System("Nina joined"), actor = "u-nina"),
            RichSpec(2, MessageContent.System("Rohan joined"), actor = "u-rohan"),
            RichSpec(3, MessageContent.System("Ana joined"), actor = "u-ana"),
            RichSpec(4, MessageContent.System("Mira joined")),
            RichSpec(7, MessageContent.Poll(
                question = "Saturday plan?",
                options = listOf("Dinner at mine", "Board games", "Movie night", "Farmers' market"),
                votes = mapOf(0 to listOf("me", "u-nina"), 1 to listOf("u-rohan"), 2 to listOf("u-ana", "u-mira")),
                multipleAnswers = false,
            )),
            RichSpec(19, MessageContent.Image(listOf("sample://photos/photo_park.jpg", "sample://photos/photo_beach.jpg", "sample://photos/photo_hills.jpg", "sample://photos/photo_route.jpg"), caption = "Weekend photos", widthPx = 1200, heightPx = 900)),
            RichSpec(28, MessageContent.Text("Poll is live for next month too."), isStarred = true),
        )
        "c-run" -> listOf(
            RichSpec(0, MessageContent.System("Iva created the group"), actor = "u-iva"),
            RichSpec(1, MessageContent.System("You joined"), actor = "me"),
            RichSpec(2, MessageContent.System("Tara joined"), actor = "u-tara"),
            RichSpec(3, MessageContent.System("Sam joined"), actor = "u-sam"),
            RichSpec(8, MessageContent.Text("https://example.com/runs/segment-river-5k")),
            RichSpec(14, MessageContent.Sticker("sticker_run")),
            RichSpec(21, MessageContent.Text("Route exported to the file below.")),
            RichSpec(22, MessageContent.File("river-route.gpx", 18_240, "application/gpx+xml"), replyToPrevious = true),
        )
        else -> emptyList()
    }

    /**
     * Chats whose history reaches back 7-12 days (weekday + full-date pills)
     * while still ending recently, so Today/Yesterday stay visible too.
     */
    private val deepHistoryDays: Map<String, Int> = mapOf(
        "c-noah" to 7,
        "c-mira" to 8,
        "c-fam" to 9,
        "c-rohan" to 10,
        "c-lea" to 12,
    )

    /** Builds one chat's history (ascending). Deterministic per chat id. */
    private fun buildConversation(chat: Chat, endAgoMinutes: Long, count: Int): List<Message> {
        val rng = Random(chat.id.hashCode())
        val peers = chat.participantIds.filter { it != "me" }
        val pool = messagePool(chat.id)

        // Wall-clock: histories span >= 2 days by default (several nights);
        // deep-history chats add multi-day pauses so weekday and full-date
        // separators render while recent chats keep Today/Yesterday activity.
        val endMillis = now() - endAgoMinutes * MINUTE
        val targetSpanMillis = (deepHistoryDays[chat.id]?.times(DAY)) ?: (2 * DAY + 7 * HOUR)
        val nightGapMillis = rng.nextLong(9, 12) * HOUR
        val nights = (targetSpanMillis / nightGapMillis).toInt().coerceIn(3, 5)
        val nightAfter = (1..nights).map { (it * count) / (nights + 1) }.filter { it < count - 1 }.toSet()
        val gapMinutesList = (0 until count).map { i ->
            if (i in nightAfter) gapMinutes(rng, nightBias = true) else gapMinutes(rng, nightBias = false)
        }.toMutableList()
        val baseTotalMillis = gapMinutesList.sum() * MINUTE
        if (targetSpanMillis > baseTotalMillis) {
            // Spread 24-40h pauses across the conversation until the span is met.
            val extraHours = (targetSpanMillis - baseTotalMillis) / HOUR
            val pauses = (extraHours / 32).toInt().coerceIn(1, count / 4)
            val step = count / (pauses + 1)
            for (k in 1..pauses) {
                val at = (k * step).coerceIn(1, count - 2)
                gapMinutesList[at] += rng.nextLong(24, 41) * HOUR / MINUTE
            }
        }
        val totalMillis = gapMinutesList.sum() * MINUTE
        val startMillis = endMillis - totalMillis

        // Senders with natural runs; unread tail forced to peers.
        val unread = unreadCounts[chat.id] ?: 0
        val tailFrom = count - unread
        val senderIds = ArrayList<String>(count)
        var runLeft = 0
        for (i in 0 until count) {
            val forcedPeer = i >= tailFrom
            if (runLeft > 0 && !forcedPeer) {
                senderIds.add(senderIds.last())
                runLeft--
            } else {
                val isMe = if (forcedPeer) false else rng.nextInt(100) < 52
                senderIds.add(if (isMe) "me" else peers[rng.nextInt(peers.size)])
                runLeft = if (!forcedPeer && rng.nextInt(100) < 30) rng.nextInt(1, 3) else 0
            }
        }
        // Guarantee run starts/ends do not split the unread tail weirdly.
        if (unread > 0 && tailFrom > 0 && senderIds[tailFrom - 1] !in peers && senderIds.getOrNull(tailFrom) != null) {
            senderIds[tailFrom - 1] = peers[rng.nextInt(peers.size)]
        }

        // Texts round-robin per sender through the pool.
        val senderTurn = HashMap<String, Int>()
        fun nextText(sender: String): String {
            val poolIdx = senderTurn[sender] ?: (sender.hashCode().let { Math.floorMod(it, pool.size) })
            senderTurn[sender] = poolIdx + 1
            return pool[poolIdx % pool.size]
        }

        val rich = richSpecs(chat.id, count)
        val drafts = ArrayList<Draft>(count)
        var prevId: String? = null
        for (i in 0 until count) {
            val spec = rich.firstOrNull { it.atIndex == i }
            val sender = spec?.actor ?: senderIds[i]
            val content = spec?.content ?: MessageContent.Text(nextText(senderIds[i]))
            val draft = Draft(
                senderId = sender,
                content = content,
                status = spec?.status ?: MessageStatus.Read,
                replyToMessageId = if (spec?.replyToPrevious == true) prevId else null,
                isEdited = spec?.isEdited ?: false,
                isDeleted = spec?.isDeleted ?: false,
                isStarred = spec?.isStarred ?: false,
                reactions = spec?.reactions ?: emptyList(),
                linkPreview = spec?.linkPreview,
            )
            val id = "s${chat.id}-${String.format("%03d", i + 1)}"
            drafts.add(draft)
            prevId = id
        }

        var t = startMillis
        return drafts.mapIndexed { i, draft ->
            if (i > 0) t += gapMinutesList[i - 1] * MINUTE
            // Delivery ticks: mostly Read; a couple of chats demo Sent/Delivered.
            val status = when {
                draft.senderId == "me" && chat.id == "c-noah" && i == drafts.indexOfLast { it.senderId == "me" } ->
                    MessageStatus.Delivered
                draft.senderId == "me" && chat.id == "c-iva" && i == drafts.indexOfLast { it.senderId == "me" } ->
                    MessageStatus.Sent
                else -> draft.status
            }
            Message(
                id = "s${chat.id}-${String.format("%03d", i + 1)}",
                chatId = chat.id,
                senderId = draft.senderId,
                content = draft.content,
                sentAtMillis = t,
                status = status,
                isOutgoing = draft.senderId == "me",
                replyToMessageId = draft.replyToMessageId,
                isEdited = draft.isEdited,
                isDeleted = draft.isDeleted,
                isStarred = draft.isStarred,
                reactions = draft.reactions,
                linkPreview = draft.linkPreview,
            )
        }
    }

    /** Per-chat message counts (20-45 each → ~440 total). */
    private val messageCounts: Map<String, Int> = mapOf(
        "c-aria" to 38,
        "c-noah" to 30,
        "c-iva" to 24,
        "c-dev" to 22,
        "c-zara" to 21,
        "c-kabir" to 27,
        "c-mira" to 20,
        "c-sam" to 24,
        "c-lea" to 26,
        "c-rohan" to 21,
        "c-tara" to 20,
        "c-assistant" to 22,
        "c-design" to 44,
        "c-roadtrip" to 42,
        "c-fam" to 34,
        "c-run" to 26,
    )

    val messagesByChatId: Map<String, List<Message>> = chatSpecs.associate { (chat, endAgoMin) ->
        chat.id to buildConversation(chat, endAgoMin, messageCounts[chat.id] ?: 20)
    }

    /** Message classification for S20 sections. */
    fun isLink(text: String) = text.contains("http://") || text.contains("https://")
}
