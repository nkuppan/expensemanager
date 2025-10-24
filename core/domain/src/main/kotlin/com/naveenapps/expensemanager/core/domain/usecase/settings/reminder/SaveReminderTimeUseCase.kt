package com.naveenapps.expensemanager.core.domain.usecase.settings.reminder

import com.naveenapps.expensemanager.core.model.ReminderTimeState
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.ReminderTimeRepository

class SaveReminderTimeUseCase(
    private val repository: ReminderTimeRepository,
) {
    suspend operator fun invoke(reminderTimeState: ReminderTimeState): Resource<Boolean> {
        return Resource.Success(repository.saveReminderTime(reminderTimeState))
    }
}
