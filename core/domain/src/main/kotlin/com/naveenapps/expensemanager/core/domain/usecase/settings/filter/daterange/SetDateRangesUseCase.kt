package com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange

import com.naveenapps.expensemanager.core.model.Resource
import java.util.Date
import javax.inject.Inject

class SetDateRangesUseCase @Inject constructor(
    private val dateRangeFilterRepository: com.naveenapps.expensemanager.core.repository.DateRangeFilterRepository,
) {

    suspend operator fun invoke(customDateRange: List<Date>): Resource<Boolean> {
        return dateRangeFilterRepository.setDateRanges(customDateRange)
    }
}
