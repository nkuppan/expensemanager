package com.naveenapps.expensemanager.core.common.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.DateTimeUnit
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
 */
private class ThreadSafeDateFormat(
    private val pattern: String,
    private val fixedLocale: Locale? = null,
) {
    private val perThread = ThreadLocal<Pair<Locale, SimpleDateFormat>>()

    fun get(): SimpleDateFormat {
        val locale = fixedLocale ?: Locale.getDefault()
        perThread.get()?.let { (cachedLocale, format) -> if (cachedLocale == locale) return format }
        return SimpleDateFormat(pattern, locale).also { perThread.set(locale to it) }
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

@OptIn(ExperimentalTime::class)
fun getTodayRange(timeZone: TimeZone = TimeZone.currentSystemDefault()): List<Long> {
    val clock = kotlin.time.Clock.System.now()
    val todayStartTime = clock.toLocalDateTime(timeZone).date
    val nextDateStartTime = todayStartTime.plus(1, DateTimeUnit.DAY)
    return listOf(
        todayStartTime.atStartOfDayIn(timeZone).toEpochMilliseconds(),
        nextDateStartTime.atStartOfDayIn(timeZone).toEpochMilliseconds(),
    )
}

@OptIn(ExperimentalTime::class)
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

@OptIn(ExperimentalTime::class)
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

@OptIn(ExperimentalTime::class)
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

@OptIn(ExperimentalTime::class)
fun Long.fromLocalToUTCTimeStamp(): Long = Instant.fromEpochMilliseconds(this)
    .toLocalDateTime(TimeZone.currentSystemDefault())
    .toInstant(TimeZone.UTC)
    .toEpochMilliseconds()

@OptIn(ExperimentalTime::class)
fun Long.fromUTCToLocalTimeStamp(): Long = Instant.fromEpochMilliseconds(this)
    .toLocalDateTime(TimeZone.UTC)
    .toInstant(TimeZone.currentSystemDefault())
    .toEpochMilliseconds()

fun Long.fromUTCToLocalDate(): Date = Date(this.fromUTCToLocalTimeStamp())

@OptIn(ExperimentalTime::class)
fun Long.toExactStartOfTheDay(): Date {
    val dateTime = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC)
    return Date(dateTime.date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds())
}

@OptIn(ExperimentalTime::class)
fun Date.getStartOfTheMonth(): Long {
    val dateTime =
        Instant.fromEpochMilliseconds(this.time).toLocalDateTime(TimeZone.currentSystemDefault())
    return dateTime.date.minus(dateTime.dayOfMonth, DateTimeUnit.DAY)
        .atStartOfDayIn(TimeZone.currentSystemDefault())
        .toEpochMilliseconds()
}

@OptIn(ExperimentalTime::class)
fun Date.getEndOfTheMonth(): Long {
    val dateTime =
        Instant.fromEpochMilliseconds(this.time).toLocalDateTime(TimeZone.currentSystemDefault())
    val startOfTheWeekDay = dateTime.date.minus(dateTime.dayOfMonth, DateTimeUnit.DAY)
        .atStartOfDayIn(TimeZone.currentSystemDefault())
    return startOfTheWeekDay.plus(1, DateTimeUnit.MONTH, TimeZone.currentSystemDefault())
        .toEpochMilliseconds()
}

/** Year-scoped counterpart of [getStartOfTheMonth], used for yearly (`BudgetPeriod.YEARLY`) budgets. */
@OptIn(ExperimentalTime::class)
fun Date.getStartOfTheYear(): Long {
    val dateTime =
        Instant.fromEpochMilliseconds(this.time).toLocalDateTime(TimeZone.currentSystemDefault())
    return dateTime.date.minus(dateTime.dayOfYear, DateTimeUnit.DAY)
        .atStartOfDayIn(TimeZone.currentSystemDefault())
        .toEpochMilliseconds()
}

/** Year-scoped counterpart of [getEndOfTheMonth], used for yearly (`BudgetPeriod.YEARLY`) budgets. */
@OptIn(ExperimentalTime::class)
fun Date.getEndOfTheYear(): Long {
    val dateTime =
        Instant.fromEpochMilliseconds(this.time).toLocalDateTime(TimeZone.currentSystemDefault())
    val startOfTheYear = dateTime.date.minus(dateTime.dayOfYear, DateTimeUnit.DAY)
        .atStartOfDayIn(TimeZone.currentSystemDefault())
    return startOfTheYear.plus(1, DateTimeUnit.YEAR, TimeZone.currentSystemDefault())
        .toEpochMilliseconds()
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
