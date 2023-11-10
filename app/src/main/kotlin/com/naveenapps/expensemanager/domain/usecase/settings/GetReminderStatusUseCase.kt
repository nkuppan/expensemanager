package com.naveenapps.expensemanager.domain.usecase.settings

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetReminderStatusUseCase @Inject constructor(private val repository: com.naveenapps.expensemanager.core.data.repository.SettingsRepository) {
    operator fun invoke(): Flow<Boolean> {
        return repository.isReminderOn()
    }
}