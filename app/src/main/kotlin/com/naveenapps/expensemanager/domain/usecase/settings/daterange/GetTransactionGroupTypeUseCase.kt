package com.naveenapps.expensemanager.domain.usecase.settings.daterange

import com.naveenapps.expensemanager.core.data.repository.DateRangeFilterRepository
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.GroupType
import javax.inject.Inject

class GetTransactionGroupTypeUseCase @Inject constructor(
    private val dateRangeFilterRepository: DateRangeFilterRepository
) {

    suspend operator fun invoke(dateRangeType: DateRangeType): GroupType {
        return dateRangeFilterRepository.getTransactionGroupType(dateRangeType)
    }
}