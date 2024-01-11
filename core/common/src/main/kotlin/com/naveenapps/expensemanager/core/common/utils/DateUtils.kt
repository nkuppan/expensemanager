package com.naveenapps.expensemanager.core.common.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getTodayRange(timeZone: TimeZone = TimeZone.currentSystemDefault()): List<Long> {
    val clock = Clock.System.now()
    val todayStartTime = clock.toLocalDateTime(timeZone).date
    val nextDateStartTime = todayStartTime.plus(1, DateTimeUnit.DAY)
    return listOf(
        todayStartTime.atStartOfDayIn(timeZone).toEpochMilliseconds(),
        nextDateStartTime.atStartOfDayIn(timeZone).toEpochMilliseconds()
    )
}

fun getThisWeekRange(timeZone: TimeZone = TimeZone.currentSystemDefault()): List<Long> {
    val clock = Clock.System.now()
    val todayDateTime = clock.toLocalDateTime(timeZone)
    val startOfTheWeekDay =
        todayDateTime.date.minus(todayDateTime.dayOfWeek.isoDayNumber, DateTimeUnit.DAY)
    val endTimeOfTheWeek = startOfTheWeekDay.plus(1, DateTimeUnit.WEEK)
    return listOf(
        startOfTheWeekDay.atStartOfDayIn(timeZone).toEpochMilliseconds(),
        endTimeOfTheWeek.atStartOfDayIn(timeZone).toEpochMilliseconds()
    )
}

fun getThisMonthRange(timeZone: TimeZone = TimeZone.currentSystemDefault()): List<Long> {
    val clock = Clock.System.now()
    val todayDateTime = clock.toLocalDateTime(timeZone)
    val startOfTheWeekDay = todayDateTime.date.minus(todayDateTime.dayOfMonth - 1, DateTimeUnit.DAY)
    val endTimeOfTheWeek = startOfTheWeekDay.plus(1, DateTimeUnit.MONTH)
    return listOf(
        startOfTheWeekDay.atStartOfDayIn(timeZone).toEpochMilliseconds(),
        endTimeOfTheWeek.atStartOfDayIn(timeZone).toEpochMilliseconds()
    )
}

fun getThisYearRange(timeZone: TimeZone = TimeZone.currentSystemDefault()): List<Long> {
    val clock = Clock.System.now()
    val todayDateTime = clock.toLocalDateTime(timeZone)
    val startOfTheWeekDay = todayDateTime.date.minus(todayDateTime.dayOfYear - 1, DateTimeUnit.DAY)
    val endTimeOfTheWeek = startOfTheWeekDay.plus(1, DateTimeUnit.YEAR)
    return listOf(
        startOfTheWeekDay.atStartOfDayIn(timeZone).toEpochMilliseconds(),
        endTimeOfTheWeek.atStartOfDayIn(timeZone).toEpochMilliseconds()
    )
}

fun Long.toExactStartOfTheDay(): Date {
    val dateTime = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC)
    return Date(dateTime.date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds())
}

fun Date.getStartOfTheMonth(): Long {
    val dateTime =
        Instant.fromEpochMilliseconds(this.time).toLocalDateTime(TimeZone.currentSystemDefault())
    return dateTime.date.minus(dateTime.dayOfMonth, DateTimeUnit.DAY)
        .atStartOfDayIn(TimeZone.currentSystemDefault())
        .toEpochMilliseconds()
}

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
    return SimpleDateFormat("dd", Locale.getDefault()).format(this)
}

fun Date.toDateAndMonth(): String {
    return SimpleDateFormat("dd/MM", Locale.getDefault()).format(this)
}

fun Date.toCompleteDate(): String {
    return SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(this)
}

fun Date.toCompleteDateWithDate(): String {
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(this)
}

fun String.fromCompleteDate(): Date {
    return kotlin.runCatching {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(this)
    }.getOrNull() ?: Date()
}

fun Date.toMonthAndYear(): String {
    return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(this)
}

fun String.fromMonthAndYear(): Date? {
    return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).parse(this)
}

fun Date.toMonth(): Int {
    return SimpleDateFormat("MM", Locale.getDefault()).format(this).toInt()
}

fun Date.toYearInt(): Int {
    return this.toYear().toInt()
}

fun Date.toYear(): String {
    return SimpleDateFormat("yyyy", Locale.getDefault()).format(this)
}

fun Date.toTimeAndMinutes(): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(this)
}

fun Date.toMonthYear(): String {
    return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(this)
}

fun Date.toDay(): String {
    return SimpleDateFormat("EEEE", Locale.getDefault()).format(this)
}

fun String.fromTimeAndHour(): Date {
    return kotlin.runCatching {
        SimpleDateFormat("HH:mm", Locale.getDefault()).parse(this)
    }.getOrNull() ?: Date()
}

fun Date.toTimeAndMinutesWithAMPM(): String {
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(this)
}
