package com.naveenapps.expensemanager.core.domain.usecase.settings.daterange

import com.naveenapps.expensemanager.core.data.repository.DateRangeFilterRepository
import com.naveenapps.expensemanager.core.model.Resource
import java.util.Date
import javax.inject.Inject

class SetDateRangesUseCase @Inject constructor(
    private val dateRangeFilterRepository: DateRangeFilterRepository
) {

    suspend operator fun invoke(customDateRange: List<Date>): Resource<Boolean> {
        return dateRangeFilterRepository.setDateRanges(customDateRange)
    }
}