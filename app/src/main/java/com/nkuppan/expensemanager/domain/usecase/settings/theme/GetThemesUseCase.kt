package com.nkuppan.expensemanager.domain.usecase.settings.theme

import com.nkuppan.expensemanager.domain.model.Theme
import com.nkuppan.expensemanager.domain.repository.ThemeRepository
import javax.inject.Inject

class GetThemesUseCase @Inject constructor(private val repository: ThemeRepository) {

    operator fun invoke(): List<Theme> {
        return repository.getThemes()
    }
}