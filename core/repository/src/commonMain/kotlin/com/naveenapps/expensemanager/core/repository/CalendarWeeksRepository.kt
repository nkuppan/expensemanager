package com.naveenapps.expensemanager.core.repository

import kotlin.time.Instant

/**
 * How many calendar weeks the month or year containing [Instant] spans, following the
 * device's region rules (first day of the week, minimal days in the first week).
 *
 * kotlinx-datetime has no locale-aware week numbering, so this stays a platform
 * implementation: `java.util.Calendar` on Android, `NSCalendar` on iOS later.
 */
interface CalendarWeeksRepository {
    fun weeksInMonthOf(at: Instant): Int

    fun weeksInYearOf(at: Instant): Int
}
