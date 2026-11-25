package com.naveenapps.expensemanager.core.common.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Instant
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

// Formats below fall into two groups:
//
// 1. Purely numeric patterns (digits only, no month/day *names*) — these are pinned to a fixed
//    Locale.US regardless of the app's selected display language. Some of them (notably
//    DateMonthAndYearFormat) are used as round-tripped keys: a value they format is later parsed
//    back with the *same* formatter (e.g. to group transactions by day). If that formatter's
//    Locale changes — which it now can, since the app supports switching its display language at
//    runtime — a value written under one locale can fail to parse under another. Numeric digits
//    don't need localization anyway, so pinning these removes that whole class of bug for free.
//
// 2. Patterns with localized month/day *names* (MonthAndYearFormat, ElabratedMonthDataFormat,
//    DayFormat, HourAndMinutesIn12HoursFormat) are kept locale-aware because they're only ever
//    used for one-way, human-facing display text — never parsed back — so following the app's
//    selected language is both safe and desirable.
//
// MonthAndYearFormat is the one exception that's used for *both* roles (see MonthAndYearKeyFormat
// below for the round-tripped one, used for Budget.selectedMonth).

/**
 * SimpleDateFormat is not thread-safe, and these are used from use cases running on background
 * dispatchers as well as from the UI, so each thread gets its own instance. Formats without a
 * fixed locale follow the current default locale, so an in-app language switch is picked up
 * without restarting the process (they used to keep the locale they were first created with).
 *
 * A SimpleDateFormat also keeps the device time zone it was created in, so the cache is keyed on
 * the zone too. Otherwise a zone change while the app is running (travel, automatic time-zone
 * updates) kept formatting and parsing budget keys in the old zone, while the period bounds
 * (kotlinx-datetime) used the new one, and a budget could pick up the wrong month.
 */
private class ThreadSafeDateFormat(
    private val pattern: String,
    private val fixedLocale: Locale? = null,
) {
    private class Cached(val locale: Locale, val timeZone: java.util.TimeZone, val format: SimpleDateFormat)

    private val perThread = ThreadLocal<Cached>()

    fun get(): SimpleDateFormat {
        val locale = fixedLocale ?: Locale.getDefault()
        val timeZone = java.util.TimeZone.getDefault()
        perThread.get()?.let { if (it.locale == locale && it.timeZone == timeZone) return it.format }
        return SimpleDateFormat(pattern, locale).also {
            it.timeZone = timeZone
            perThread.set(Cached(locale, timeZone, it))
        }
    }
}

private val YearDataFormat = ThreadSafeDateFormat("yyyy", Locale.US)

private val HourAndMinutesIn24HoursFormat = ThreadSafeDateFormat("HH:mm", Locale.US)

private val HourAndMinutesIn12HoursFormat = ThreadSafeDateFormat("hh:mm a")

private val MonthFormat = ThreadSafeDateFormat("MM", Locale.US)

private val MonthAndYearFormat = ThreadSafeDateFormat("MMMM yyyy")

/**
 * Same pattern as [MonthAndYearFormat] but always English, used only for values that get
 * persisted or matched later (currently `Budget.selectedMonth`, and the transaction date it's
 * compared against). Never use this to show text to the user — use [Date.toMonthAndYear] for
 * that instead.
 */
private val MonthAndYearKeyFormat = ThreadSafeDateFormat("MMMM yyyy", Locale.ENGLISH)

private val DateMonthAndYearFormat = ThreadSafeDateFormat("dd/MM/yyyy", Locale.US)

private val DateFormat = ThreadSafeDateFormat("dd", Locale.US)

private val DateAndMonthFormat = ThreadSafeDateFormat("dd/MM", Locale.US)

private val ElabratedMonthDataFormat = ThreadSafeDateFormat("MMMM dd, yyyy")

private val DayFormat = ThreadSafeDateFormat("EEEE")

private val ShortMontAndYearFormat = ThreadSafeDateFormat("MM-yyyy", Locale.US)

