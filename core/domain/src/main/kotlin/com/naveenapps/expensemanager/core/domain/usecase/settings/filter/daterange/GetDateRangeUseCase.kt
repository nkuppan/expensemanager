package com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange

import com.naveenapps.expensemanager.core.model.DateRangeModel
import com.naveenapps.expensemanager.core.repository.DateRangeFilterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetDateRangeUseCase(
    private val getDateRangeByTypeUseCase: GetDateRangeByTypeUseCase,
    private val dateRangeFilterRepository: DateRangeFilterRepository,
) {

    operator fun invoke(): Flow<DateRangeModel> {
        return combine(
            dateRangeFilterRepository.getDateRangeFilterType(),
            dateRangeFilterRepository.getDateRangeTimeFrame(),
        ) { type, _ ->
            getDateRangeByTypeUseCase.invoke(type)
        }
    }
}
