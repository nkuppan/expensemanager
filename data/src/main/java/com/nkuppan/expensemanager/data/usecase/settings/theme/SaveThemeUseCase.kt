package com.nkuppan.expensemanager.data.usecase.settings.theme

import com.nkuppan.expensemanager.core.model.Resource
import com.nkuppan.expensemanager.core.model.Theme
import com.nkuppan.expensemanager.data.repository.ThemeRepository

class SaveThemeUseCase(private val repository: ThemeRepository) {

    suspend operator fun invoke(theme: Theme): Resource<Boolean> {
        return Resource.Success(repository.saveTheme(theme))
    }
}