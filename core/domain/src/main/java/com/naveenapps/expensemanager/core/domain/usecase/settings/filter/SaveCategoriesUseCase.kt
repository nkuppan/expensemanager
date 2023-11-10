package com.naveenapps.expensemanager.core.domain.usecase.settings.filter

import com.naveenapps.expensemanager.core.model.Resource
import javax.inject.Inject

class SaveCategoriesUseCase @Inject constructor(
    private val settingsRepository: com.naveenapps.expensemanager.core.data.repository.SettingsRepository
) {

    suspend operator fun invoke(categories: List<String>?): Resource<Boolean> {
        return settingsRepository.setCategories(categories)
    }
}