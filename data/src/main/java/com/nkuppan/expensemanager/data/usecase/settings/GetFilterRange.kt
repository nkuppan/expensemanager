package com.nkuppan.expensemanager.data.usecase.settings

import com.nkuppan.expensemanager.data.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetFilterRange(private val settingsRepository: SettingsRepository) {

    operator fun invoke(): Flow<String> {
        return settingsRepository.getFilterType().map {
            settingsRepository.getFilterRangeValue(it)
        }
    }
}