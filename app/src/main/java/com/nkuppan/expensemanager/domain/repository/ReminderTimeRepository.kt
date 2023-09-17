package com.nkuppan.expensemanager.domain.repository

import com.nkuppan.expensemanager.domain.model.ReminderTimeState
import kotlinx.coroutines.flow.Flow

interface ReminderTimeRepository {

    suspend fun saveReminderTime(reminderTime: ReminderTimeState): Boolean

    fun getReminderTime(): Flow<ReminderTimeState>
}