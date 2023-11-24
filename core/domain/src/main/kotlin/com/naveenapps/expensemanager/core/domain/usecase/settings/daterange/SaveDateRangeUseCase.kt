package com.naveenapps.expensemanager.core.domain.usecase.settings.daterange

import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.Resource
import java.util.Date
import javax.inject.Inject

class SaveDateRangeUseCase @Inject constructor(
    private val dateRangeFilterRepository: com.naveenapps.expensemanager.core.repository.DateRangeFilterRepository,
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