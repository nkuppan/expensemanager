package com.naveenapps.expensemanager.domain.usecase.settings.theme

import com.naveenapps.expensemanager.core.model.Theme
import javax.inject.Inject

class GetThemesUseCase @Inject constructor(private val repository: com.naveenapps.expensemanager.core.data.repository.ThemeRepository) {

    operator fun invoke(): List<Theme> {
        return repository.getThemes()
    }
}