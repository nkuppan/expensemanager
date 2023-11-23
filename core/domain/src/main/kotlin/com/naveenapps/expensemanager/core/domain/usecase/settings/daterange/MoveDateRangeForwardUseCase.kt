package com.naveenapps.expensemanager.core.domain.usecase.settings.daterange

import com.naveenapps.expensemanager.core.domain.repository.DateRangeFilterRepository
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.Resource
import org.joda.time.DateTime
import javax.inject.Inject

class MoveDateRangeForwardUseCase @Inject constructor(
    private val dateRangeFilterRepository: DateRangeFilterRepository
) {

    suspend operator fun invoke(type: DateRangeType): Resource<Boolean> {
        val dateRangeModel = dateRangeFilterRepository.getDateRangeFilterTypeString(type)
        var startTime = DateTime(dateRangeModel.dateRanges[0])
        var endTime = DateTime(dateRangeModel.dateRanges[1])
        startTime = startTime.addRespectiveFrame(type)
        endTime = endTime.addRespectiveFrame(type)
        dateRangeFilterRepository.setDateRanges(
            listOf(startTime.toDate(), endTime.toDate())
        )
        return Resource.Success(true)
    }
}

fun DateTime.addRespectiveFrame(type: DateRangeType): DateTime {
    return when (type) {
        DateRangeType.TODAY -> {
            this.plusDays(1)
        }

        DateRangeType.THIS_WEEK -> {
            this.plusWeeks(1)
        }

        DateRangeType.THIS_MONTH -> {
            this.plusMonths(1)
        }

        DateRangeType.THIS_YEAR -> {
            this.plusYears(1)
        }

        else -> this
    }
}

fun DateTime.minusRespectiveFrame(type: DateRangeType): DateTime {
    return when (type) {
        DateRangeType.TODAY -> {
            this.minusDays(1)
        }

        DateRangeType.THIS_WEEK -> {
            this.minusWeeks(1)
        }

        DateRangeType.THIS_MONTH -> {
            this.minusMonths(1)
        }

        DateRangeType.THIS_YEAR -> {
            this.minusYears(1)
        }

        else -> this
    }
}