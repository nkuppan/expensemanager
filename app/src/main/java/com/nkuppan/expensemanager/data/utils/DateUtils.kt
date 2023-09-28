package com.nkuppan.expensemanager.data.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


fun getDateTime(): Calendar {
    // get today and clear time of day
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    // ! clear would not reset the hour of day !
    calendar.clear(Calendar.MINUTE)
    calendar.clear(Calendar.SECOND)
    calendar.clear(Calendar.MILLISECOND)
    return calendar
}


fun getPreviousDateTime(numberOfDays: Int): Calendar {
    // get today and clear time of day
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    // ! clear would not reset the hour of day !
    calendar.clear(Calendar.MINUTE)
    calendar.clear(Calendar.SECOND)
    calendar.clear(Calendar.MILLISECOND)

    calendar.add(Calendar.DAY_OF_YEAR, -numberOfDays)
    return calendar
}

fun Date.toTransactionDate(): String {
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(this)
}

fun Date.toTransactionMonth(): String {
    return SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(this)
}

fun Date.toTransactionMonthValue(): Int {
    return SimpleDateFormat("MM", Locale.getDefault()).format(this).toInt()
}

fun Date.toTransactionYearValue(): Int {
    return SimpleDateFormat("yyyy", Locale.getDefault()).format(this).toInt()
}

fun Date.toTransactionDateOnly(): String {
    return SimpleDateFormat("dd/MM", Locale.getDefault()).format(this)
}

fun Date.toTransactionTimeOnly(): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(this)
}

fun Long.getDateValue(): Date? {
    return if (this > 0) {
        Date(this)
    } else {
        null
    }
}