package com.naveenapps.expensemanager.domain.usecase.settings.theme

import javax.inject.Inject

class ApplyThemeUseCase @Inject constructor(private val repository: com.naveenapps.expensemanager.core.data.repository.ThemeRepository) {

    suspend operator fun invoke() {
        repository.applyTheme()
    }
}