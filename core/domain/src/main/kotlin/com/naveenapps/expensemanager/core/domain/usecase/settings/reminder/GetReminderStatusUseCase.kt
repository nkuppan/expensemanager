package com.naveenapps.expensemanager.core.domain.usecase.settings.reminder

import com.naveenapps.expensemanager.core.domain.repository.ReminderTimeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetReminderStatusUseCase @Inject constructor(private val repository: ReminderTimeRepository) {
    operator fun invoke(): Flow<Boolean> {
        return repository.isReminderOn()
    }
}