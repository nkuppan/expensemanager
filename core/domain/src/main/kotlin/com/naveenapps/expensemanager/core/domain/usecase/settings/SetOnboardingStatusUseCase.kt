package com.naveenapps.expensemanager.core.domain.usecase.settings

import com.naveenapps.expensemanager.core.domain.repository.SettingsRepository
import javax.inject.Inject

class SetOnboardingStatusUseCase @Inject constructor(private val repository: SettingsRepository) {

    suspend operator fun invoke(onboardingCompleted: Boolean) {
        repository.setOnboardingCompleted(onboardingCompleted)
    }
}