package com.naveenapps.expensemanager.core.domain.usecase.settings.theme

import com.naveenapps.expensemanager.core.model.Theme
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentThemeUseCase @Inject constructor(private val repository: com.naveenapps.expensemanager.core.data.repository.ThemeRepository) {

    operator fun invoke(): Flow<Theme> {
        return repository.getSelectedTheme()
    }
}