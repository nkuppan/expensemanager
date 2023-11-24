package com.naveenapps.expensemanager.core.domain.usecase.settings.daterange

import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.Resource
import org.joda.time.DateTime
import javax.inject.Inject

class MoveDateRangeBackwardUseCase @Inject constructor(
    private val dateRangeFilterRepository: com.naveenapps.expensemanager.core.repository.DateRangeFilterRepository
) {

    suspend operator fun invoke(type: DateRangeType): Resource<Boolean> {
        val dateRangeModel = dateRangeFilterRepository.getDateRangeFilterTypeString(type)
        var startTime = DateTime(dateRangeModel.dateRanges[0])
        var endTime = DateTime(dateRangeModel.dateRanges[1])
        startTime = startTime.minusRespectiveFrame(type)
        endTime = endTime.minusRespectiveFrame(type)
        dateRangeFilterRepository.setDateRanges(
            listOf(startTime.toDate(), endTime.toDate())
        )
        return Resource.Success(true)
    }
}