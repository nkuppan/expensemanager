package com.naveenapps.expensemanager.core.domain.usecase.settings.onboarding

import com.naveenapps.expensemanager.core.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetOnboardingStatusUseCase @Inject constructor(private val repository: SettingsRepository) {
    suspend operator fun invoke(): Boolean {
        return repository.isOnboardingCompleted().first()
    }
}
