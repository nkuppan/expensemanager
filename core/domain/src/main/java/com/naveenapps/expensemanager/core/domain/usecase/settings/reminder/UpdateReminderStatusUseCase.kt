package com.naveenapps.expensemanager.core.domain.usecase.settings.reminder

import com.naveenapps.expensemanager.core.data.repository.ReminderTimeRepository
import javax.inject.Inject

class UpdateReminderStatusUseCase @Inject constructor(
    private val repository: ReminderTimeRepository
) {

    suspend operator fun invoke(reminderStatus: Boolean) {
        repository.setReminderOn(reminderStatus)
    }
}