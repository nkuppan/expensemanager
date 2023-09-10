package com.nkuppan.expensemanager.domain.usecase.settings.theme

import com.nkuppan.expensemanager.domain.repository.ThemeRepository
import javax.inject.Inject

class ApplyThemeUseCase @Inject constructor(private val repository: ThemeRepository) {

    suspend operator fun invoke() {
        repository.applyTheme()
    }
}