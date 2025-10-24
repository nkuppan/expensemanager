package com.naveenapps.expensemanager.core.domain.usecase.settings.reminder

import com.naveenapps.expensemanager.core.repository.ReminderTimeRepository

class UpdateReminderStatusUseCase(
    private val repository: ReminderTimeRepository,
) {

    suspend operator fun invoke(reminderStatus: Boolean) {
        repository.setReminderOn(reminderStatus)
    }
}
