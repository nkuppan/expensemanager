package com.nkuppan.expensemanager.domain.usecase.settings.filter

import com.nkuppan.expensemanager.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetFilterTypeNameUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    operator fun invoke(): Flow<String> {
        return settingsRepository.getFilterType().map {
            settingsRepository.getFilterRangeValue(it)
        }
    }
}