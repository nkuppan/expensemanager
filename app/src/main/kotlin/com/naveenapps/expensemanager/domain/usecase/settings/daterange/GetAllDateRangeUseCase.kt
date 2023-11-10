package com.naveenapps.expensemanager.domain.usecase.settings.daterange

import com.naveenapps.expensemanager.core.model.DateRangeModel
import com.naveenapps.expensemanager.core.model.Resource
import javax.inject.Inject

class GetAllDateRangeUseCase @Inject constructor(
    private val dateRangeFilterRepository: com.naveenapps.expensemanager.core.data.repository.DateRangeFilterRepository
) {
    suspend operator fun invoke(): Resource<List<DateRangeModel>> {
        return dateRangeFilterRepository.getAllDateRanges()
    }
}