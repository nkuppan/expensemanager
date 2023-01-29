package com.nkuppan.expensemanager.data.utils

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


fun getPreviousDateTime(numberOfDays:Int): Calendar {
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