package com.nkuppan.expensemanager.domain.usecase.settings.daterange

import com.nkuppan.expensemanager.domain.model.DateRangeFilterType
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.repository.DateRangeFilterRepository
import com.nkuppan.expensemanager.domain.usecase.settings.filter.SetCustomFilterRangeUseCase
import java.util.Date
import javax.inject.Inject

class SaveFilterTypeUseCase @Inject constructor(
    private val dateRangeFilterRepository: DateRangeFilterRepository,
    private val setCustomFilterRangeUseCase: SetCustomFilterRangeUseCase,
) {

    suspend operator fun invoke(
        dateRangeFilterType: DateRangeFilterType,
        customRanges: List<Date>
    ): Resource<Boolean> {
        return when (val response = setCustomFilterRangeUseCase.invoke(customRanges)) {
            is Resource.Error -> {
                response
            }

            is Resource.Success -> {
                dateRangeFilterRepository.setDateRangeFilterType(dateRangeFilterType)
            }
        }
    }
}