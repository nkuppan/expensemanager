package com.naveenapps.expensemanager.core.model

/**
 * Daily logging habit. A day counts as "logged" when it has at least one transaction or the user
 * confirmed "Nothing spent today".
 */
data class LoggingStreak(
    /** Consecutive logged days ending today, or ending yesterday if today isn't logged yet. */
    val current: Int,
    val longest: Int,
    val loggedToday: Boolean,
    /** Oldest first, today last: whether each of the last 7 days was logged. */
    val lastSevenDays: List<Boolean>,
)
