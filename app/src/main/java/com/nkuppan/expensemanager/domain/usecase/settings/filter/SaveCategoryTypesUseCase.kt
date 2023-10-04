package com.nkuppan.expensemanager.domain.usecase.settings.filter

import com.nkuppan.expensemanager.domain.model.CategoryType
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.repository.SettingsRepository
import javax.inject.Inject

class SaveCategoryTypesUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(categoryTypes: List<CategoryType>?): Resource<Boolean> {
        return settingsRepository.setCategoryTypes(categoryTypes)
    }
}