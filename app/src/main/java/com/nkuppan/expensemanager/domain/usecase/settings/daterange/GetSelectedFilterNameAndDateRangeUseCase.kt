package com.nkuppan.expensemanager.domain.usecase.settings.daterange

import com.nkuppan.expensemanager.domain.repository.DateRangeFilterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetSelectedFilterNameAndDateRangeUseCase @Inject constructor(
    private val dateRangeFilterRepository: DateRangeFilterRepository,
    private val getFilterTypeUseCase: GetFilterTypeUseCase,
    private val getFilterRangeDateStringUseCase: GetFilterRangeDateStringUseCase,
) {

    operator fun invoke(): Flow<String> {
        return getFilterTypeUseCase.invoke().map {
            val filterRangeText = getFilterRangeDateStringUseCase.invoke(it)
            val filterNameText = dateRangeFilterRepository.getDateRangeFilterRangeName(it)
            return@map "${filterNameText}(${filterRangeText})"
        }
    }
}