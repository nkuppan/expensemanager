package com.naveenapps.expensemanager.core.domain.usecase.settings.theme

import javax.inject.Inject

class ApplyThemeUseCase @Inject constructor(private val repository: com.naveenapps.expensemanager.core.repository.ThemeRepository) {

    suspend operator fun invoke() {
        repository.applyTheme()
    }
}
