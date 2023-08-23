package com.nkuppan.expensemanager.data.usecase.settings

import com.nkuppan.expensemanager.data.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetReminderStatusUseCase @Inject constructor(private val repository: SettingsRepository) {
    operator fun invoke(): Flow<Boolean> {
        return repository.isReminderOn()
    }
}