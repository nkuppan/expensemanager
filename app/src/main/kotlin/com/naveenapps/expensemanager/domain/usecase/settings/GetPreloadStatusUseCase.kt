package com.naveenapps.expensemanager.domain.usecase.settings

import com.naveenapps.expensemanager.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetPreloadStatusUseCase @Inject constructor(private val repository: SettingsRepository) {
    suspend operator fun invoke(): Boolean {
        return repository.isPreloaded().first()
    }
}