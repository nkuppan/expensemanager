package com.naveenapps.expensemanager.domain.usecase.settings.theme

import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.Theme
import com.naveenapps.expensemanager.domain.repository.ThemeRepository
import javax.inject.Inject

class SaveThemeUseCase @Inject constructor(private val repository: ThemeRepository) {

    suspend operator fun invoke(theme: Theme): Resource<Boolean> {
        return Resource.Success(repository.saveTheme(theme))
    }
}