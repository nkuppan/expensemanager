package com.naveenapps.expensemanager.core.domain.usecase.recurring

import com.naveenapps.expensemanager.core.model.RecurringFrequency
import java.time.ZoneId
import java.util.Date

/**
 * Date of occurrence [index] of a rule that started on [start]. Always counted from the start,
 * never from the previous occurrence, so a rule on Jan 31 gives Feb 28, Mar 31, Apr 30, ...
 * instead of drifting to the 28th forever. Local time keeps the same wall-clock time across DST.
 */
fun occurrenceDate(
    start: Date,
    frequency: RecurringFrequency,
    index: Int,
    zone: ZoneId = ZoneId.systemDefault(),
): Date {
    val base = start.toInstant().atZone(zone).toLocalDateTime()
    val n = index.toLong()
    val shifted = when (frequency) {
        RecurringFrequency.DAILY -> base.plusDays(n)
        RecurringFrequency.WEEKLY -> base.plusWeeks(n)
        RecurringFrequency.MONTHLY -> base.plusMonths(n)
        RecurringFrequency.YEARLY -> base.plusYears(n)
    }
    return Date.from(shifted.atZone(zone).toInstant())
}
