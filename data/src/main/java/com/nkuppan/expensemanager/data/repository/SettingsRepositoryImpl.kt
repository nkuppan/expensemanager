package com.nkuppan.expensemanager.data.repository

import android.content.Context
import com.nkuppan.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.nkuppan.expensemanager.core.model.FilterType
import com.nkuppan.expensemanager.core.model.Resource
import com.nkuppan.expensemanager.data.R
import com.nkuppan.expensemanager.data.datastore.SettingsDataStore
import com.nkuppan.expensemanager.data.utils.getDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.*

class SettingsRepositoryImpl(
    private val applicationContext: Context,
    private val dataStore: SettingsDataStore,
    private val dispatcher: AppCoroutineDispatchers
) : SettingsRepository {

    override fun getAccountId(): Flow<String> {
        return dataStore.getAccountId()
    }

    override suspend fun setAccountId(accountId: String?): Resource<Boolean> =
        withContext(dispatcher.io) {
            dataStore.setAccountId(accountId ?: "-1")
            return@withContext Resource.Success(true)
        }

    override fun getFilterType(): Flow<FilterType> {
        return dataStore.getFilterType()
    }

    override suspend fun getFilterRangeValue(filterType: FilterType): String {
        return when (filterType) {
            FilterType.THIS_MONTH -> applicationContext.getString(R.string.this_month)
            FilterType.LAST_MONTH -> applicationContext.getString(R.string.previous_month)
            FilterType.LAST_THREE_MONTH -> applicationContext.getString(R.string.last_three_month)
            FilterType.LAST_SIX_MONTH -> applicationContext.getString(R.string.last_six_month)
            FilterType.LAST_YEAR -> applicationContext.getString(R.string.last_year)
            FilterType.ALL -> applicationContext.getString(R.string.all)
            FilterType.CUSTOM -> applicationContext.getString(R.string.custom)
        }
    }

    override suspend fun getFilterRange(filterType: FilterType): List<Long> {
        return getFilterValue(filterType)
    }

    override suspend fun setFilterType(filterType: FilterType): Resource<Boolean> =
        withContext(dispatcher.io) {
            dataStore.setFilterType(filterType)
            return@withContext Resource.Success(true)
        }

    override fun isReminderOn(): Flow<Boolean> {
        return dataStore.isReminderOn()
    }

    override suspend fun setReminderOn(reminder: Boolean): Resource<Boolean> =
        withContext(dispatcher.io) {
            dataStore.setReminder(reminder)
            return@withContext Resource.Success(true)
        }


    private fun getFilterValue(filterType: FilterType): List<Long> {
        return when (filterType) {
            FilterType.THIS_MONTH -> getThisMonthValues()
            FilterType.LAST_MONTH -> getPreviousMonthValues()
            FilterType.LAST_THREE_MONTH -> getLastThreeMonthValues()
            FilterType.LAST_SIX_MONTH -> getLastSixMonthValues()
            FilterType.LAST_YEAR -> getLastYearValues()
            FilterType.ALL -> listOf(0, 0)
            FilterType.CUSTOM -> getThisMonthValues()
        }
    }

    private fun getThisMonthValues(): List<Long> {

        val startDate = getDateTime()
        startDate.set(Calendar.DAY_OF_MONTH, 1)
        val fromDate = startDate.timeInMillis

        startDate.set(Calendar.DAY_OF_MONTH, startDate.getActualMaximum(Calendar.DAY_OF_MONTH))
        startDate.set(Calendar.HOUR_OF_DAY, 24)
        val toDate = startDate.timeInMillis

        return arrayListOf(fromDate, toDate)
    }

    private fun getPreviousMonthValues(): List<Long> {

        val startDate = getDateTime()
        startDate.add(Calendar.MONTH, -1)
        startDate.set(Calendar.DATE, 1)
        val fromDate = startDate.timeInMillis

        startDate.set(Calendar.DAY_OF_MONTH, startDate.getActualMaximum(Calendar.DAY_OF_MONTH))
        startDate.set(Calendar.HOUR_OF_DAY, 24)
        val toDate = startDate.timeInMillis

        return arrayListOf(fromDate, toDate)
    }

    private fun getLastThreeMonthValues(): List<Long> {

        var previousMonthStartDate = getDateTime()
        previousMonthStartDate.add(Calendar.MONTH, -3)
        previousMonthStartDate.set(Calendar.DATE, 1)
        val fromDate = previousMonthStartDate.timeInMillis

        previousMonthStartDate = getDateTime()
        previousMonthStartDate.set(Calendar.HOUR_OF_DAY, 24)
        previousMonthStartDate.set(
            Calendar.DAY_OF_MONTH,
            previousMonthStartDate.getActualMaximum(Calendar.DAY_OF_MONTH)
        )
        val toDate = previousMonthStartDate.timeInMillis

        return arrayListOf(fromDate, toDate)
    }

    private fun getLastSixMonthValues(): List<Long> {

        var previousMonthStartDate = getDateTime()
        previousMonthStartDate.add(Calendar.MONTH, -6)
        previousMonthStartDate.set(Calendar.DATE, 1)
        val fromDate = previousMonthStartDate.timeInMillis

        previousMonthStartDate = getDateTime()
        previousMonthStartDate.set(Calendar.HOUR_OF_DAY, 24)
        previousMonthStartDate.set(
            Calendar.DAY_OF_MONTH,
            previousMonthStartDate.getActualMaximum(Calendar.DAY_OF_MONTH)
        )
        val toDate = previousMonthStartDate.timeInMillis

        return arrayListOf(fromDate, toDate)
    }

    private fun getLastYearValues(): List<Long> {

        var previousMonthStartDate = getDateTime()
        previousMonthStartDate.add(Calendar.MONTH, -12)
        previousMonthStartDate.set(Calendar.DATE, 1)
        val fromDate = previousMonthStartDate.timeInMillis

        previousMonthStartDate = getDateTime()
        previousMonthStartDate.set(Calendar.HOUR_OF_DAY, 24)
        previousMonthStartDate.set(
            Calendar.DAY_OF_MONTH,
            previousMonthStartDate.getActualMaximum(Calendar.DAY_OF_MONTH)
        )
        val toDate = previousMonthStartDate.timeInMillis

        return arrayListOf(fromDate, toDate)
    }
}
