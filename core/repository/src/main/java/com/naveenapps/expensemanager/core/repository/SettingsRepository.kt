package com.naveenapps.expensemanager.core.repository

import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    fun getTransactionTypes(): Flow<List<TransactionType>?>

    suspend fun setTransactionTypes(transactionTypes: List<TransactionType>?): Resource<Boolean>

    fun getAccounts(): Flow<List<String>?>

    suspend fun setAccounts(accounts: List<String>?): Resource<Boolean>

    fun getCategories(): Flow<List<String>?>

    suspend fun setCategories(categories: List<String>?): Resource<Boolean>

    fun isFilterEnabled(): Flow<Boolean>

    suspend fun setFilterEnabled(filterEnable: Boolean): Resource<Boolean>

    fun isPreloaded(): Flow<Boolean>

    suspend fun setPreloaded(preloaded: Boolean): Resource<Boolean>

    fun isOnboardingCompleted(): Flow<Boolean>

    suspend fun setOnboardingCompleted(isOnboardingCompleted: Boolean): Resource<Boolean>
}