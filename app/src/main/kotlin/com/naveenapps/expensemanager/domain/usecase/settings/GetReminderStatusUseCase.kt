package com.naveenapps.expensemanager.domain.usecase.settings

import com.naveenapps.expensemanager.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetReminderStatusUseCase @Inject constructor(private val repository: SettingsRepository) {
    operator fun invoke(): Flow<Boolean> {
        return repository.isReminderOn()
    }
}