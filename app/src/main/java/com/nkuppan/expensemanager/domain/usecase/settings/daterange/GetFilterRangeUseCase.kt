package com.nkuppan.expensemanager.domain.usecase.settings.daterange

import com.nkuppan.expensemanager.domain.model.DateRangeFilterType
import com.nkuppan.expensemanager.domain.repository.DateRangeFilterRepository
import javax.inject.Inject

class GetFilterRangeUseCase @Inject constructor(
    private val dateRangeFilterRepository: DateRangeFilterRepository
) {

    suspend operator fun invoke(dateRangeFilterType: DateRangeFilterType): List<Long> {
        return dateRangeFilterRepository.getDateRanges(dateRangeFilterType)
    }
}