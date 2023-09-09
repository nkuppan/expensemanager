package com.nkuppan.expensemanager.data.usecase.settings.filter

import com.nkuppan.expensemanager.domain.model.FilterType
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.repository.SettingsRepository
import javax.inject.Inject

class SaveFilterTypeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(filterType: FilterType): Resource<Boolean> {
        return settingsRepository.setFilterType(filterType)
    }
}