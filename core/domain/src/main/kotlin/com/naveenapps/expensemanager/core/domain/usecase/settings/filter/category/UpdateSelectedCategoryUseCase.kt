package com.naveenapps.expensemanager.core.domain.usecase.settings.filter.category

import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.SettingsRepository
import javax.inject.Inject

class UpdateSelectedCategoryUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {

    suspend operator fun invoke(categories: List<String>?): Resource<Boolean> {
        return settingsRepository.setCategories(categories)
    }
}
