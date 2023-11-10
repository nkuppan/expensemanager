package com.naveenapps.expensemanager.core.domain.usecase.settings.daterange

import com.naveenapps.expensemanager.core.model.DateRangeModel
import com.naveenapps.expensemanager.core.model.DateRangeType
import javax.inject.Inject

class GetDateRangeByTypeUseCase @Inject constructor(
    private val dateRangeFilterRepository: com.naveenapps.expensemanager.core.data.repository.DateRangeFilterRepository
) {
    suspend operator fun invoke(type: DateRangeType): DateRangeModel {
        return dateRangeFilterRepository.getDateRangeFilterTypeString(type)
    }
}