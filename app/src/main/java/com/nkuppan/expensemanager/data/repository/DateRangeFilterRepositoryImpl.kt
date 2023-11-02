package com.nkuppan.expensemanager.data.repository

import android.content.Context
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.data.datastore.SettingsDataStore
import com.nkuppan.expensemanager.data.utils.getThisMonthRange
import com.nkuppan.expensemanager.data.utils.getThisWeekRange
import com.nkuppan.expensemanager.data.utils.getThisYearRange
import com.nkuppan.expensemanager.data.utils.getTodayRange
import com.nkuppan.expensemanager.data.utils.toCompleteDate
import com.nkuppan.expensemanager.data.utils.toDate
import com.nkuppan.expensemanager.data.utils.toDateAndMonth
import com.nkuppan.expensemanager.data.utils.toMonthAndYear
import com.nkuppan.expensemanager.data.utils.toYear
import com.nkuppan.expensemanager.domain.model.DateRangeFilterType
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.repository.DateRangeFilterRepository
import com.nkuppan.expensemanager.domain.usecase.transaction.GroupType
import com.nkuppan.expensemanager.utils.AppCoroutineDispatchers
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

    override fun getDateRangeFilterType(): Flow<DateRangeFilterType> {
        return dataStore.getFilterType()
    }

    override suspend fun getDateRangeFilterRangeName(dateRangeFilterType: DateRangeFilterType): String {
        return when (dateRangeFilterType) {
            DateRangeFilterType.TODAY -> context.getString(R.string.today)
            DateRangeFilterType.THIS_WEEK -> context.getString(R.string.this_week)
            DateRangeFilterType.THIS_MONTH -> context.getString(R.string.this_month)
            DateRangeFilterType.THIS_YEAR -> context.getString(R.string.this_year)
            DateRangeFilterType.CUSTOM -> context.getString(R.string.custom)
            DateRangeFilterType.ALL -> context.getString(R.string.all_time)
        }
    }

    override suspend fun getDateRangeFilterTypeString(dateRangeFilterType: DateRangeFilterType): String {
        return getFilterDateValue(dateRangeFilterType)
    }

    override suspend fun getDateRanges(dateRangeFilterType: DateRangeFilterType): List<Long> {
        return getDateRangeValues(dateRangeFilterType)
    }

    override suspend fun setDateRangeFilterType(dateRangeFilterType: DateRangeFilterType): Resource<Boolean> =
        withContext(dispatcher.io) {
            dataStore.setFilterType(dateRangeFilterType)
            return@withContext Resource.Success(true)
        }

    override suspend fun setCustomDateRanges(customDateRanges: List<Date>): Resource<Boolean> =
        withContext(dispatcher.io)
        {
            dataStore.setCustomFilterStartDate(customDateRanges[0].time)
            dataStore.setCustomFilterEndDate(customDateRanges[1].time)
            return@withContext Resource.Success(true)
        }


    private suspend fun getDateRangeValues(dateRangeFilterType: DateRangeFilterType): List<Long> {
        return when (dateRangeFilterType) {
            DateRangeFilterType.TODAY -> getTodayRange()
            DateRangeFilterType.THIS_WEEK -> getThisWeekRange()
            DateRangeFilterType.THIS_MONTH -> getThisMonthRange()
            DateRangeFilterType.THIS_YEAR -> getThisYearRange()
            DateRangeFilterType.ALL -> listOf(0, Long.MAX_VALUE)
            DateRangeFilterType.CUSTOM -> getCustomRange()
        }
    }

    private suspend fun getCustomRange(): List<Long> {
        return listOf(
            dataStore.getCustomFilterStartDate().first() ?: 0,
            dataStore.getCustomFilterEndDate().first() ?: 0,
        )
    }

    private suspend fun getFilterDateValue(dateRangeFilterType: DateRangeFilterType): String {

        val dateRanges = getDateRangeValues(dateRangeFilterType)
        val fromDate = dateRanges[0].toCompleteDate()
        val toDate = dateRanges[1].toCompleteDate()

        return when (dateRangeFilterType) {
            DateRangeFilterType.TODAY -> fromDate.toCompleteDate()
            DateRangeFilterType.THIS_MONTH -> fromDate.toMonthAndYear()
            DateRangeFilterType.THIS_YEAR -> fromDate.toYear()
            DateRangeFilterType.ALL -> context.getString(R.string.all_time)
            DateRangeFilterType.THIS_WEEK,
            DateRangeFilterType.CUSTOM -> getFormattedDateRangeString(fromDate, toDate)
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
        dateRangeFilterType: DateRangeFilterType
    ): GroupType = withContext(Dispatchers.IO) {

        val dateRanges = getDateRangeValues(dateRangeFilterType)
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

        return@withContext when (dateRangeFilterType) {
            DateRangeFilterType.THIS_YEAR -> {
                GroupType.MONTH
            }

            DateRangeFilterType.THIS_MONTH,
            DateRangeFilterType.THIS_WEEK,
            DateRangeFilterType.TODAY -> {
                GroupType.DATE
            }

            DateRangeFilterType.ALL,
            DateRangeFilterType.CUSTOM -> {
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