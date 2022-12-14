package com.nkuppan.expensemanager.data.repository

import com.nkuppan.expensemanager.core.model.FilterType
import com.nkuppan.expensemanager.core.model.Resource
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    fun getAccountId(): Flow<String>

    suspend fun setAccountId(accountId: String): Resource<Boolean>


    fun getFilterType(): Flow<FilterType>

    suspend fun getFilterRange(filterType: FilterType): List<Long>

    suspend fun setFilterType(filterType: FilterType): Resource<Boolean>

    fun isReminderOn(): Flow<Boolean>

    suspend fun setReminderOn(reminder: Boolean): Resource<Boolean>
}