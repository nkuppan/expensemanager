package com.nkuppan.expensemanager.domain.usecase.settings.filter

import com.nkuppan.expensemanager.domain.model.CategoryType
import com.nkuppan.expensemanager.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCategoryTypesUseCase @Inject constructor(private val settingsRepository: SettingsRepository) {

    operator fun invoke(): Flow<List<CategoryType>?> {
        return settingsRepository.getCategoryTypes()
    }
}