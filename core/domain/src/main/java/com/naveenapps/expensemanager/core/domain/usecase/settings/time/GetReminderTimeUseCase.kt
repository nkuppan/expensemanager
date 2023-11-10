package com.naveenapps.expensemanager.core.domain.usecase.settings.time

import com.naveenapps.expensemanager.core.model.ReminderTimeState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetReminderTimeUseCase @Inject constructor(
    private val repository: com.naveenapps.expensemanager.core.data.repository.ReminderTimeRepository
) {
    operator fun invoke(): Flow<ReminderTimeState> {
        return repository.getReminderTime()
    }
}