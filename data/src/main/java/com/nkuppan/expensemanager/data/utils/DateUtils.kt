package com.nkuppan.expensemanager.data.utils

import java.text.SimpleDateFormat
import java.util.*


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

fun Long.getDateValue(): Date? {
    return if (this > 0) {
        Date(this)
    } else {
        null
    }
}