fun getTodayRange(timeZone: TimeZone = TimeZone.currentSystemDefault()): List<Long> {
    val clock = kotlin.time.Clock.System.now()
    val todayStartTime = clock.toLocalDateTime(timeZone).date
    val nextDateStartTime = todayStartTime.plus(1, DateTimeUnit.DAY)
    return listOf(
        todayStartTime.atStartOfDayIn(timeZone).toEpochMilliseconds(),
        nextDateStartTime.atStartOfDayIn(timeZone).toEpochMilliseconds(),
    )
}

fun getThisWeekRange(timeZone: TimeZone = TimeZone.currentSystemDefault()): List<Long> {
    val clock = kotlin.time.Clock.System.now()
    val todayDateTime = clock.toLocalDateTime(timeZone)
    val startOfTheWeekDay =
        todayDateTime.date.minus(todayDateTime.dayOfWeek.isoDayNumber, DateTimeUnit.DAY)
    val endTimeOfTheWeek = startOfTheWeekDay.plus(1, DateTimeUnit.WEEK)
    return listOf(
        startOfTheWeekDay.atStartOfDayIn(timeZone).toEpochMilliseconds(),
        endTimeOfTheWeek.atStartOfDayIn(timeZone).toEpochMilliseconds(),
    )
}

fun getThisMonthRange(timeZone: TimeZone = TimeZone.currentSystemDefault()): List<Long> {
    val clock = kotlin.time.Clock.System.now()
    val todayDateTime = clock.toLocalDateTime(timeZone)
    val startOfTheWeekDay = todayDateTime.date.minus(todayDateTime.dayOfMonth - 1, DateTimeUnit.DAY)
    val endTimeOfTheWeek = startOfTheWeekDay.plus(1, DateTimeUnit.MONTH)
    return listOf(
        startOfTheWeekDay.atStartOfDayIn(timeZone).toEpochMilliseconds(),
        endTimeOfTheWeek.atStartOfDayIn(timeZone).toEpochMilliseconds(),
    )
}

fun getThisYearRange(timeZone: TimeZone = TimeZone.currentSystemDefault()): List<Long> {
    val clock = kotlin.time.Clock.System.now()
    val todayDateTime = clock.toLocalDateTime(timeZone)
    val startOfTheWeekDay = todayDateTime.date.minus(todayDateTime.dayOfYear - 1, DateTimeUnit.DAY)
    val endTimeOfTheWeek = startOfTheWeekDay.plus(1, DateTimeUnit.YEAR)
    return listOf(
        startOfTheWeekDay.atStartOfDayIn(timeZone).toEpochMilliseconds(),
        endTimeOfTheWeek.atStartOfDayIn(timeZone).toEpochMilliseconds(),
    )
}

fun Long.fromLocalToUTCTimeStamp(): Long = Instant.fromEpochMilliseconds(this)
    .toLocalDateTime(TimeZone.currentSystemDefault())
    .toInstant(TimeZone.UTC)
    .toEpochMilliseconds()

fun Long.fromUTCToLocalTimeStamp(): Long = Instant.fromEpochMilliseconds(this)
    .toLocalDateTime(TimeZone.UTC)
    .toInstant(TimeZone.currentSystemDefault())
    .toEpochMilliseconds()

fun Long.fromUTCToLocalDate(): Date = Date(this.fromUTCToLocalTimeStamp())

fun Long.toExactStartOfTheDay(): Date {
    val dateTime = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC)
    return Date(dateTime.date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds())
}

// Period bounds for budgets. Transactions are matched with SQL `BETWEEN start AND end`, so
// both ends are inclusive: start is the first millisecond of the period, end is the last one.
// (These used to start on the last day of the *previous* period and stop at 00:00 on a day near
// the end of this one, so a month's last 1–4 days and 31 December never counted towards a budget.)

/** First millisecond of this date's month, in the device time zone. */

fun Date.getStartOfTheMonth(): Long {
    val timeZone = TimeZone.currentSystemDefault()
    val date = Instant.fromEpochMilliseconds(this.time).toLocalDateTime(timeZone).date
    return LocalDate(date.year, date.month, 1).atStartOfDayIn(timeZone).toEpochMilliseconds()
}

/** Last millisecond of this date's month, in the device time zone (inclusive, see above). */

