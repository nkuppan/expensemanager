package com.nkuppan.expensemanager.domain.repository

import com.nkuppan.expensemanager.domain.model.CategoryType
import com.nkuppan.expensemanager.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    fun getCategoryTypes(): Flow<List<CategoryType>?>

    suspend fun setCategoryTypes(categoryTypes: List<CategoryType>?): Resource<Boolean>

    fun getAccounts(): Flow<List<String>?>

    suspend fun setAccounts(accounts: List<String>?): Resource<Boolean>

    fun getCategories(): Flow<List<String>?>

    suspend fun setCategories(categories: List<String>?): Resource<Boolean>

    fun isReminderOn(): Flow<Boolean>

    suspend fun setReminderOn(reminder: Boolean): Resource<Boolean>

    fun isFilterEnabled(): Flow<Boolean>

    suspend fun setFilterEnabled(filterEnable: Boolean): Resource<Boolean>
}