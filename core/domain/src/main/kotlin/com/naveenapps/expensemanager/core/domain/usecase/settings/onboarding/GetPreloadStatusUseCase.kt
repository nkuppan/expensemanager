package com.naveenapps.expensemanager.core.domain.usecase.settings.onboarding

import com.naveenapps.expensemanager.core.repository.SettingsRepository
import kotlinx.coroutines.flow.first

class GetPreloadStatusUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(): Boolean {
        return repository.isPreloaded().first()
    }
}
