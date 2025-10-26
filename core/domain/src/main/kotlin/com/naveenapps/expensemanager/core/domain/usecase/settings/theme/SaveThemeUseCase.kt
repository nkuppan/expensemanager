package com.naveenapps.expensemanager.core.domain.usecase.settings.theme

import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.Theme

class SaveThemeUseCase(private val repository: com.naveenapps.expensemanager.core.repository.ThemeRepository) {

    suspend operator fun invoke(theme: Theme): Resource<Boolean> {
        return Resource.Success(repository.saveTheme(theme))
    }
}
