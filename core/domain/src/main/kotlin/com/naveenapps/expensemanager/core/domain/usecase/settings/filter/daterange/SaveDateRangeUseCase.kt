package com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange

import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.DateRangeFilterRepository
import java.util.Date

class SaveDateRangeUseCase(
    private val dateRangeFilterRepository: DateRangeFilterRepository,
    private val setDateRangesUseCase: SetDateRangesUseCase,
) {

    suspend operator fun invoke(
        dateRangeType: DateRangeType,
        customRanges: List<Date>,
    ): Resource<Boolean> {
        return when (val response = setDateRangesUseCase.invoke(customRanges)) {
            is Resource.Error -> {
                response
            }

            is Resource.Success -> {
                dateRangeFilterRepository.setDateRangeFilterType(dateRangeType)
            }
        }
    }
}
