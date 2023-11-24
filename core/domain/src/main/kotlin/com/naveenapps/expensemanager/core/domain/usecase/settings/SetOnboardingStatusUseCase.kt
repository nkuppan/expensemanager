package com.naveenapps.expensemanager.core.domain.usecase.settings

import javax.inject.Inject

class SetOnboardingStatusUseCase @Inject constructor(private val repository: com.naveenapps.expensemanager.core.repository.SettingsRepository) {

    suspend operator fun invoke(onboardingCompleted: Boolean) {
        repository.setOnboardingCompleted(onboardingCompleted)
    }
}