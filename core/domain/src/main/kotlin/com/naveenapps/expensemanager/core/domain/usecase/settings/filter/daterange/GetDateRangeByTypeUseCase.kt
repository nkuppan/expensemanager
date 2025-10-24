package com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange

import com.naveenapps.expensemanager.core.model.DateRangeModel
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.repository.DateRangeFilterRepository

class GetDateRangeByTypeUseCase(
    private val dateRangeFilterRepository: DateRangeFilterRepository,
) {
    suspend operator fun invoke(type: DateRangeType): DateRangeModel {
        return dateRangeFilterRepository.getDateRangeFilterTypeString(type)
    }
}
