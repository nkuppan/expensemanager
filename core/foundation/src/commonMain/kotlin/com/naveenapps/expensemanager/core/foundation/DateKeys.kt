package com.naveenapps.expensemanager.core.foundation

import kotlin.time.Instant
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/*
 * Locale-independent date *keys*: values that are stored or matched later, so they must never
 * depend on the app's language. Pure kotlinx-datetime, no java.* (KMP groundwork C6), and
 * byte-identical to the SimpleDateFormat versions in DateUtils.kt they replace (pinned by
 * DateKeysTest across time zones and default locales). Text shown to the user is a different
 * job: it follows the app language and stays in DateUtils until the display formatter lands.
 */

private val ENGLISH_MONTHS = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December",
)

private fun Int.pad(width: Int): String = toString().padStart(width, '0')

/** The calendar day of this instant in [timeZone] (the device zone by default). */
fun Instant.toLocalDateIn(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate = toLocalDateTime(timeZone).date

/** "March 2026": the month key stored in `Budget.selectedMonth`. Always English. */
fun LocalDate.toMonthAndYearKey(): String = "${ENGLISH_MONTHS[month.number - 1]} ${year.pad(4)}"

/**
 * The 1st of the month a [toMonthAndYearKey] value names, or null if it isn't one. Month names
 * match without regard to case, and the three-letter form is accepted too.
 */
fun String.monthAndYearKeyToLocalDate(): LocalDate? {
    val parts = trim().split(' ').filter { it.isNotEmpty() }
    if (parts.size != 2) return null
    val name = parts[0]
    val index = ENGLISH_MONTHS.indexOfFirst { it.equals(name, ignoreCase = true) }
        .takeIf { it >= 0 }
        ?: ENGLISH_MONTHS.indexOfFirst { name.length == 3 && it.take(3).equals(name, ignoreCase = true) }
            .takeIf { it >= 0 }
        ?: return null
    val year = parts[1].toIntOrNull()?.takeIf { parts[1].all(Char::isDigit) } ?: return null
    return LocalDate(year, index + 1, 1)
}

/** "2026": the year key stored in `Budget.selectedMonth` for yearly budgets. */
fun LocalDate.toYearKey(): String = year.pad(4)

/** 1 January of the year a [toYearKey] value names, or null if it isn't one. */
fun String.yearKeyToLocalDate(): LocalDate? {
    val text = trim()
    if (text.isEmpty() || !text.all(Char::isDigit)) return null
    return text.toIntOrNull()?.let { LocalDate(it, 1, 1) }
}

/** "22/11/2026": the day key that groups transactions by day (also shown as-is in lists). */
fun LocalDate.toDayKey(): String = "${day.pad(2)}/${month.number.pad(2)}/${year.pad(4)}"

// Period bounds. Transactions are matched with SQL `BETWEEN start AND end`, so both ends are
// inclusive: start is the first millisecond of the period, end is the last one. Same results as
// Date.getStartOfTheMonth() & co. in core:common's DateUtils.kt.

/** First millisecond of this date's month in [timeZone]. */
fun LocalDate.startOfMonthMillis(timeZone: TimeZone = TimeZone.currentSystemDefault()): Long = LocalDate(year, month, 1).atStartOfDayIn(timeZone).toEpochMilliseconds()

/** Last millisecond of this date's month in [timeZone]. */
fun LocalDate.endOfMonthMillis(timeZone: TimeZone = TimeZone.currentSystemDefault()): Long = LocalDate(year, month, 1).plus(1, DateTimeUnit.MONTH).atStartOfDayIn(timeZone).toEpochMilliseconds() - 1

/** First millisecond of this date's year in [timeZone]. */
fun LocalDate.startOfYearMillis(timeZone: TimeZone = TimeZone.currentSystemDefault()): Long = LocalDate(year, 1, 1).atStartOfDayIn(timeZone).toEpochMilliseconds()

/** Last millisecond of this date's year in [timeZone]. */
fun LocalDate.endOfYearMillis(timeZone: TimeZone = TimeZone.currentSystemDefault()): Long = LocalDate(year + 1, 1, 1).atStartOfDayIn(timeZone).toEpochMilliseconds() - 1

/** [toMonthAndYearKey] of this instant's date in [timeZone] (the stored `Budget.selectedMonth` key). */
fun Instant.toMonthAndYearKey(timeZone: TimeZone = TimeZone.currentSystemDefault()): String = toLocalDateIn(timeZone).toMonthAndYearKey()

/** [toYearKey] of this instant's date in [timeZone] (the stored key of a yearly budget). */
fun Instant.toYearKey(timeZone: TimeZone = TimeZone.currentSystemDefault()): String = toLocalDateIn(timeZone).toYearKey()
