package com.naveenapps.expensemanager.core.domain.usecase.settings.reminder

import javax.inject.Inject

class UpdateReminderStatusUseCase @Inject constructor(
    private val repository: com.naveenapps.expensemanager.core.repository.ReminderTimeRepository,
) {

    suspend operator fun invoke(reminderStatus: Boolean) {
        repository.setReminderOn(reminderStatus)
    }
}
