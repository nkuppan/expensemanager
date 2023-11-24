package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.datastore.SettingsDataStore
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: SettingsDataStore,
    private val dispatcher: AppCoroutineDispatchers
) : com.naveenapps.expensemanager.core.repository.SettingsRepository {
    override fun getTransactionTypes(): Flow<List<TransactionType>?> {
        return dataStore.getTransactionType()
    }

    override suspend fun setTransactionTypes(transactionTypes: List<TransactionType>?): Resource<Boolean> =
        withContext(dispatcher.io) {
            dataStore.setTransactionType(transactionTypes)
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

    override fun isFilterEnabled(): Flow<Boolean> {
        return dataStore.isFilterEnabled()
    }

    override suspend fun setFilterEnabled(filterEnable: Boolean): Resource<Boolean> =
        withContext(dispatcher.io) {
            dataStore.setFilterEnabled(filterEnable)
            return@withContext Resource.Success(true)
        }

    override fun isPreloaded(): Flow<Boolean> {
        return dataStore.isPreloaded()
    }

    override suspend fun setPreloaded(preloaded: Boolean): Resource<Boolean> =
        withContext(dispatcher.io) {
            dataStore.setPreloaded(preloaded)
            return@withContext Resource.Success(true)
        }

    override fun isOnboardingCompleted(): Flow<Boolean> {
        return dataStore.isOnboardingCompleted()
    }

    override suspend fun setOnboardingCompleted(isOnboardingCompleted: Boolean): Resource<Boolean> =
        withContext(dispatcher.io) {
            dataStore.setOnboardingCompleted(isOnboardingCompleted)
            return@withContext Resource.Success(true)
        }
}
