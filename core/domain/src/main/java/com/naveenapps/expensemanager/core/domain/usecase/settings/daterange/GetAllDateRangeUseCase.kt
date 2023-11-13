package com.naveenapps.expensemanager.core.domain.usecase.settings.daterange

import com.naveenapps.expensemanager.core.data.repository.DateRangeFilterRepository
import com.naveenapps.expensemanager.core.model.DateRangeModel
import com.naveenapps.expensemanager.core.model.Resource
import javax.inject.Inject

class GetAllDateRangeUseCase @Inject constructor(
    private val dateRangeFilterRepository: DateRangeFilterRepository
) {
    suspend operator fun invoke(): Resource<List<DateRangeModel>> {
        return dateRangeFilterRepository.getAllDateRanges()
    }
}