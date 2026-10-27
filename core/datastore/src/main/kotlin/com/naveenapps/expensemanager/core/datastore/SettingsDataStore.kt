package com.naveenapps.expensemanager.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.naveenapps.expensemanager.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsDataStore(private val dataStore: DataStore<Preferences>) {

    suspend fun setTransactionType(transactionTypes: List<TransactionType>?) = dataStore.edit { preferences ->
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

    suspend fun setOnboardingCompleted(onboardingCompleted: Boolean) = dataStore.edit { preferences ->
        preferences[KEY_IS_ON_BOARDING_COMPLETED] = onboardingCompleted
    }

    fun isOnboardingCompleted(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_IS_ON_BOARDING_COMPLETED] ?: false
    }

    suspend fun setDefaultAccount(defaultAccount: String?) = dataStore.edit { preferences ->
        preferences[KEY_DEFAULT_ACCOUNT] = defaultAccount ?: ""
    }

    fun getDefaultAccount(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[KEY_DEFAULT_ACCOUNT]
    }

    suspend fun setDefaultExpenseCategory(expenseCategory: String?) = dataStore.edit { preferences ->
        preferences[KEY_DEFAULT_EXPENSE_CATEGORY] = expenseCategory ?: ""
    }

    fun getDefaultExpenseCategory(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[KEY_DEFAULT_EXPENSE_CATEGORY]
    }

    suspend fun setDefaultIncomeCategory(expenseCategory: String?) = dataStore.edit { preferences ->
        preferences[KEY_DEFAULT_INCOME_CATEGORY] = expenseCategory ?: ""
    }

    fun getDefaultIncomeCategory(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[KEY_DEFAULT_INCOME_CATEGORY]
    }

    suspend fun setHomeSummaryCompact(compact: Boolean) = dataStore.edit { preferences ->
        preferences[KEY_HOME_SUMMARY_COMPACT] = compact
    }

    fun getHomeSummaryCompact(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_HOME_SUMMARY_COMPACT] ?: false
    }

    suspend fun setAppLockEnabled(enabled: Boolean) = dataStore.edit { preferences ->
        preferences[KEY_APP_LOCK_ENABLED] = enabled
    }

    fun isAppLockEnabled(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_APP_LOCK_ENABLED] ?: false
    }

    suspend fun setScreenshotBlocked(blocked: Boolean) = dataStore.edit { preferences ->
        preferences[KEY_BLOCK_SCREENSHOTS] = blocked
    }

    fun isScreenshotBlocked(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_BLOCK_SCREENSHOTS] ?: false
    }

    suspend fun setAutoOpenCalculator(enabled: Boolean) = dataStore.edit { preferences ->
        preferences[KEY_AUTO_OPEN_CALCULATOR] = enabled
    }

    // Defaults to true: a new transaction opening straight onto the calculator is the
    // existing behaviour, so users who never touch the setting see no change.
    fun isAutoOpenCalculator(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_AUTO_OPEN_CALCULATOR] ?: true
    }

    companion object {
        private val KEY_IS_PRELOAD = booleanPreferencesKey("is_preloaded")
        private val KEY_IS_ON_BOARDING_COMPLETED = booleanPreferencesKey("is_on_boarding_completed")

        private val KEY_SELECTED_ACCOUNTS = stringSetPreferencesKey("selected_accounts")
        private val KEY_SELECTED_CATEGORIES = stringSetPreferencesKey("selected_categories")
        private val KEY_TRANSACTION_TYPES = stringSetPreferencesKey("transaction_types")

        private val KEY_DEFAULT_ACCOUNT = stringPreferencesKey("default_account")
        private val KEY_DEFAULT_EXPENSE_CATEGORY = stringPreferencesKey("default_expense_category")
        private val KEY_DEFAULT_INCOME_CATEGORY = stringPreferencesKey("default_income_category")
        private val KEY_HOME_SUMMARY_COMPACT = booleanPreferencesKey("home_summary_compact")
        private val KEY_APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        private val KEY_BLOCK_SCREENSHOTS = booleanPreferencesKey("block_screenshots")
        private val KEY_AUTO_OPEN_CALCULATOR = booleanPreferencesKey("auto_open_calculator")
    }
}
