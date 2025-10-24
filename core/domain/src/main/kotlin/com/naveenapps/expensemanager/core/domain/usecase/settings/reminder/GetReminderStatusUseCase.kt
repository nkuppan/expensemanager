package com.naveenapps.expensemanager.core.domain.usecase.settings.reminder

import com.naveenapps.expensemanager.core.repository.ReminderTimeRepository
import kotlinx.coroutines.flow.Flow

class GetReminderStatusUseCase(private val repository: ReminderTimeRepository) {
    operator fun invoke(): Flow<Boolean> {
        return repository.isReminderOn()
    }
}
