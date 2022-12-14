package com.nkuppan.expensemanager.data.usecase.settings.theme

import com.nkuppan.expensemanager.core.model.Theme
import com.nkuppan.expensemanager.data.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow

class GetThemeUseCase(private val repository: ThemeRepository) {

    operator fun invoke(): Flow<Theme> {
        return repository.getSelectedTheme()
    }
}