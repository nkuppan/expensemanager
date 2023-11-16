package com.naveenapps.expensemanager.core.domain.usecase.settings.filter

import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.Resource
import javax.inject.Inject

class SaveCategoryTypesUseCase @Inject constructor(
    private val settingsRepository: com.naveenapps.expensemanager.core.data.repository.SettingsRepository
) {

    suspend operator fun invoke(categoryTypes: List<CategoryType>?): Resource<Boolean> {
        return settingsRepository.setCategoryTypes(categoryTypes)
    }
}