package com.naveenapps.expensemanager.core.domain.usecase.settings.filter

import com.naveenapps.expensemanager.core.domain.repository.SettingsRepository
import com.naveenapps.expensemanager.core.model.CategoryType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCategoryTypesUseCase @Inject constructor(private val settingsRepository: SettingsRepository) {

    operator fun invoke(): Flow<List<CategoryType>?> {
        return settingsRepository.getCategoryTypes()
    }
}