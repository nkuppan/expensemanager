package com.naveenapps.expensemanager.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single

class FeedbackDataStore(private val dataStore: DataStore<Preferences>) {

    private val transactionCreatedCount = intPreferencesKey("transaction_created_count")

    private val feedbackDialogShownKey = booleanPreferencesKey("feedback_dialog_shown")

    suspend fun increaseTransactionCreatedCount() = dataStore.edit { preferences ->
        preferences[transactionCreatedCount] = getTransactionCreatedCount().single() + 1
    }

    fun getTransactionCreatedCount(): Flow<Int> = dataStore.data.map { preferences ->
        preferences[transactionCreatedCount] ?: 0
    }

    suspend fun setFeedbackDialogShown(shown: Boolean) = dataStore.edit { preferences ->
        preferences[feedbackDialogShownKey] = shown
    }

    fun isFeedbackDialogShown(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[feedbackDialogShownKey] ?: false
    }
}