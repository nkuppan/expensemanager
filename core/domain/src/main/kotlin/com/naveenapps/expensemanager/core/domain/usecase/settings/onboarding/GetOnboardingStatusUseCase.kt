package com.naveenapps.expensemanager.core.domain.usecase.settings.onboarding

import com.naveenapps.expensemanager.core.repository.SettingsRepository
import kotlinx.coroutines.flow.first

class GetOnboardingStatusUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(): Boolean {
        return repository.isOnboardingCompleted().first()
    }
}
