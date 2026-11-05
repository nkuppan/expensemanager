package com.naveenapps.expensemanager.core.repository

import com.naveenapps.expensemanager.core.model.ReminderTimeState
import com.naveenapps.expensemanager.core.model.Resource
import kotlinx.coroutines.flow.Flow

interface ReminderTimeRepository {

    suspend fun saveReminderTime(reminderTime: ReminderTimeState): Boolean

    fun getReminderTime(): Flow<ReminderTimeState>

    fun isReminderOn(): Flow<Boolean>

    suspend fun setReminderOn(reminder: Boolean): Resource<Boolean>

    /** True once the in-app explainer before the notification permission has been answered. */
    fun isNotificationPrimerShown(): Flow<Boolean>

    suspend fun setNotificationPrimerShown()

    /** "Upcoming payment alerts": heads-up the evening before a monthly / yearly recurring entry. */
    fun isUpcomingAlertOn(): Flow<Boolean>

    suspend fun setUpcomingAlertOn(on: Boolean)

    fun getLastUpcomingAlertDay(): Flow<Long?>

    suspend fun setLastUpcomingAlertDay(epochDay: Long)
}
