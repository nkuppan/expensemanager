package com.naveenapps.expensemanager.core.domain.usecase.settings.onboarding

import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetPreloadStatusUseCase @Inject constructor(private val repository: com.naveenapps.expensemanager.core.repository.SettingsRepository) {
    suspend operator fun invoke(): Boolean {
        return repository.isPreloaded().first()
    }
}
