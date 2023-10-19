package com.nkuppan.expensemanager.data.repository

import android.content.Context
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.common.utils.AppCoroutineDispatchers
import com.nkuppan.expensemanager.data.datastore.SettingsDataStore
import com.nkuppan.expensemanager.data.utils.getThisMonthRange
import com.nkuppan.expensemanager.data.utils.getThisWeekRange
import com.nkuppan.expensemanager.data.utils.getThisYearRange
import com.nkuppan.expensemanager.data.utils.getTodayRange
import com.nkuppan.expensemanager.data.utils.toDate
import com.nkuppan.expensemanager.data.utils.toTransactionDate
import com.nkuppan.expensemanager.data.utils.toTransactionMonth
import com.nkuppan.expensemanager.data.utils.toTransactionYearValue
import com.nkuppan.expensemanager.domain.model.CategoryType
import com.nkuppan.expensemanager.domain.model.FilterType
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: SettingsDataStore,
    private val dispatcher: AppCoroutineDispatchers
) : SettingsRepository {
    override fun getCategoryTypes(): Flow<List<CategoryType>?> {
        return dataStore.getCategoryTypes()
    }

    override suspend fun setCategoryTypes(categoryTypes: List<CategoryType>?): Resource<Boolean> =
        withContext(dispatcher.io) {
            dataStore.setCategoryTypes(categoryTypes)
            return@withContext Resource.Success(true)
        }

    override fun getAccounts(): Flow<List<String>?> {
        return dataStore.getAccounts()
    }

    override suspend fun setAccounts(accounts: List<String>?): Resource<Boolean> =
        withContext(dispatcher.io) {
            dataStore.setAccounts(accounts)
            return@withContext Resource.Success(true)
        }

    override fun getCategories(): Flow<List<String>?> {
        return dataStore.getCategories()
    }

    override suspend fun setCategories(categories: List<String>?): Resource<Boolean> =
        withContext(dispatcher.io) {
            dataStore.setCategories(categories)
            return@withContext Resource.Success(true)
        }

    override fun getFilterType(): Flow<FilterType> {
        return dataStore.getFilterType()
    }

    override suspend fun getFilterRangeValue(filterType: FilterType): String {
        return when (filterType) {
            FilterType.TODAY -> context.getString(R.string.this_month)
            FilterType.THIS_WEEK -> context.getString(R.string.this_week)
            FilterType.THIS_MONTH -> context.getString(R.string.this_month)
            FilterType.THIS_YEAR -> context.getString(R.string.this_year)
            FilterType.CUSTOM -> context.getString(R.string.custom)
            FilterType.ALL -> context.getString(R.string.all)
        }
    }

    override suspend fun getFilterRangeDateString(filterType: FilterType): String {
        return getFilterDateValue(filterType)
    }

    override suspend fun getFilterRange(filterType: FilterType): List<Long> {
        return getFilterValue(filterType)
    }

    override suspend fun setFilterType(filterType: FilterType): Resource<Boolean> =
        withContext(dispatcher.io) {
            dataStore.setFilterType(filterType)
            return@withContext Resource.Success(true)
        }

    override suspend fun setCustomFilterRange(customFilterRange: List<Date>): Resource<Boolean> =
        withContext(dispatcher.io)
        {
            dataStore.setCustomFilterStartDate(customFilterRange[0].time)
            dataStore.setCustomFilterEndDate(customFilterRange[1].time)
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

    override fun isFilterEnabled(): Flow<Boolean> {
        return dataStore.isFilterEnabled()
    }

    override suspend fun setFilterEnabled(filterEnable: Boolean): Resource<Boolean> =
        withContext(dispatcher.io) {
            dataStore.setFilterEnabled(filterEnable)
            return@withContext Resource.Success(true)
        }


    private suspend fun getFilterValue(filterType: FilterType): List<Long> {
        return when (filterType) {
            FilterType.TODAY -> getTodayRange()
            FilterType.THIS_WEEK -> getThisWeekRange()
            FilterType.THIS_MONTH -> getThisMonthRange()
            FilterType.THIS_YEAR -> getThisYearRange()
            FilterType.ALL -> listOf(0, 0)
            FilterType.CUSTOM -> getCustomRange()
        }
    }


    private fun getFilterDateValue(filterType: FilterType): String {
        return when (filterType) {
            FilterType.TODAY -> {
                getTodayRange()[0].toDate().toTransactionDate()
            }

            FilterType.THIS_WEEK -> {
                "${
                    getThisWeekRange()[0].toDate().toTransactionDate()
                }-${getThisWeekRange()[1].toDate().toTransactionDate()}"
            }

            FilterType.THIS_MONTH -> getThisMonthRange()[0].toDate().toTransactionMonth()
            FilterType.THIS_YEAR -> getThisYearRange()[0].toDate().toTransactionYearValue()
                .toString()

            FilterType.ALL -> context.getString(R.string.all)
            FilterType.CUSTOM -> {
                "${
                    getThisWeekRange()[0].toDate().toTransactionDate()
                }-${getThisWeekRange()[1].toDate().toTransactionDate()}"
            }
        }
    }

    private suspend fun getCustomRange(): List<Long> {
        return listOf(
            dataStore.getCustomFilterStartDate().first() ?: 0,
            dataStore.getCustomFilterEndDate().first() ?: 0,
        )
    }
}
