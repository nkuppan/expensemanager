package com.nkuppan.expensemanager.domain.usecase.settings.daterange

import com.nkuppan.expensemanager.domain.model.DateRangeModel
import com.nkuppan.expensemanager.domain.repository.DateRangeFilterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetDateRangeUseCase @Inject constructor(
    private val getDateRangeByTypeUseCase: GetDateRangeByTypeUseCase,
    private val dateRangeFilterRepository: DateRangeFilterRepository
) {

    operator fun invoke(): Flow<DateRangeModel> {
        return dateRangeFilterRepository.getDateRangeFilterType().map { type ->
            getDateRangeByTypeUseCase.invoke(type)
        }
    }
}