package com.naveenapps.expensemanager.core.common.utils

import java.util.Date
import kotlin.time.Instant

// Instant versions of the DateUtils functions the UI uses (KMP groundwork C5). Each one formats
// through the same SimpleDateFormat as its Date twin, so the text on screen is unchanged. They
// share names with the Date versions, so an existing `import ...utils.toCompleteDate` covers both.
//
// Display text is Android-only for now: iOS gets its own formatter in Phase 3 (C6 display half).

private fun Instant.asDate(): Date = Date(toEpochMilliseconds())

fun Instant.toDate(): String = asDate().toDate()

fun Instant.toDateAndMonth(): String = asDate().toDateAndMonth()

fun Instant.toCompleteDate(): String = asDate().toCompleteDate()

fun Instant.toCompleteDateWithDate(): String = asDate().toCompleteDateWithDate()

fun Instant.toMonthAndYear(): String = asDate().toMonthAndYear()

fun Instant.toMonthYear(): String = asDate().toMonthYear()

fun Instant.toMonth(): Int = asDate().toMonth()

fun Instant.toYear(): String = asDate().toYear()

fun Instant.toYearInt(): Int = asDate().toYearInt()

fun Instant.toDay(): String = asDate().toDay()

fun Instant.toTimeAndMinutes(): String = asDate().toTimeAndMinutes()

fun Instant.toTimeAndMinutesWithAMPM(): String = asDate().toTimeAndMinutesWithAMPM()

fun Instant.getStartOfTheMonth(): Long = asDate().getStartOfTheMonth()

fun Instant.getEndOfTheMonth(): Long = asDate().getEndOfTheMonth()

fun Instant.getStartOfTheYear(): Long = asDate().getStartOfTheYear()

fun Instant.getEndOfTheYear(): Long = asDate().getEndOfTheYear()

/** [toExactStartOfTheDay] as an Instant: a date picker's UTC-midnight millis → start of that day in the device zone. */
fun Long.toExactStartOfTheDayInstant(): Instant = Instant.fromEpochMilliseconds(toExactStartOfTheDay().time)
