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
        MessageType.System -> null
    }
}
