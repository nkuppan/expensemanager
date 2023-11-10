package com.naveenapps.expensemanager.domain.usecase.settings.filter

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(private val settingsRepository: com.naveenapps.expensemanager.core.data.repository.SettingsRepository) {

    operator fun invoke(): Flow<List<String>?> {
        return settingsRepository.getCategories()
    }
}