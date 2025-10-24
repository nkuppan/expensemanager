package com.naveenapps.expensemanager.core.domain.usecase.settings.theme

import com.naveenapps.expensemanager.core.repository.ThemeRepository


class ApplyThemeUseCase(private val repository: ThemeRepository) {

    suspend operator fun invoke() {
        repository.applyTheme()
    }
}
