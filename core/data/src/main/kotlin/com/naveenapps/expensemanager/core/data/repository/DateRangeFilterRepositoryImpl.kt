package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.common.utils.getThisMonthRange
import com.naveenapps.expensemanager.core.common.utils.getThisWeekRange
import com.naveenapps.expensemanager.core.common.utils.getThisYearRange
import com.naveenapps.expensemanager.core.common.utils.getTodayRange
import com.naveenapps.expensemanager.core.common.utils.toCompleteDate
import com.naveenapps.expensemanager.core.common.utils.toDate
import com.naveenapps.expensemanager.core.common.utils.toDateAndMonth
import com.naveenapps.expensemanager.core.common.utils.toMonthAndYear
import com.naveenapps.expensemanager.core.common.utils.toYear
import com.naveenapps.expensemanager.core.datastore.DateRangeDataStore
import com.naveenapps.expensemanager.core.model.DateRangeModel
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.GroupType
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.DateRangeFilterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.Date
import kotlin.time.ExperimentalTime

class DateRangeFilterRepositoryImpl(
    private val dataStore: DateRangeDataStore,
    private val dispatcher: AppCoroutineDispatchers,
) : DateRangeFilterRepository {

    override suspend fun getAllDateRanges(): Resource<List<DateRangeModel>> {
        return Resource.Success(
            buildList {
                DateRangeType.entries.forEach {
                    val dateRanges = getCurrentDateRanges(it)
                    add(
                        DateRangeModel(
                            name = getDateRangeFilterRangeName(it),
                            description = getDateRangeName(dateRanges, it),
                            type = it,
                            dateRanges = dateRanges,
                        ),
                    )
                }
            },
        )
    }

    override fun getDateRangeFilterType(): Flow<DateRangeType> {
        return dataStore.getFilterType()
    }

    override fun getDateRangeTimeFrame(): Flow<List<Long>?> {
        return dataStore.getDateRanges()
    }

    override suspend fun getDateRangeFilterRangeName(dateRangeType: DateRangeType): String {
        return when (dateRangeType) {
            DateRangeType.TODAY -> "Today"
            DateRangeType.THIS_WEEK -> "This Week"
            DateRangeType.THIS_MONTH -> "This Month"
            DateRangeType.THIS_YEAR -> "This Year"
            DateRangeType.CUSTOM -> "Custom"
            DateRangeType.ALL -> "All time"
        }
    }

    override suspend fun getDateRangeFilterTypeString(dateRangeType: DateRangeType): DateRangeModel {
        return DateRangeModel(
            name = getDateRangeFilterRangeName(dateRangeType),
            description = getFilterDateValue(dateRangeType),
            type = dateRangeType,
            dateRanges = getOriginalDateRangeValues(dateRangeType),
        )
    }

    override suspend fun setDateRangeFilterType(dateRangeType: DateRangeType): Resource<Boolean> =
        withContext(dispatcher.io) {
            dataStore.setFilterType(dateRangeType)
            return@withContext Resource.Success(true)
        }

    override suspend fun setDateRanges(dateRanges: List<Date>): Resource<Boolean> =
        withContext(dispatcher.io) {
            dataStore.setDateRanges(dateRanges[0].time, dateRanges[1].time)
            return@withContext Resource.Success(true)
        }

    private suspend fun getOriginalDateRangeValues(dateRangeType: DateRangeType): List<Long> {
        val dateRanges = getDateRange()

        if (dateRanges != null) {
            return dateRanges
        }

        return getCurrentDateRanges(dateRangeType)
    }

    private fun getCurrentDateRanges(dateRangeType: DateRangeType): List<Long> {
        return when (dateRangeType) {
            DateRangeType.TODAY -> getTodayRange()
            DateRangeType.THIS_WEEK -> getThisWeekRange()
            DateRangeType.THIS_MONTH -> getThisMonthRange()
            DateRangeType.THIS_YEAR -> getThisYearRange()
            DateRangeType.ALL -> listOf(0, Long.MAX_VALUE)
            DateRangeType.CUSTOM -> listOf(Date().time, Date().time)
        }
    }

    private suspend fun getDateRange(): List<Long>? {
        return dataStore.getDateRanges().firstOrNull()
    }

    private suspend fun getFilterDateValue(dateRangeType: DateRangeType): String {
        val dateRanges = getOriginalDateRangeValues(dateRangeType)
        return getDateRangeName(dateRanges, dateRangeType)
    }

    private fun getDateRangeName(
        dateRanges: List<Long>,
        dateRangeType: DateRangeType,
    ): String {
        val fromDate = dateRanges[0].toCompleteDate()
        val toDate = dateRanges[1].toCompleteDate()

        return when (dateRangeType) {
            DateRangeType.TODAY -> fromDate.toCompleteDate()
            DateRangeType.THIS_MONTH -> fromDate.toMonthAndYear()
            DateRangeType.THIS_YEAR -> fromDate.toYear()
            DateRangeType.ALL -> ""
            DateRangeType.THIS_WEEK,
            DateRangeType.CUSTOM,
            -> getFormattedDateRangeString(fromDate, toDate)
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun getFormattedDateRangeString(fromDate: Date, toDate: Date): String {
        val fromDateTime =
            Instant.fromEpochMilliseconds(fromDate.time).toLocalDateTime(TimeZone.UTC)
        val toDateTime = Instant.fromEpochMilliseconds(toDate.time).toLocalDateTime(TimeZone.UTC)

        return if (fromDateTime.year == toDateTime.year) {
            if (fromDateTime.monthNumber == toDateTime.monthNumber) {
                "${fromDate.toDate()} - ${toDate.toCompleteDate()}"
            } else {
                "${fromDate.toDateAndMonth()} - ${toDate.toCompleteDate()}"
            }
        } else {
            "${fromDate.toCompleteDate()}-${toDate.toCompleteDate()}"
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun getTransactionGroupType(
        dateRangeType: DateRangeType,
    ): GroupType = withContext(dispatcher.computation) {
        val dateRanges = getOriginalDateRangeValues(dateRangeType)
        val fromDateTime =
            Instant.fromEpochMilliseconds(dateRanges[0]).toLocalDateTime(TimeZone.UTC)
        val toDateTime = Instant.fromEpochMilliseconds(dateRanges[1]).toLocalDateTime(TimeZone.UTC)

        var isCrossingYears = false
        var isCrossingMonths = false

        if (fromDateTime.year == toDateTime.year) {
            if (fromDateTime.monthNumber != toDateTime.monthNumber) {
                isCrossingMonths = true
            }
        } else {
            isCrossingYears = true
        }

        return@withContext when (dateRangeType) {
            DateRangeType.THIS_YEAR -> {
                GroupType.MONTH
            }

            DateRangeType.THIS_MONTH,
            DateRangeType.THIS_WEEK,
            DateRangeType.TODAY,
            -> {
                GroupType.DATE
            }

            DateRangeType.ALL,
            DateRangeType.CUSTOM,
            -> {
                if (isCrossingYears) {
                    GroupType.YEAR
                } else if (isCrossingMonths) {
                    GroupType.MONTH
                } else {
                    GroupType.DATE
                }
            }
        }
    }
}
