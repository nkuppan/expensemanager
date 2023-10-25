package com.nkuppan.expensemanager.domain.usecase.settings.filter

import com.nkuppan.expensemanager.domain.model.FilterType
import com.nkuppan.expensemanager.domain.repository.SettingsRepository
import javax.inject.Inject

class GetFilterRangeDateStringUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(filterType: FilterType): String {
        return settingsRepository.getFilterRangeDateString(filterType)
    }
}