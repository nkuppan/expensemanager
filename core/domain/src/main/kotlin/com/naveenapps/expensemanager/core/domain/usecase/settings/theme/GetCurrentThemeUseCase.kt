package com.naveenapps.expensemanager.core.domain.usecase.settings.theme

import com.naveenapps.expensemanager.core.model.Theme
import com.naveenapps.expensemanager.core.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow

class GetCurrentThemeUseCase(private val repository: ThemeRepository) {

    operator fun invoke(): Flow<Theme> {
        return repository.getSelectedTheme()
    }
}
