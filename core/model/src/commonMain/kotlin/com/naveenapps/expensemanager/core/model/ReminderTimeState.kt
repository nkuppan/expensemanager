package com.naveenapps.expensemanager.core.model

data class ReminderTimeState(
    val hour: Int,
    val minute: Int,
    val is24Hour: Boolean,
)
