package com.naveenapps.expensemanager.feature.reminder

import androidx.compose.runtime.Stable
import com.naveenapps.expensemanager.core.model.ReminderTimeState
import com.naveenapps.expensemanager.core.notification.ReminderBlock

@Stable
data class ReminderState(
    val reminderStatus: Boolean = false,
    val reminderTime: String = "06:00",
    val reminderTimeState: ReminderTimeState = ReminderTimeState(6, 0, false),
    val showTimePickerDialog: Boolean = false,
    val shouldShowRationale: Boolean = false,
    val showPermissionMessage: Boolean = false,
    /** "Upcoming payment alerts": heads-up the evening before a weekly / monthly / yearly recurring entry. */
    val upcomingAlertsOn: Boolean = true,
    /** When the daily reminder last ran; null before the first run. */
    val lastReminderRunAt: Long? = null,
    /** A system setting that stops reminders from showing, or null. */
    val deliveryBlock: ReminderBlock? = null,
    /** Battery optimisation applies to the app, so some phones may not run reminders on time. */
    val isBatteryOptimised: Boolean = false,
)
