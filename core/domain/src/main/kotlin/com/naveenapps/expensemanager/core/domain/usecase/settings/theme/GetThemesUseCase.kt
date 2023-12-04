package com.naveenapps.expensemanager.core.domain.usecase.settings.theme

import com.naveenapps.expensemanager.core.model.Theme
import com.naveenapps.expensemanager.core.repository.ThemeRepository
import javax.inject.Inject

class GetThemesUseCase @Inject constructor(private val repository: ThemeRepository) {

    operator fun invoke(): List<Theme> {
        return repository.getThemes()
    }
}
