package com.nkuppan.expensemanager.domain.repository

import com.nkuppan.expensemanager.domain.model.FilterType
import com.nkuppan.expensemanager.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface SettingsRepository {

    fun getAccountId(): Flow<String?>

    suspend fun setAccountId(accountId: String?): Resource<Boolean>

    fun getFilterType(): Flow<FilterType>

    suspend fun getFilterRangeValue(filterType: FilterType): String

    suspend fun getFilterRange(filterType: FilterType): List<Long>

    suspend fun setFilterType(filterType: FilterType): Resource<Boolean>

    suspend fun setCustomFilterRange(customFilterRange: List<Date>): Resource<Boolean>

    fun isReminderOn(): Flow<Boolean>

    suspend fun setReminderOn(reminder: Boolean): Resource<Boolean>
}