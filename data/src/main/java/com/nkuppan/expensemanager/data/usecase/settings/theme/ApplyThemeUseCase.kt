package com.nkuppan.expensemanager.data.usecase.settings.theme

import com.nkuppan.expensemanager.data.repository.ThemeRepository

class ApplyThemeUseCase(private val repository: ThemeRepository) {

    suspend operator fun invoke() {
        repository.applyTheme()
    }
}