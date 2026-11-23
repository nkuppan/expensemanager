package com.naveenapps.expensemanager.core.common.utils

import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

// Wall-clock edits on an Instant, for forms with separate date and time pickers (KMP groundwork
// C5). Pure kotlinx-datetime, so they can move to shared code unchanged.

/** Hour (0–23) and minute of this instant's wall-clock time in [zone]. */
fun Instant.hourAndMinute(zone: TimeZone = TimeZone.currentSystemDefault()): Pair<Int, Int> = toLocalDateTime(zone).let { it.hour to it.minute }

/** The calendar day of [day], keeping this instant's time of day (seconds included). */
fun Instant.withDateOf(day: Instant, zone: TimeZone = TimeZone.currentSystemDefault()): Instant = LocalDateTime(day.toLocalDateTime(zone).date, toLocalDateTime(zone).time).toInstant(zone)

/**
 * This instant's day at [hour]:[minute]. With [resetSeconds] the seconds and fraction become 0;
 * without, they're kept (what the add-transaction form has always done).
 */
fun Instant.withTime(
    hour: Int,
    minute: Int,
    resetSeconds: Boolean = true,
    zone: TimeZone = TimeZone.currentSystemDefault(),
): Instant {
    val local = toLocalDateTime(zone)
    val time = if (resetSeconds) {
        LocalTime(hour, minute)
    } else {
        LocalTime(hour, minute, local.time.second, local.time.nanosecond)
    }
    return LocalDateTime(local.date, time).toInstant(zone)
}
