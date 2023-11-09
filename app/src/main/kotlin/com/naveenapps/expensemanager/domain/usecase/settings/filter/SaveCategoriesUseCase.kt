package com.naveenapps.expensemanager.domain.usecase.settings.filter

import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.domain.repository.SettingsRepository
import javax.inject.Inject

class SaveCategoriesUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(categories: List<String>?): Resource<Boolean> {
        return settingsRepository.setCategories(categories)
    }
}