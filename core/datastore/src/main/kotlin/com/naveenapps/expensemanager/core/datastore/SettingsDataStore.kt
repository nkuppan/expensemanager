package com.naveenapps.expensemanager.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.naveenapps.expensemanager.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsDataStore @Inject constructor(private val dataStore: DataStore<Preferences>) {

    suspend fun setTransactionType(transactionTypes: List<TransactionType>?) =
        dataStore.edit { preferences ->
            preferences[KEY_TRANSACTION_TYPES] =
                transactionTypes?.map { it.ordinal.toString() }?.toSet() ?: emptySet()
        }

    fun getTransactionType(): Flow<List<TransactionType>?> = dataStore.data.map { preferences ->
        preferences[KEY_TRANSACTION_TYPES]?.toList()?.map { TransactionType.entries[it.toInt()] }
            ?: emptyList()
    }

    suspend fun setAccounts(accounts: List<String>?) = dataStore.edit { preferences ->
        preferences[KEY_SELECTED_ACCOUNTS] = accounts?.toSet() ?: emptySet()
    }

    fun getAccounts(): Flow<List<String>?> = dataStore.data.map { preferences ->
        preferences[KEY_SELECTED_ACCOUNTS]?.toList() ?: emptyList()
    }

    suspend fun setCategories(categories: List<String>?) = dataStore.edit { preferences ->
        preferences[KEY_SELECTED_CATEGORIES] = categories?.toSet() ?: emptySet()
    }

    fun getCategories(): Flow<List<String>?> = dataStore.data.map { preferences ->
        preferences[KEY_SELECTED_CATEGORIES]?.toList() ?: emptyList()
    }

    suspend fun setPreloaded(preload: Boolean) = dataStore.edit { preferences ->
        preferences[KEY_IS_PRELOAD] = preload
    }

    fun isPreloaded(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_IS_PRELOAD] ?: false
    }

    suspend fun setOnboardingCompleted(onboardingCompleted: Boolean) =
        dataStore.edit { preferences ->
            preferences[KEY_IS_ON_BOARDING_COMPLETED] = onboardingCompleted
        }

    fun isOnboardingCompleted(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_IS_ON_BOARDING_COMPLETED] ?: false
    }

    companion object {
        private val KEY_IS_PRELOAD = booleanPreferencesKey("is_preloaded")
        private val KEY_IS_ON_BOARDING_COMPLETED = booleanPreferencesKey("is_on_boarding_completed")

        private val KEY_SELECTED_ACCOUNTS = stringSetPreferencesKey("selected_accounts")
        private val KEY_SELECTED_CATEGORIES = stringSetPreferencesKey("selected_categories")
        private val KEY_TRANSACTION_TYPES = stringSetPreferencesKey("transaction_types")
    }
}
