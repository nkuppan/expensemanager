package com.naveenapps.expensemanager.core.data.repository

import android.content.Context
import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.common.utils.toCompleteDate
import com.naveenapps.expensemanager.core.common.utils.toDate
import com.naveenapps.expensemanager.core.common.utils.toDateAndMonth
import com.naveenapps.expensemanager.core.common.utils.toMonthAndYear
import com.naveenapps.expensemanager.core.common.utils.toYear
import com.naveenapps.expensemanager.core.data.R
import com.naveenapps.expensemanager.core.datastore.SettingsDataStore
import com.naveenapps.expensemanager.core.model.DateRangeModel
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.GroupType
import com.naveenapps.expensemanager.core.model.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import java.util.Date
import javax.inject.Inject

class DateRangeFilterRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: SettingsDataStore,
    private val dispatcher: AppCoroutineDispatchers
) : DateRangeFilterRepository {

    override suspend fun getAllDateRanges(): Resource<List<DateRangeModel>> {
        return Resource.Success(
            buildList {
                DateRangeType.values().forEach {
                    add(
                        DateRangeModel(
                            name = getDateRangeFilterRangeName(it),
                            description = getFilterDateValue(it),
                            type = it,
                            dateRanges = getAllDateRanges(it)
                        )
                    )
                }
            }
        )
    }

    override fun getDateRangeFilterType(): Flow<DateRangeType> {
        return dataStore.getFilterType()
    }

    override suspend fun getDateRangeFilterRangeName(dateRangeType: DateRangeType): String {
        return when (dateRangeType) {
            DateRangeType.TODAY -> context.getString(R.string.today)
            DateRangeType.THIS_WEEK -> context.getString(R.string.this_week)
            DateRangeType.THIS_MONTH -> context.getString(R.string.this_month)
            DateRangeType.THIS_YEAR -> context.getString(R.string.this_year)
            DateRangeType.CUSTOM -> context.getString(R.string.custom)
            DateRangeType.ALL -> context.getString(R.string.all_time)
        }
    }

    override suspend fun getDateRangeFilterTypeString(dateRangeType: DateRangeType): DateRangeModel {
        return DateRangeModel(
            name = getDateRangeFilterRangeName(dateRangeType),
            description = getFilterDateValue(dateRangeType),
            type = dateRangeType,
            dateRanges = getAllDateRanges(dateRangeType)
        )
    }

    override suspend fun getAllDateRanges(dateRangeType: DateRangeType): List<Long> {
        return getDateRangeValues(dateRangeType)
    }

    override suspend fun setDateRangeFilterType(dateRangeType: DateRangeType): Resource<Boolean> =
        withContext(dispatcher.io) {
            dataStore.setFilterType(dateRangeType)
            return@withContext Resource.Success(true)
        }

    override suspend fun setDateRanges(dateRanges: List<Date>): Resource<Boolean> =
        withContext(dispatcher.io)
        {
            dataStore.setDateRangeStartDate(dateRanges[0].time)
            dataStore.setDateRangeEndDate(dateRanges[1].time)
            return@withContext Resource.Success(true)
        }


    private suspend fun getDateRangeValues(dateRangeType: DateRangeType): List<Long> {
        return when (dateRangeType) {
            DateRangeType.TODAY -> com.naveenapps.expensemanager.core.common.utils.getTodayRange()
            DateRangeType.THIS_WEEK -> com.naveenapps.expensemanager.core.common.utils.getThisWeekRange()
            DateRangeType.THIS_MONTH -> com.naveenapps.expensemanager.core.common.utils.getThisMonthRange()
            DateRangeType.THIS_YEAR -> com.naveenapps.expensemanager.core.common.utils.getThisYearRange()
            DateRangeType.ALL -> listOf(0, Long.MAX_VALUE)
            DateRangeType.CUSTOM -> getDateRange()
        }
    }

    private suspend fun getDateRange(): List<Long> {
        val defaultTime = Date().time
        return listOf(
            dataStore.getDateRangeStartDate().first() ?: defaultTime,
            dataStore.getDateRangeEndDate().first() ?: defaultTime,
        )
    }

    private suspend fun getFilterDateValue(dateRangeType: DateRangeType): String {

        val dateRanges = getDateRangeValues(dateRangeType)
        val fromDate = dateRanges[0].toCompleteDate()
        val toDate = dateRanges[1].toCompleteDate()

        return when (dateRangeType) {
            DateRangeType.TODAY -> fromDate.toCompleteDate()
            DateRangeType.THIS_MONTH -> fromDate.toMonthAndYear()
            DateRangeType.THIS_YEAR -> fromDate.toYear()
            DateRangeType.ALL -> ""
            DateRangeType.THIS_WEEK,
            DateRangeType.CUSTOM -> getFormattedDateRangeString(fromDate, toDate)
        }
    }

    private fun getFormattedDateRangeString(fromDate: Date, toDate: Date): String {
        val fromDateTime = DateTime(fromDate)
        val toDateTime = DateTime(toDate)

        return if (fromDateTime.year == toDateTime.year) {
            if (fromDateTime.monthOfYear == toDateTime.monthOfYear) {
                "${fromDate.toDate()} - ${toDate.toCompleteDate()}"
            } else {
                "${fromDate.toDateAndMonth()} - ${toDate.toCompleteDate()}"
            }
        } else {
            "${fromDate.toCompleteDate()}-${toDate.toCompleteDate()}"
        }
    }


    override suspend fun getTransactionGroupType(
        dateRangeType: DateRangeType
    ): GroupType = withContext(Dispatchers.IO) {

        val dateRanges = getDateRangeValues(dateRangeType)
        val fromDate = dateRanges[0]
        val fromDateTime = DateTime(fromDate)
        val toDate = dateRanges[1]
        val toDateTime = DateTime(toDate)

        var isCrossingYears = false
        var isCrossingMonths = false

        if (fromDateTime.year == toDateTime.year) {
            if (fromDateTime.monthOfYear != toDateTime.monthOfYear) {
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
            DateRangeType.TODAY -> {
                GroupType.DATE
            }

            DateRangeType.ALL,
            DateRangeType.CUSTOM -> {
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