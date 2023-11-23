package com.naveenapps.expensemanager.core.domain.usecase.settings

import com.naveenapps.expensemanager.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetPreloadStatusUseCase @Inject constructor(private val repository: SettingsRepository) {
    suspend operator fun invoke(): Boolean {
        return repository.isPreloaded().first()
    }
}