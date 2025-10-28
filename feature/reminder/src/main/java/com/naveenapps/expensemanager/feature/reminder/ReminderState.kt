package com.naveenapps.expensemanager.feature.reminder

import com.naveenapps.expensemanager.core.model.ReminderTimeState

data class ReminderState(
    val reminderStatus: Boolean = false,
    val reminderTime: String = "06:00",
    val reminderTimeState: ReminderTimeState = ReminderTimeState(6, 0, false),
    val showTimePickerDialog: Boolean = false,
    val shouldShowRationale: Boolean = false,
    val showPermissionMessage: Boolean = false
)