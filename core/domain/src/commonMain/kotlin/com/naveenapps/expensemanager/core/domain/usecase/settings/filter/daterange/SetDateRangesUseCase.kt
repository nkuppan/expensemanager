package com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange

import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.DateRangeFilterRepository
import kotlin.time.Instant

class SetDateRangesUseCase(
    private val dateRangeFilterRepository: DateRangeFilterRepository,
) {

    suspend operator fun invoke(customDateRange: List<Instant>): Resource<Boolean> = dateRangeFilterRepository.setDateRanges(customDateRange)
}
