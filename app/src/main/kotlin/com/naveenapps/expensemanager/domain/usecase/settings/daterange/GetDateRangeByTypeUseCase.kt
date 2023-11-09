package com.naveenapps.expensemanager.domain.usecase.settings.daterange

import com.naveenapps.expensemanager.core.model.DateRangeModel
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.domain.repository.DateRangeFilterRepository
import javax.inject.Inject

class GetDateRangeByTypeUseCase @Inject constructor(
    private val dateRangeFilterRepository: DateRangeFilterRepository
) {
    suspend operator fun invoke(type: DateRangeType): DateRangeModel {
        return dateRangeFilterRepository.getDateRangeFilterTypeString(type)
    }
}