package com.naveenapps.expensemanager.domain.usecase.settings

import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetPreloadStatusUseCase @Inject constructor(private val repository: com.naveenapps.expensemanager.core.data.repository.SettingsRepository) {
    suspend operator fun invoke(): Boolean {
        return repository.isPreloaded().first()
    }
}