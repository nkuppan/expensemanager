package com.naveenapps.expensemanager.domain.usecase.settings.filter

import com.naveenapps.expensemanager.domain.model.CategoryType
import com.naveenapps.expensemanager.domain.model.Resource
import com.naveenapps.expensemanager.domain.repository.SettingsRepository
import javax.inject.Inject

class SaveCategoryTypesUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(categoryTypes: List<CategoryType>?): Resource<Boolean> {
        return settingsRepository.setCategoryTypes(categoryTypes)
    }
}