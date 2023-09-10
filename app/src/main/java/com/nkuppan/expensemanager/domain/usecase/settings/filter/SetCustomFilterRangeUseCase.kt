package com.nkuppan.expensemanager.domain.usecase.settings.filter

import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.repository.SettingsRepository
import java.util.Date
import javax.inject.Inject

class SetCustomFilterRangeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(customDateRange: List<Date>): Resource<Boolean> {
        return settingsRepository.setCustomFilterRange(customDateRange)
    }
}