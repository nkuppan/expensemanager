package com.naveenapps.expensemanager.core.domain.usecase.settings.theme

import com.naveenapps.expensemanager.core.domain.repository.ThemeRepository
import javax.inject.Inject

class ApplyThemeUseCase @Inject constructor(private val repository: ThemeRepository) {

    suspend operator fun invoke() {
        repository.applyTheme()
    }
}