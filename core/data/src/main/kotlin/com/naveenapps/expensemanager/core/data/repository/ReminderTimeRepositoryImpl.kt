package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.datastore.DeviceLocalDataStore
import com.naveenapps.expensemanager.core.datastore.ReminderTimeDataStore
import com.naveenapps.expensemanager.core.model.ReminderTimeState
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.ReminderTimeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

private const val DEFAULT_REMINDER_TIMER = "10:00:false"

class ReminderTimeRepositoryImpl(
    private val dataStore: ReminderTimeDataStore,
    private val deviceLocalDataStore: DeviceLocalDataStore,
    private val dispatchers: AppCoroutineDispatchers,
) : ReminderTimeRepository {

    override suspend fun saveReminderTime(reminderTime: ReminderTimeState): Boolean = withContext(dispatchers.io) {
        dataStore.setReminderTime(reminderTime)
        true
    }

    override fun getReminderTime(): Flow<ReminderTimeState> = dataStore.getReminderTime(DEFAULT_REMINDER_TIMER)

    override fun isReminderOn(): Flow<Boolean> = dataStore.isReminderOn()

    override suspend fun setReminderOn(reminder: Boolean): Resource<Boolean> = withContext(dispatchers.io) {
        dataStore.setReminder(reminder)
        return@withContext Resource.Success(true)
    }

    override fun isNotificationPrimerShown(): Flow<Boolean> = deviceLocalDataStore.isNotificationPrimerShown()

    override suspend fun setNotificationPrimerShown() = withContext(dispatchers.io) {
        deviceLocalDataStore.setNotificationPrimerShown()
        Unit
    }

    override fun isUpcomingAlertOn(): Flow<Boolean> = dataStore.isUpcomingAlertOn()

    override suspend fun setUpcomingAlertOn(on: Boolean) = withContext(dispatchers.io) {
        dataStore.setUpcomingAlertOn(on)
        Unit
    }

    override fun getLastUpcomingAlertDay(): Flow<Long?> = dataStore.getLastUpcomingAlertDay()

    override suspend fun setLastUpcomingAlertDay(epochDay: Long) = withContext(dispatchers.io) {
        dataStore.setLastUpcomingAlertDay(epochDay)
        Unit
    }

    override fun getReminderScheduledFor(): Flow<Long?> = dataStore.getReminderScheduledFor()

    override suspend fun setReminderScheduledFor(millis: Long) = withContext(dispatchers.io) {
        dataStore.setReminderScheduledFor(millis)
        Unit
    }

    override fun getReminderLastRunAt(): Flow<Long?> = dataStore.getReminderLastRunAt()

    override suspend fun setReminderLastRunAt(millis: Long) = withContext(dispatchers.io) {
        dataStore.setReminderLastRunAt(millis)
        Unit
    }
}
