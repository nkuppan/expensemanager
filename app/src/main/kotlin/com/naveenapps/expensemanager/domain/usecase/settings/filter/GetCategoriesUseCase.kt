package com.naveenapps.expensemanager.domain.usecase.settings.filter

import com.naveenapps.expensemanager.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(private val settingsRepository: SettingsRepository) {

    operator fun invoke(): Flow<List<String>?> {
        return settingsRepository.getCategories()
    }
}