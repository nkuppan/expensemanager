package com.naveenapps.expensemanager.domain.usecase.settings

import javax.inject.Inject

class SetPreloadStatusUseCase @Inject constructor(private val repository: com.naveenapps.expensemanager.core.data.repository.SettingsRepository) {

    suspend operator fun invoke(preloaded: Boolean) {
        repository.setPreloaded(preloaded)
    }
}