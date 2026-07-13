package com.naveenapps.expensemanager.core.common.utils

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

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

private val YearDataFormat by lazy {
    SimpleDateFormat("yyyy", Locale.US)
}

private val HourAndMinutesIn24HoursFormat by lazy {
    SimpleDateFormat("HH:mm", Locale.US)
}

private val HourAndMinutesIn12HoursFormat by lazy {
    SimpleDateFormat("hh:mm a", Locale.getDefault())
}

private val MonthFormat by lazy {
    SimpleDateFormat("MM", Locale.US)
}

private val MonthAndYearFormat by lazy {
    SimpleDateFormat("MMMM yyyy", Locale.getDefault())
}

/**
 * Same pattern as [MonthAndYearFormat] but always English, used only for values that get
 * persisted or matched later (currently `Budget.selectedMonth`, and the transaction date it's
 * compared against). Never use this to show text to the user — use [Date.toMonthAndYear] for
 * that instead.
 */
private val MonthAndYearKeyFormat by lazy {
    SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
}

private val DateMonthAndYearFormat by lazy {
    SimpleDateFormat("dd/MM/yyyy", Locale.US)
}

private val DateFormat by lazy {
    SimpleDateFormat("dd", Locale.US)
}

private val DateAndMonthFormat by lazy {
    SimpleDateFormat("dd/MM", Locale.US)
}

private val ElabratedMonthDataFormat by lazy {
    SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
}

private val DayFormat by lazy {
    SimpleDateFormat("EEEE", Locale.getDefault())
}

private val ShortMontAndYearFormat by lazy {
    SimpleDateFormat("MM-yyyy", Locale.US)
}


@OptIn(ExperimentalTime::class)
fun getTodayRange(timeZone: TimeZone = TimeZone.currentSystemDefault()): List<Long> {
    val clock = kotlin.time.Clock.System.now()
    val todayStartTime = clock.toLocalDateTime(timeZone).date
    val nextDateStartTime = todayStartTime.plus(1, DateTimeUnit.DAY)
    return listOf(
        todayStartTime.atStartOfDayIn(timeZone).toEpochMilliseconds(),
        nextDateStartTime.atStartOfDayIn(timeZone).toEpochMilliseconds()
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
        endTimeOfTheWeek.atStartOfDayIn(timeZone).toEpochMilliseconds()
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
        endTimeOfTheWeek.atStartOfDayIn(timeZone).toEpochMilliseconds()
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
        endTimeOfTheWeek.atStartOfDayIn(timeZone).toEpochMilliseconds()
    )
}

@OptIn(ExperimentalTime::class)
fun Long.fromLocalToUTCTimeStamp(): Long {
    return Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .toInstant(TimeZone.UTC)
        .toEpochMilliseconds()
}

@OptIn(ExperimentalTime::class)
fun Long.fromUTCToLocalTimeStamp(): Long {
    return Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.UTC)
        .toInstant(TimeZone.currentSystemDefault())
        .toEpochMilliseconds()
}

fun Long.fromUTCToLocalDate(): Date {
    return Date(this.fromUTCToLocalTimeStamp())
}

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

fun Long.toCompleteDate(): Date {
    return Date(this)
}

fun Date.toDate(): String {
    return DateFormat.format(this)
}

fun Date.toDateAndMonth(): String {
    return DateAndMonthFormat.format(this)
}

fun Date.toCompleteDate(): String {
    return ElabratedMonthDataFormat.format(this)
}

fun Date.toCompleteDateWithDate(): String {
    return DateMonthAndYearFormat.format(this)
}

fun String.fromCompleteDate(): Date {
    return kotlin.runCatching {
        DateMonthAndYearFormat.parse(this)
    }.getOrNull() ?: Date()
}

fun Date.toMonthAndYear(): String {
    return MonthAndYearFormat.format(this)
}

fun String.fromMonthAndYear(): Date? {
    return kotlin.runCatching { MonthAndYearFormat.parse(this) }.getOrNull()
}

/**
 * Locale-independent counterpart of [toMonthAndYear], for values that get persisted or compared
 * later (e.g. `Budget.selectedMonth`). Always formats/parses in English so a budget saved while
 * the app was in one language can still be read back correctly after the user switches to
 * another — see [MonthAndYearKeyFormat].
 */
fun Date.toMonthAndYearKey(): String {
    return MonthAndYearKeyFormat.format(this)
}

/** See [Date.toMonthAndYearKey]. Returns null instead of throwing on unparseable input. */
fun String.fromMonthAndYearKey(): Date? {
    return kotlin.runCatching { MonthAndYearKeyFormat.parse(this) }.getOrNull()
}

fun Date.toMonth(): Int {
    return MonthFormat.format(this).toInt()
}

fun Date.toYearInt(): Int {
    return this.toYear().toInt()
}

fun Date.toYear(): String {
    return YearDataFormat.format(this)
}

fun Date.toTimeAndMinutes(): String {
    return HourAndMinutesIn24HoursFormat.format(this)
}

fun Date.toMonthYear(): String {
    return MonthAndYearFormat.format(this)
}

fun Date.toDay(): String {
    return DayFormat.format(this)
}

fun String.fromTimeAndHour(): Date {
    return kotlin.runCatching {
        HourAndMinutesIn24HoursFormat.parse(this)
    }.getOrNull() ?: Date()
}

fun Date.toTimeAndMinutesWithAMPM(): String {
    return HourAndMinutesIn12HoursFormat.format(this)
}

fun String.fromShortMonthAndYearToDate(): Date? {
    return kotlin.runCatching {
        ShortMontAndYearFormat.parse(this)
    }.getOrNull()
}
