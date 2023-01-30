package com.nkuppan.expensemanager.data.usecase.settings.filter

import com.nkuppan.expensemanager.core.model.FilterType
import com.nkuppan.expensemanager.data.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class GetFilterTypeUseCase(private val settingsRepository: SettingsRepository) {

    operator fun invoke(): Flow<FilterType> {
        return settingsRepository.getFilterType()
    }
}