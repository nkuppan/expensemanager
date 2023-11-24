package com.naveenapps.expensemanager.core.domain.usecase.settings

import javax.inject.Inject

class SetPreloadStatusUseCase @Inject constructor(private val repository: com.naveenapps.expensemanager.core.repository.SettingsRepository) {

    suspend operator fun invoke(preloaded: Boolean) {
        repository.setPreloaded(preloaded)
    }
}