package com.naveenapps.expensemanager.core.domain.usecase.settings.reminder

import com.naveenapps.expensemanager.core.model.ReminderTimeState

/**
 * Picks a daily reminder time from when the user actually logs: the median time of day of their
 * recent saves, rounded down to the half hour so the reminder arrives just before their usual
 * moment, and kept between [EARLIEST] and [LATEST] so it never lands in the middle of the night.
 * Null when there are too few saves to tell.
 */
object SuggestReminderTime {

    const val MIN_SAVES = 3

    private const val STEP_MINUTES = 30
    private const val EARLIEST = 8 * 60
    private const val LATEST = 22 * 60

    fun from(saveMinutes: List<Int>, is24Hour: Boolean): ReminderTimeState? {
        if (saveMinutes.size < MIN_SAVES) return null
        val sorted = saveMinutes.sorted()
        val median = sorted[(sorted.size - 1) / 2]
        val rounded = (median / STEP_MINUTES) * STEP_MINUTES
        val minute = rounded.coerceIn(EARLIEST, LATEST)
        return ReminderTimeState(hour = minute / 60, minute = minute % 60, is24Hour = is24Hour)
    }
}
