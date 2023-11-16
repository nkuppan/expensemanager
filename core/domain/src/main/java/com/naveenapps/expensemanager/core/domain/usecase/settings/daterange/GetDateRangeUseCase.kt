package com.naveenapps.expensemanager.core.domain.usecase.settings.daterange

import com.naveenapps.expensemanager.core.data.repository.DateRangeFilterRepository
import com.naveenapps.expensemanager.core.model.DateRangeModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetDateRangeUseCase @Inject constructor(
    private val getDateRangeByTypeUseCase: GetDateRangeByTypeUseCase,
    private val dateRangeFilterRepository: DateRangeFilterRepository
) {

    operator fun invoke(): Flow<DateRangeModel> {
        return combine(
            dateRangeFilterRepository.getDateRangeFilterType(),
            dateRangeFilterRepository.getDateRangeTimeFrame()
        ) { type, _ ->
            getDateRangeByTypeUseCase.invoke(type)
        }
    }
}