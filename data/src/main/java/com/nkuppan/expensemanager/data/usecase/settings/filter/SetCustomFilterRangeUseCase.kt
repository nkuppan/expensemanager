package com.nkuppan.expensemanager.data.usecase.settings.filter

import com.nkuppan.expensemanager.core.model.Resource
import com.nkuppan.expensemanager.data.repository.SettingsRepository
import java.util.*

class SetCustomFilterRangeUseCase(private val settingsRepository: SettingsRepository) {

    suspend operator fun invoke(customDateRange: List<Date>): Resource<Boolean> {
        return settingsRepository.setCustomFilterRange(customDateRange)
    }
}