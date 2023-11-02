package com.nkuppan.expensemanager.data.utils

import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getTodayRange(): List<Long> {
    val startOfTheDay = DateTime().withTimeAtStartOfDay().millis
    val endOfTheDay = DateTime().plusDays(1).withTimeAtStartOfDay().millis
    return listOf(startOfTheDay, endOfTheDay)
}

fun getThisWeekRange(): List<Long> {
    val startDayOfWeek = DateTime().withDayOfWeek(1).withTimeAtStartOfDay().millis
    val endDayOfWeek = DateTime()
        .run {
            return@run dayOfWeek().withMaximumValue().plusDays(1).withTimeAtStartOfDay().millis
        }

    return listOf(startDayOfWeek, endDayOfWeek)
}

fun getThisMonthRange(): List<Long> {
    val startDayOfMonth = DateTime().withDayOfMonth(1).withTimeAtStartOfDay().millis
    val endDayOfMonth = DateTime()
        .run {
            return@run dayOfMonth().withMaximumValue().plusDays(1)
                .withTimeAtStartOfDay().millis
        }

    return listOf(startDayOfMonth, endDayOfMonth)
}

fun getThisYearRange(): List<Long> {
    val startDayOfMonth = DateTime().withMonthOfYear(1).withTimeAtStartOfDay().millis
    val endDayOfMonth = DateTime()
        .run {
            return@run monthOfYear().withMaximumValue().plusDays(1).withTimeAtStartOfDay().millis
        }

    return listOf(startDayOfMonth, endDayOfMonth)
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
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(this)
}

fun Date.toMonthAndYear(): String {
    return SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(this)
}

fun String.fromMonthAndYear(): Date? {
    return SimpleDateFormat("MM/yyyy", Locale.getDefault()).parse(this)
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