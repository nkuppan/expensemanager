package com.naveenapps.expensemanager.domain.usecase.settings.daterange

import com.naveenapps.expensemanager.domain.model.DateRangeType
import com.naveenapps.expensemanager.domain.model.Resource
import com.naveenapps.expensemanager.domain.repository.DateRangeFilterRepository
import java.util.Date
import javax.inject.Inject

class SaveDateRangeUseCase @Inject constructor(
    private val dateRangeFilterRepository: DateRangeFilterRepository,
    private val setDateRangesUseCase: SetDateRangesUseCase,
) {

    suspend operator fun invoke(
        dateRangeType: DateRangeType,
        customRanges: List<Date>
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