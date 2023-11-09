package com.naveenapps.expensemanager.data.repository

import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.data.datastore.SettingsDataStore
import com.naveenapps.expensemanager.domain.repository.SettingsRepository
import com.naveenapps.expensemanager.utils.AppCoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
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
}
