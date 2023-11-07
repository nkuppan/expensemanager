package com.nkuppan.expensemanager.domain.usecase.settings.daterange

import com.nkuppan.expensemanager.domain.model.DateRangeModel
import com.nkuppan.expensemanager.domain.model.DateRangeType
import com.nkuppan.expensemanager.domain.repository.DateRangeFilterRepository
import javax.inject.Inject

class GetDateRangeByTypeUseCase @Inject constructor(
    private val dateRangeFilterRepository: DateRangeFilterRepository
) {
    suspend operator fun invoke(type: DateRangeType): DateRangeModel {
        return dateRangeFilterRepository.getDateRangeFilterTypeString(type)
    }
}