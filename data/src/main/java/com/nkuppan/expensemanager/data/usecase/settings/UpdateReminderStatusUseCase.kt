package com.nkuppan.expensemanager.data.usecase.settings

import com.nkuppan.expensemanager.data.repository.SettingsRepository

class UpdateReminderStatusUseCase(private val repository: SettingsRepository) {

    suspend operator fun invoke(reminderStatus: Boolean) {
        repository.setReminderOn(reminderStatus)
    }
}