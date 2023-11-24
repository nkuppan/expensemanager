package com.naveenapps.expensemanager.core.domain.usecase.settings.daterange

import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.GroupType
import javax.inject.Inject

class GetTransactionGroupTypeUseCase @Inject constructor(
    private val dateRangeFilterRepository: com.naveenapps.expensemanager.core.repository.DateRangeFilterRepository
) {

    suspend operator fun invoke(dateRangeType: DateRangeType): GroupType {
        return dateRangeFilterRepository.getTransactionGroupType(dateRangeType)
    }
}