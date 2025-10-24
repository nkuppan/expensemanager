package com.naveenapps.expensemanager.core.domain.usecase.settings.reminder

import com.naveenapps.expensemanager.core.model.ReminderTimeState
import com.naveenapps.expensemanager.core.repository.ReminderTimeRepository
import kotlinx.coroutines.flow.Flow

class GetReminderTimeUseCase(
    private val repository: ReminderTimeRepository,
) {
    operator fun invoke(): Flow<ReminderTimeState> {
        return repository.getReminderTime()
    }
}
