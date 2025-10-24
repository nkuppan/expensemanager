package com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange

import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.GroupType
import com.naveenapps.expensemanager.core.repository.DateRangeFilterRepository

class GetTransactionGroupTypeUseCase(
    private val dateRangeFilterRepository: DateRangeFilterRepository,
) {

    suspend operator fun invoke(dateRangeType: DateRangeType): GroupType {
        return dateRangeFilterRepository.getTransactionGroupType(dateRangeType)
    }
}
