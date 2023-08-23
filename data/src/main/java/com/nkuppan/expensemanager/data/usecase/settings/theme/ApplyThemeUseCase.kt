package com.nkuppan.expensemanager.data.usecase.settings.theme

import com.nkuppan.expensemanager.data.repository.ThemeRepository
import javax.inject.Inject

class ApplyThemeUseCase @Inject constructor(private val repository: ThemeRepository) {

    suspend operator fun invoke() {
        repository.applyTheme()
    }
}