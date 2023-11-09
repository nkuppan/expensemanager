package com.naveenapps.expensemanager.domain.usecase.settings.daterange

import com.naveenapps.expensemanager.core.model.DateRangeModel
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.domain.repository.DateRangeFilterRepository
import javax.inject.Inject

class GetAllDateRangeUseCase @Inject constructor(
    private val dateRangeFilterRepository: DateRangeFilterRepository
) {
    suspend operator fun invoke(): Resource<List<DateRangeModel>> {
        return dateRangeFilterRepository.getAllDateRanges()
    }
}