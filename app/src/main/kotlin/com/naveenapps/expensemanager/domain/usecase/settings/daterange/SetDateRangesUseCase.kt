package com.naveenapps.expensemanager.domain.usecase.settings.daterange

import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.domain.repository.DateRangeFilterRepository
import java.util.Date
import javax.inject.Inject

class SetDateRangesUseCase @Inject constructor(
    private val dateRangeFilterRepository: DateRangeFilterRepository
) {

    suspend operator fun invoke(customDateRange: List<Date>): Resource<Boolean> {
        return dateRangeFilterRepository.setDateRanges(customDateRange)
    }
}