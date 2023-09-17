package com.nkuppan.expensemanager.domain.model

data class ReminderTimeState(
    val hour: Int,
    val minute: Int,
    val is24Hour: Boolean
)
