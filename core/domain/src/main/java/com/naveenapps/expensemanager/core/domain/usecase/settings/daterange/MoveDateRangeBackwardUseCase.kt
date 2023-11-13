package com.naveenapps.expensemanager.core.domain.usecase.settings.daterange

import com.naveenapps.expensemanager.core.data.repository.DateRangeFilterRepository
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.Resource
import org.joda.time.DateTime
import javax.inject.Inject

class MoveDateRangeBackwardUseCase @Inject constructor(
    private val dateRangeFilterRepository: DateRangeFilterRepository
) {

    suspend operator fun invoke(type: DateRangeType): Resource<Boolean> {
        val dateRangeModel = dateRangeFilterRepository.getDateRangeFilterTypeString(type)
        val startTime = DateTime(dateRangeModel.dateRanges[0])
        val endTime = DateTime(dateRangeModel.dateRanges[1])

        when (type) {
            DateRangeType.TODAY -> {
                startTime.minusDays(1)
                endTime.minusDays(1)
            }

            DateRangeType.THIS_WEEK -> {
                startTime.minusWeeks(1)
                endTime.minusWeeks(1)
            }

            DateRangeType.THIS_MONTH -> {
                startTime.minusMonths(1)
                endTime.minusMonths(1)
            }

            DateRangeType.THIS_YEAR -> {
                startTime.minusYears(1)
                endTime.minusYears(1)
            }

            else -> Unit
        }

        dateRangeFilterRepository.setDateRanges(
            listOf(startTime.toDate(), endTime.toDate())
        )
        return Resource.Success(true)
    }
}