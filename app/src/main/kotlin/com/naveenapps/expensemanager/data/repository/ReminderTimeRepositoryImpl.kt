package com.naveenapps.expensemanager.data.repository

import com.naveenapps.expensemanager.core.model.ReminderTimeState
import com.naveenapps.expensemanager.data.datastore.ReminderTimeDataStore
import com.naveenapps.expensemanager.domain.repository.ReminderTimeRepository
import com.naveenapps.expensemanager.utils.AppCoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val DEFAULT_REMINDER_TIMER = "10:00:false"

class ReminderTimeRepositoryImpl @Inject constructor(
    private val dataStore: ReminderTimeDataStore,
    private val dispatchers: AppCoroutineDispatchers
) : ReminderTimeRepository {
    override suspend fun saveReminderTime(reminderTime: ReminderTimeState): Boolean =
        withContext(dispatchers.io) {
            dataStore.setReminderTime(reminderTime)
            true
        }


    override fun getReminderTime(): Flow<ReminderTimeState> {
        return dataStore.getReminderTime(DEFAULT_REMINDER_TIMER)
    }
}