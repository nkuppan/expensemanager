package com.naveenapps.expensemanager.feature.reminder

import com.naveenapps.expensemanager.core.model.ReminderTimeState

sealed class ReminderAction {

    data object ClosePage : ReminderAction()

    data object OpenSettings : ReminderAction()

    data object RequestPermission : ReminderAction()

    data object PermissionGranted : ReminderAction()

    data object ShowPermissionRationale : ReminderAction()

    data object CloseReminderDialog : ReminderAction()

    data object ShowReminderDialog : ReminderAction()

    data class ChangeReminderStatus(val status: Boolean) : ReminderAction()

    data class SaveReminder(val reminderState: ReminderTimeState) : ReminderAction()
}