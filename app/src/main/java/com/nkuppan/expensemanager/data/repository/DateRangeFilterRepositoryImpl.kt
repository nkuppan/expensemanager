package com.nkuppan.expensemanager.data.repository

import android.content.Context
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.data.datastore.SettingsDataStore
import com.nkuppan.expensemanager.data.utils.getThisMonthRange
import com.nkuppan.expensemanager.data.utils.getThisWeekRange
import com.nkuppan.expensemanager.data.utils.getThisYearRange
import com.nkuppan.expensemanager.data.utils.getTodayRange
import com.nkuppan.expensemanager.data.utils.toDate
import com.nkuppan.expensemanager.data.utils.toTransactionDate
import com.nkuppan.expensemanager.data.utils.toTransactionMonth
import com.nkuppan.expensemanager.data.utils.toTransactionYearValue
import com.nkuppan.expensemanager.domain.model.DateRangeFilterType
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.repository.DateRangeFilterRepository
import com.nkuppan.expensemanager.utils.AppCoroutineDispatchers
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
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
            DateRangeFilterType.TODAY -> context.getString(R.string.this_month)
            DateRangeFilterType.THIS_WEEK -> context.getString(R.string.this_week)
            DateRangeFilterType.THIS_MONTH -> context.getString(R.string.this_month)
            DateRangeFilterType.THIS_YEAR -> context.getString(R.string.this_year)
            DateRangeFilterType.CUSTOM -> context.getString(R.string.custom)
            DateRangeFilterType.ALL -> context.getString(R.string.all)
        }
    }

    override suspend fun getDateRangeFilterTypeString(dateRangeFilterType: DateRangeFilterType): String {
        return getFilterDateValue(dateRangeFilterType)
    }

    override suspend fun getDateRanges(dateRangeFilterType: DateRangeFilterType): List<Long> {
        return getFilterValue(dateRangeFilterType)
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


    private suspend fun getFilterValue(dateRangeFilterType: DateRangeFilterType): List<Long> {
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

    private fun getFilterDateValue(dateRangeFilterType: DateRangeFilterType): String {
        return when (dateRangeFilterType) {
            DateRangeFilterType.TODAY -> {
                getTodayRange()[0].toDate().toTransactionDate()
            }

            DateRangeFilterType.THIS_WEEK -> {
                "${
                    getThisWeekRange()[0].toDate().toTransactionDate()
                }-${getThisWeekRange()[1].toDate().toTransactionDate()}"
            }

            DateRangeFilterType.THIS_MONTH -> getThisMonthRange()[0].toDate().toTransactionMonth()
            DateRangeFilterType.THIS_YEAR -> getThisYearRange()[0].toDate().toTransactionYearValue()
                .toString()

            DateRangeFilterType.ALL -> context.getString(R.string.all)
            DateRangeFilterType.CUSTOM -> {
                "${
                    getThisWeekRange()[0].toDate().toTransactionDate()
                }-${getThisWeekRange()[1].toDate().toTransactionDate()}"
            }
        }
    }
}