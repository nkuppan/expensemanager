package com.naveenapps.expensemanager.core.domain.usecase.settings.theme

import com.naveenapps.expensemanager.core.model.Theme
import javax.inject.Inject

class GetThemesUseCase @Inject constructor(private val repository: com.naveenapps.expensemanager.core.repository.ThemeRepository) {

    operator fun invoke(): List<Theme> {
        return repository.getThemes()
    }
}