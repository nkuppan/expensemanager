package com.naveenapps.expensemanager.core.domain.usecase.settings

import javax.inject.Inject

class UpdateReminderStatusUseCase @Inject constructor(private val repository: com.naveenapps.expensemanager.core.data.repository.SettingsRepository) {

    suspend operator fun invoke(reminderStatus: Boolean) {
        repository.setReminderOn(reminderStatus)
    }
}