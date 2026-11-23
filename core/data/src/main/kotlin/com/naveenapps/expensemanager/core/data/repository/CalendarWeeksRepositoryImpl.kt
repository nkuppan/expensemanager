package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.repository.CalendarWeeksRepository
import java.util.Calendar
import kotlin.time.Instant

/** `Calendar.getInstance()` in the device zone and locale, as `GetAverageDataUseCase` always used. */
class CalendarWeeksRepositoryImpl : CalendarWeeksRepository {
    override fun weeksInMonthOf(at: Instant): Int = calendarAt(at).getActualMaximum(Calendar.WEEK_OF_MONTH)

    override fun weeksInYearOf(at: Instant): Int = calendarAt(at).getActualMaximum(Calendar.WEEK_OF_YEAR)

    private fun calendarAt(at: Instant): Calendar = Calendar.getInstance().apply { timeInMillis = at.toEpochMilliseconds() }
}
