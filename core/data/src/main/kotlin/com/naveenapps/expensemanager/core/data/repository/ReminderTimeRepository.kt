package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.model.ReminderTimeState
import kotlinx.coroutines.flow.Flow

interface ReminderTimeRepository {

    suspend fun saveReminderTime(reminderTime: ReminderTimeState): Boolean

    fun getReminderTime(): Flow<ReminderTimeState>
}