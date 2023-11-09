package com.naveenapps.expensemanager.domain.usecase.settings

import com.naveenapps.expensemanager.domain.repository.SettingsRepository
import javax.inject.Inject

class SetPreloadStatusUseCase @Inject constructor(private val repository: SettingsRepository) {

    suspend operator fun invoke(preloaded: Boolean) {
        repository.setPreloaded(preloaded)
    }
}