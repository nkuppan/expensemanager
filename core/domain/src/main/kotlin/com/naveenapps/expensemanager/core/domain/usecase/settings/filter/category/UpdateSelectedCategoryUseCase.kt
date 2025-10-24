package com.naveenapps.expensemanager.core.domain.usecase.settings.filter.category

import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.SettingsRepository

class UpdateSelectedCategoryUseCase(
    private val settingsRepository: SettingsRepository,
) {

    suspend operator fun invoke(categories: List<String>?): Resource<Boolean> {
        return settingsRepository.setCategories(categories)
    }
}
