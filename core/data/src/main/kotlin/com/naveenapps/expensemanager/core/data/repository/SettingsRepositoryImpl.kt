package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.datastore.SettingsDataStore
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: SettingsDataStore,
    private val dispatcher: AppCoroutineDispatchers,
) : SettingsRepository {
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

    override fun getDefaultAccount(): Flow<String?> {
        return dataStore.getDefaultAccount()
    }

    override suspend fun setDefaultAccount(accountId: String): Resource<Boolean> =
        withContext(dispatcher.io) {
            dataStore.setDefaultAccount(accountId)
            return@withContext Resource.Success(true)
        }

    override fun getDefaultExpenseCategory(): Flow<String?> {
        return dataStore.getDefaultExpenseCategory()
    }

    override suspend fun setDefaultExpenseCategory(categoryId: String): Resource<Boolean> =
        withContext(dispatcher.io) {
            dataStore.setDefaultExpenseCategory(categoryId)
            return@withContext Resource.Success(true)
        }

    override fun getDefaultIncomeCategory(): Flow<String?> {
        return dataStore.getDefaultIncomeCategory()
    }

    override suspend fun setDefaultIncomeCategory(categoryId: String): Resource<Boolean> =
        withContext(dispatcher.io) {
            dataStore.setDefaultIncomeCategory(categoryId)
            return@withContext Resource.Success(true)
        }
}