fun Date.getEndOfTheMonth(): Long {
    val timeZone = TimeZone.currentSystemDefault()
    val date = Instant.fromEpochMilliseconds(this.time).toLocalDateTime(timeZone).date
    val firstOfNextMonth = LocalDate(date.year, date.month, 1).plus(1, DateTimeUnit.MONTH)
    return firstOfNextMonth.atStartOfDayIn(timeZone).toEpochMilliseconds() - 1
}

/** Year-scoped counterpart of [getStartOfTheMonth], used for yearly (`BudgetPeriod.YEARLY`) budgets. */

fun Date.getStartOfTheYear(): Long {
    val timeZone = TimeZone.currentSystemDefault()
    val date = Instant.fromEpochMilliseconds(this.time).toLocalDateTime(timeZone).date
    return LocalDate(date.year, 1, 1).atStartOfDayIn(timeZone).toEpochMilliseconds()
}

/** Year-scoped counterpart of [getEndOfTheMonth], used for yearly (`BudgetPeriod.YEARLY`) budgets. */

fun Date.getEndOfTheYear(): Long {
    val timeZone = TimeZone.currentSystemDefault()
    val date = Instant.fromEpochMilliseconds(this.time).toLocalDateTime(timeZone).date
    return LocalDate(date.year + 1, 1, 1).atStartOfDayIn(timeZone).toEpochMilliseconds() - 1
}

fun Long.toCompleteDate(): Date = Date(this)

fun Date.toDate(): String = DateFormat.get().format(this)

fun Date.toDateAndMonth(): String = DateAndMonthFormat.get().format(this)

fun Date.toCompleteDate(): String = ElabratedMonthDataFormat.get().format(this)

fun Date.toCompleteDateWithDate(): String = DateMonthAndYearFormat.get().format(this)

fun String.fromCompleteDate(): Date = kotlin.runCatching {
    DateMonthAndYearFormat.get().parse(this)
}.getOrNull() ?: Date()

fun Date.toMonthAndYear(): String = MonthAndYearFormat.get().format(this)

fun String.fromMonthAndYear(): Date? = kotlin.runCatching { MonthAndYearFormat.get().parse(this) }.getOrNull()

/**
 * Locale-independent counterpart of [toMonthAndYear], for values that get persisted or compared
 * later (e.g. `Budget.selectedMonth`). Always formats/parses in English so a budget saved while
 * the app was in one language can still be read back correctly after the user switches to
 * another — see [MonthAndYearKeyFormat].
 */
fun Date.toMonthAndYearKey(): String = MonthAndYearKeyFormat.get().format(this)

/** See [Date.toMonthAndYearKey]. Returns null instead of throwing on unparseable input. */
fun String.fromMonthAndYearKey(): Date? = kotlin.runCatching { MonthAndYearKeyFormat.get().parse(this) }.getOrNull()

fun Date.toMonth(): Int = MonthFormat.get().format(this).toInt()

fun Date.toYearInt(): Int = this.toYear().toInt()

fun Date.toYear(): String = YearDataFormat.get().format(this)

/**
 * See [Date.toYear]. Returns null instead of throwing on unparseable input. Used as the
 * round-trip key for yearly (`BudgetPeriod.YEARLY`) `Budget.selectedMonth` values, mirroring
 * [fromMonthAndYearKey]. Safe to round-trip like the other purely-numeric formats in this file
 * (see the file header comment) since [YearDataFormat] is already pinned to `Locale.US`.
 */
fun String.fromYear(): Date? = kotlin.runCatching { YearDataFormat.get().parse(this) }.getOrNull()

fun Date.toTimeAndMinutes(): String = HourAndMinutesIn24HoursFormat.get().format(this)

fun Date.toMonthYear(): String = MonthAndYearFormat.get().format(this)

fun Date.toDay(): String = DayFormat.get().format(this)

fun String.fromTimeAndHour(): Date = kotlin.runCatching {
    HourAndMinutesIn24HoursFormat.get().parse(this)
}.getOrNull() ?: Date()

fun Date.toTimeAndMinutesWithAMPM(): String = HourAndMinutesIn12HoursFormat.get().format(this)

fun String.fromShortMonthAndYearToDate(): Date? = kotlin.runCatching {
    ShortMontAndYearFormat.get().parse(this)
}.getOrNull()
