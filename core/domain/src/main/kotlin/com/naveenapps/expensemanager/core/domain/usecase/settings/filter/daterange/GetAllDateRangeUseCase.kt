package com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange

import com.naveenapps.expensemanager.core.model.DateRangeModel
import com.naveenapps.expensemanager.core.model.Resource
import javax.inject.Inject

class GetAllDateRangeUseCase @Inject constructor(
    private val dateRangeFilterRepository: com.naveenapps.expensemanager.core.repository.DateRangeFilterRepository,
) {
    suspend operator fun invoke(): Resource<List<DateRangeModel>> {
        return dateRangeFilterRepository.getAllDateRanges()
    }
}
