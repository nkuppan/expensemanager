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

    data class ChangeUpcomingAlerts(val on: Boolean) : ReminderAction()

    /** Screen resumed (maybe back from system settings): re-check reminder delivery. */
    data object RefreshDelivery : ReminderAction()

    /** A delivery fix button was tapped; [reason] is what it fixes (notifications or battery). */
    data class DeliveryFixOpened(val reason: String) : ReminderAction()
}
