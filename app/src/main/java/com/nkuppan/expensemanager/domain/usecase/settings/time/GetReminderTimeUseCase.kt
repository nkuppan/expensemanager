package com.nkuppan.expensemanager.domain.usecase.settings.time

import com.nkuppan.expensemanager.domain.model.ReminderTimeState
import com.nkuppan.expensemanager.domain.repository.ReminderTimeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetReminderTimeUseCase @Inject constructor(
    private val repository: ReminderTimeRepository
) {
    operator fun invoke(): Flow<ReminderTimeState> {
        return repository.getReminderTime()
    }
}