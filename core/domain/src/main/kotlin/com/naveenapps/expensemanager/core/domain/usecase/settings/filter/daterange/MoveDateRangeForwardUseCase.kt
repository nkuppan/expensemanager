package com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange

import com.naveenapps.expensemanager.core.common.utils.toCompleteDate
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.DateRangeFilterRepository
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import javax.inject.Inject

class MoveDateRangeForwardUseCase @Inject constructor(
    private val dateRangeFilterRepository: DateRangeFilterRepository,
) {

    suspend operator fun invoke(type: DateRangeType): Resource<Boolean> {
        val dateRangeModel = dateRangeFilterRepository.getDateRangeFilterTypeString(type)
        var startTime = Instant.fromEpochMilliseconds(dateRangeModel.dateRanges[0])
        var endTime = Instant.fromEpochMilliseconds(dateRangeModel.dateRanges[1])
        startTime = startTime.addRespectiveFrame(type)
        endTime = endTime.addRespectiveFrame(type)
        dateRangeFilterRepository.setDateRanges(
            listOf(
                startTime.toEpochMilliseconds().toCompleteDate(),
                endTime.toEpochMilliseconds().toCompleteDate()
            ),
        )
        return Resource.Success(true)
    }
}

fun Instant.addRespectiveFrame(type: DateRangeType): Instant {
    return when (type) {
        DateRangeType.TODAY -> {
            this.plus(1, DateTimeUnit.DAY, TimeZone.UTC)
        }

        DateRangeType.THIS_WEEK -> {
            this.plus(1, DateTimeUnit.WEEK, TimeZone.UTC)
        }

        DateRangeType.THIS_MONTH -> {
            this.plus(1, DateTimeUnit.MONTH, TimeZone.UTC)
        }

        DateRangeType.THIS_YEAR -> {
            this.plus(1, DateTimeUnit.YEAR, TimeZone.UTC)
        }

        else -> this
    }
}

fun Instant.minusRespectiveFrame(type: DateRangeType): Instant {
    return when (type) {
        DateRangeType.TODAY -> {
            this.minus(1, DateTimeUnit.DAY, TimeZone.UTC)
        }

        DateRangeType.THIS_WEEK -> {
            this.minus(1, DateTimeUnit.WEEK, TimeZone.UTC)
        }

        DateRangeType.THIS_MONTH -> {
            this.minus(1, DateTimeUnit.MONTH, TimeZone.UTC)
        }

        DateRangeType.THIS_YEAR -> {
            this.minus(1, DateTimeUnit.YEAR, TimeZone.UTC)
        }

        else -> this
    }
}
