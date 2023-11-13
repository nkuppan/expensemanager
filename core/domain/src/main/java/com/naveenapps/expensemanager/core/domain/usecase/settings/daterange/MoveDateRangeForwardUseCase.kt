package com.naveenapps.expensemanager.core.domain.usecase.settings.daterange

import com.naveenapps.expensemanager.core.data.repository.DateRangeFilterRepository
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.Resource
import org.joda.time.DateTime
import javax.inject.Inject

class MoveDateRangeForwardUseCase @Inject constructor(
    private val dateRangeFilterRepository: DateRangeFilterRepository
) {

    suspend operator fun invoke(type: DateRangeType): Resource<Boolean> {
        val dateRangeModel = dateRangeFilterRepository.getDateRangeFilterTypeString(type)
        val startTime = DateTime(dateRangeModel.dateRanges[0])
        val endTime = DateTime(dateRangeModel.dateRanges[1])

        when (type) {
            DateRangeType.TODAY -> {
                startTime.plusDays(1)
                endTime.plusDays(1)
            }

            DateRangeType.THIS_WEEK -> {
                startTime.plusWeeks(1)
                endTime.plusWeeks(1)
            }

            DateRangeType.THIS_MONTH -> {
                startTime.plusMonths(1)
                endTime.plusMonths(1)
            }

            DateRangeType.THIS_YEAR -> {
                startTime.plusYears(1)
                endTime.plusYears(1)
            }

            else -> Unit
        }

        dateRangeFilterRepository.setDateRanges(
            listOf(startTime.toDate(), endTime.toDate())
        )
        return Resource.Success(true)
    }
}