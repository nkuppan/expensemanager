package com.nkuppan.expensemanager.data.usecase.settings.theme

import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.model.Theme
import com.nkuppan.expensemanager.domain.repository.ThemeRepository
import javax.inject.Inject

class SaveThemeUseCase @Inject constructor(private val repository: ThemeRepository) {

    suspend operator fun invoke(theme: Theme): Resource<Boolean> {
        return Resource.Success(repository.saveTheme(theme))
    }
}