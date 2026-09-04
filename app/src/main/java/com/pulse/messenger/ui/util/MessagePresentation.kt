package com.pulse.messenger.ui.util

import com.pulse.messenger.domain.model.MessageType
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Tabular-safe time/date formats used across list rows. */
object TimeFormat {
    private val timeFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dateFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM")
    private val zone: ZoneId = ZoneId.systemDefault()

    /**
     * Chat list timestamps: "HH:mm" today, "Yesterday", weekday within the
     * current week, otherwise "d MMM".
     */
    fun chatTimestamp(millis: Long, nowMillis: Long = System.currentTimeMillis()): String {
        val date = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
        val today = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
        val days = java.time.temporal.ChronoUnit.DAYS.between(date, today)
        return when {
            days <= 0 -> timeFmt.format(date.atStartOfDay().plusSeconds(0)).let {
                Instant.ofEpochMilli(millis).atZone(zone).format(timeFmt)
            }
            days == 1L -> "Yesterday"
            days < 7 && today.dayOfWeek <= date.dayOfWeek -> date.dayOfWeek.shortName()
            date.year == today.year -> dateFmt.format(date)
            else -> DateTimeFormatter.ofPattern("d MMM yy").format(date)
        }
    }

    /** Full date for search results / media rows. */
    fun fullDate(millis: Long): String =
        Instant.ofEpochMilli(millis).atZone(zone).format(DateTimeFormatter.ofPattern("d MMM yyyy"))

    /** Short weekday name. */
    private fun DayOfWeek.shortName(): String =
        when (this) {
            DayOfWeek.MONDAY -> "Mon"
            DayOfWeek.TUESDAY -> "Tue"
            DayOfWeek.WEDNESDAY -> "Wed"
            DayOfWeek.THURSDAY -> "Thu"
            DayOfWeek.FRIDAY -> "Fri"
            DayOfWeek.SATURDAY -> "Sat"
            DayOfWeek.SUNDAY -> "Sun"
        }
}

/**
 * Human label for the message-type icon prefixes in list rows (PRD S19:
 * photo/mic/file previews) and search result sections.
 */
object MessageLabels {
    fun typeLabel(type: MessageType): String? = when (type) {
        MessageType.Text -> null
        MessageType.Image -> "Photo"
        MessageType.Video -> "Video"
        MessageType.Voice -> "Voice message"
        MessageType.File -> "File"
        MessageType.Location -> "Location"
        MessageType.Contact -> "Contact"
        MessageType.Poll -> "Poll"
        MessageType.Sticker -> "Sticker"
        MessageType.System -> null
    }

    /** Bubble label for non-text content rows (rendered as placeholders in M4a). */
    fun bubbleLabel(type: MessageType): String = when (type) {
        MessageType.Text -> ""
        MessageType.System -> ""
        else -> typeLabel(type).orEmpty()
    }
}

/**
 * Conversation chrome formatting (S23): bubble clocks, day pills and
 * presence lines. English-only by design until localization (Phase 2).
 */
object ConversationFormat {
    private val clockFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val timeZone: ZoneId = ZoneId.systemDefault()

    fun clock(millis: Long): String =
        Instant.ofEpochMilli(millis).atZone(timeZone).format(clockFmt)

    /** Playback/duration label: 0:06 / 1:02 / 12:05. */
    fun durationLabel(totalSeconds: Int): String {
        val s = totalSeconds.coerceAtLeast(0)
        return "%d:%02d".format(java.util.Locale.ROOT, s / 60, s % 60)
    }

    /** Human file size: B / KB / MB (one decimal below 10). */
    fun bytesLabel(bytes: Long): String {
        val b = bytes.coerceAtLeast(0)
        return when {
            b >= 1L shl 20 -> "%.1f MB".format(java.util.Locale.ROOT, b / 1048576.0)
            b >= 1L shl 10 -> "%.0f KB".format(java.util.Locale.ROOT, b / 1024.0)
            else -> "$b B"
        }
    }

    /** Live-location remaining time label ("7h 12m" / "12m"). */
    fun remainingLabel(millis: Long): String {
        val mins = (millis.coerceAtLeast(0) / 60_000L).toInt()
        val h = mins / 60
        val m = mins % 60
        return if (h > 0) "%dh %dm".format(java.util.Locale.ROOT, h, m) else "%dm".format(java.util.Locale.ROOT, m)
    }

    private fun dateOf(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(timeZone).toLocalDate()

    private val dayNames = listOf(
        "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday",
    )

    private fun weekdayName(date: LocalDate): String = dayNames[date.dayOfWeek.value - 1]

    /**
     * Date-pill label: "Today", "Yesterday", weekday within the current week,
     * otherwise "d MMM" (same year) / "d MMM yy".
     */
    fun dayLabel(millis: Long, nowMillis: Long = System.currentTimeMillis()): String {
        val date = dateOf(millis)
        val today = dateOf(nowMillis)
        val days = java.time.temporal.ChronoUnit.DAYS.between(date, today)
        return when {
            days <= 0 -> "Today"
            days == 1L -> "Yesterday"
            days < 7 -> weekdayName(date)
            date.year == today.year -> TimeFormat.fullDate(millis)
            else -> Instant.ofEpochMilli(millis).atZone(timeZone)
                .format(DateTimeFormatter.ofPattern("d MMM yy"))
        }
    }

    /**
     * Presence line for direct chats: "online", "last seen just now",
     * "last seen Xm ago", "last seen Xh ago", "last seen yesterday" or
     * "last seen d MMM". Null millis falls back to "last seen recently".
     */
    fun lastSeen(lastSeenAtMillis: Long?, nowMillis: Long = System.currentTimeMillis()): String {
        val seen = lastSeenAtMillis ?: return "last seen recently"
        val agoMinutes = (nowMillis - seen) / 60_000L
        return when {
            agoMinutes < 1 -> "last seen just now"
            agoMinutes < 60 -> "last seen ${agoMinutes}m ago"
            agoMinutes < 24 * 60 -> "last seen ${agoMinutes / 60}h ago"
            agoMinutes < 48 * 60 -> "last seen yesterday"
            else -> "last seen ${TimeFormat.fullDate(seen)}"
        }
    }
}
