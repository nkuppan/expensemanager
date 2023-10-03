package com.nkuppan.expensemanager.data.repository

import com.nkuppan.expensemanager.common.utils.AppCoroutineDispatchers
import com.nkuppan.expensemanager.data.datastore.ReminderTimeDataStore
import com.nkuppan.expensemanager.domain.model.ReminderTimeState
import com.nkuppan.expensemanager.domain.repository.ReminderTimeRepository
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