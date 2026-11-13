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

    /** "Upcoming payment alerts": heads-up the evening before a weekly / monthly / yearly recurring entry. */
    fun isUpcomingAlertOn(): Flow<Boolean>

    suspend fun setUpcomingAlertOn(on: Boolean)

    fun getLastUpcomingAlertDay(): Flow<Long?>

    suspend fun setLastUpcomingAlertDay(epochDay: Long)

    /** When the next daily reminder is due (epoch millis); null before one was ever scheduled. */
    fun getReminderScheduledFor(): Flow<Long?>

    suspend fun setReminderScheduledFor(millis: Long)

    /** When the daily reminder's background job last ran (epoch millis), shown or skipped. */
    fun getReminderLastRunAt(): Flow<Long?>

    suspend fun setReminderLastRunAt(millis: Long)
}
