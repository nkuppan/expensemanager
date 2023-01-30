package com.nkuppan.expensemanager.data.usecase.settings.filter

import com.nkuppan.expensemanager.core.model.FilterType
import com.nkuppan.expensemanager.core.model.Resource
import com.nkuppan.expensemanager.data.repository.SettingsRepository

class SaveFilterTypeUseCase(private val settingsRepository: SettingsRepository) {

    suspend operator fun invoke(filterType: FilterType): Resource<Boolean> {
        return settingsRepository.setFilterType(filterType)
    }
}