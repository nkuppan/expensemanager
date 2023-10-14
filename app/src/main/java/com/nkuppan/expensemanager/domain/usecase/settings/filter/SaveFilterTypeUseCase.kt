package com.nkuppan.expensemanager.domain.usecase.settings.filter

import com.nkuppan.expensemanager.domain.model.FilterType
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.repository.SettingsRepository
import java.util.Date
import javax.inject.Inject

class SaveFilterTypeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val setCustomFilterRangeUseCase: SetCustomFilterRangeUseCase,
) {

    suspend operator fun invoke(
        filterType: FilterType,
        customRanges: List<Date>
    ): Resource<Boolean> {
        return when (val response = setCustomFilterRangeUseCase.invoke(customRanges)) {
            is Resource.Error -> {
                response
            }

            is Resource.Success -> {
                settingsRepository.setFilterType(filterType)
            }
        }
    }
}