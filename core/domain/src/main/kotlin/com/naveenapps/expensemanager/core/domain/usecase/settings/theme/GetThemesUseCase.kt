package com.naveenapps.expensemanager.core.domain.usecase.settings.theme

import com.naveenapps.expensemanager.core.model.Theme
import com.naveenapps.expensemanager.core.repository.ThemeRepository

class GetThemesUseCase(private val repository: ThemeRepository) {

    operator fun invoke(): List<Theme> {
        return repository.getThemes()
    }
}
