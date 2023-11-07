package com.nkuppan.expensemanager.domain.usecase.settings.daterange

import com.nkuppan.expensemanager.domain.model.DateRangeType
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.repository.DateRangeFilterRepository
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