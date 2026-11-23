package com.naveenapps.expensemanager.core.domain.usecase.recurring

import com.naveenapps.expensemanager.core.model.RecurringFrequency
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * Date of occurrence [index] of a rule that started on [start]. Always counted from the start,
 * never from the previous occurrence, so a rule on Jan 31 gives Feb 28, Mar 31, Apr 30, ...
 * instead of drifting to the 28th forever. Local time keeps the same wall-clock time across DST:
 * a time that falls in a spring-forward gap moves later by the gap, and one in a fall-back
 * overlap takes the earlier offset (the same rules as the java.time version this replaced,
 * pinned by RecurringTransactionsTest).
 */
fun occurrenceDate(
    start: Instant,
    frequency: RecurringFrequency,
    index: Int,
    zone: TimeZone = TimeZone.currentSystemDefault(),
): Instant {
    val base = start.toLocalDateTime(zone)
    val date = when (frequency) {
        RecurringFrequency.DAILY -> base.date.plus(index, DateTimeUnit.DAY)
        RecurringFrequency.WEEKLY -> base.date.plus(index, DateTimeUnit.WEEK)
        RecurringFrequency.MONTHLY -> base.date.plus(index, DateTimeUnit.MONTH)
        RecurringFrequency.YEARLY -> base.date.plus(index, DateTimeUnit.YEAR)
    }
    return LocalDateTime(date, base.time).toInstant(zone)
}

/**
 * True when [date] falls on a later calendar day than [now]. A repeating entry dated like that is
 * planned: only the rule is saved, and its first entry is logged automatically on that day.
 */
fun isOnLaterDay(
    date: Instant,
    now: Instant = Clock.System.now(),
    zone: TimeZone = TimeZone.currentSystemDefault(),
): Boolean = date.toLocalDateTime(zone).date > now.toLocalDateTime(zone).date
