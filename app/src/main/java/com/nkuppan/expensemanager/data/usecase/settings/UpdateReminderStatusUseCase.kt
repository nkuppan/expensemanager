package com.nkuppan.expensemanager.data.usecase.settings

import com.nkuppan.expensemanager.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateReminderStatusUseCase @Inject constructor(private val repository: SettingsRepository) {

    suspend operator fun invoke(reminderStatus: Boolean) {
        repository.setReminderOn(reminderStatus)
    }
}