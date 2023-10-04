package com.nkuppan.expensemanager.domain.usecase.settings.filter

import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.repository.SettingsRepository
import javax.inject.Inject

class SaveCategoriesUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(categories: List<String>?): Resource<Boolean> {
        return settingsRepository.setCategories(categories)
    }
}