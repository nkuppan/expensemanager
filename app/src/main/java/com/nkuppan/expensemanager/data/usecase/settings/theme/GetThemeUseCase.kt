package com.nkuppan.expensemanager.data.usecase.settings.theme

import com.nkuppan.expensemanager.domain.model.Theme
import com.nkuppan.expensemanager.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetThemeUseCase @Inject constructor(private val repository: ThemeRepository) {

    operator fun invoke(): Flow<Theme> {
        return repository.getSelectedTheme()
    }
}