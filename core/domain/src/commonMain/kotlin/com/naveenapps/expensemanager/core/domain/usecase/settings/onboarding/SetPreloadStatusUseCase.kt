package com.naveenapps.expensemanager.core.domain.usecase.settings.onboarding

import com.naveenapps.expensemanager.core.repository.SettingsRepository

class SetPreloadStatusUseCase(private val repository: SettingsRepository) {

    suspend operator fun invoke(preloaded: Boolean) {
        repository.setPreloaded(preloaded)
    }
}
