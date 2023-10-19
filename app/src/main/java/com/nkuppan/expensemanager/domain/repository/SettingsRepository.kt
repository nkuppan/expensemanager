package com.nkuppan.expensemanager.domain.repository

import com.nkuppan.expensemanager.domain.model.CategoryType
import com.nkuppan.expensemanager.domain.model.FilterType
import com.nkuppan.expensemanager.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface SettingsRepository {

    fun getCategoryTypes(): Flow<List<CategoryType>?>

    suspend fun setCategoryTypes(categoryTypes: List<CategoryType>?): Resource<Boolean>

    fun getAccounts(): Flow<List<String>?>

    suspend fun setAccounts(accounts: List<String>?): Resource<Boolean>

    fun getCategories(): Flow<List<String>?>

    suspend fun setCategories(categories: List<String>?): Resource<Boolean>

    fun getFilterType(): Flow<FilterType>

    suspend fun setFilterType(filterType: FilterType): Resource<Boolean>

    suspend fun getFilterRangeValue(filterType: FilterType): String

    suspend fun getFilterRangeDateString(filterType: FilterType): String

    suspend fun getFilterRange(filterType: FilterType): List<Long>

    suspend fun setCustomFilterRange(customFilterRange: List<Date>): Resource<Boolean>

    fun isReminderOn(): Flow<Boolean>

    suspend fun setReminderOn(reminder: Boolean): Resource<Boolean>

    fun isFilterEnabled(): Flow<Boolean>

    suspend fun setFilterEnabled(filterEnable: Boolean): Resource<Boolean>
}