package com.naveenapps.expensemanager.domain.usecase.settings.theme

import com.naveenapps.expensemanager.core.model.Theme
import com.naveenapps.expensemanager.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentThemeUseCase @Inject constructor(private val repository: ThemeRepository) {

    operator fun invoke(): Flow<Theme> {
        return repository.getSelectedTheme()
    }
}