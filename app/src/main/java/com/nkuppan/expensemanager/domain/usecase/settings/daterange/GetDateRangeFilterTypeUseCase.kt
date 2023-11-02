package com.nkuppan.expensemanager.domain.usecase.settings.daterange

import com.nkuppan.expensemanager.domain.model.DateRangeFilterType
import com.nkuppan.expensemanager.domain.repository.DateRangeFilterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDateRangeFilterTypeUseCase @Inject constructor(
    private val dateRangeFilterRepository: DateRangeFilterRepository
) {

    operator fun invoke(): Flow<DateRangeFilterType> {
        return dateRangeFilterRepository.getDateRangeFilterType()
    }
}