package com.naveenapps.expensemanager.core.datastore

import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FeedbackDataStore(private val dataStore: DataStore<Preferences>) {

    private val transactionCreatedCount = intPreferencesKey("transaction_created_count")

    private val feedbackDialogShownKey = booleanPreferencesKey("feedback_dialog_shown")

    // Set when the install's very first transaction is saved; cleared once Home has shown the
    // celebration. A one-shot flag (not "count == 1") so existing users never see it.
    private val firstSaveCelebrationPendingKey = booleanPreferencesKey("first_save_celebration_pending")

    // Home's "Get started" checklist, hidden for good once the user taps its close button.
    private val gettingStartedDismissedKey = booleanPreferencesKey("getting_started_dismissed")

    suspend fun setGettingStartedDismissed() = dataStore.edit { preferences ->
        preferences[gettingStartedDismissedKey] = true
    }

    fun isGettingStartedDismissed(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[gettingStartedDismissedKey] ?: false
    }

    suspend fun setFirstSaveCelebrationPending(pending: Boolean) = dataStore.edit { preferences ->
        preferences[firstSaveCelebrationPendingKey] = pending
    }

    fun isFirstSaveCelebrationPending(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[firstSaveCelebrationPendingKey] ?: false
    }

    // Epoch day (LocalDate.toEpochDay) of the last day the user logged something: a new
    // transaction or a "nothing spent today" confirmation. The reminder skips days already done.
    private val lastActiveDayKey = longPreferencesKey("last_active_day")

    // Recent "nothing spent today" days (epoch days as strings), kept for the logging streak.
    private val noSpendDaysKey = stringSetPreferencesKey("no_spend_days")

    // Budget alerts already sent, as "<budgetId>:<period>:<threshold>" keys, so each budget
    // alerts at most once at 80% and once at 100% per month/year.
    private val sentBudgetAlertsKey = stringSetPreferencesKey("sent_budget_alerts")

    suspend fun addSentBudgetAlert(key: String) = dataStore.edit { preferences ->
        preferences[sentBudgetAlertsKey] = preferences[sentBudgetAlertsKey].orEmpty() + key
    }

    fun getSentBudgetAlerts(): Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[sentBudgetAlertsKey].orEmpty()
    }

    // "yyyyMM" of the monthly recap card the user closed, so it stays closed for that month.
    private val dismissedRecapMonthKey = stringPreferencesKey("dismissed_recap_month")

    suspend fun setDismissedRecapMonth(monthKey: String) = dataStore.edit { preferences ->
        preferences[dismissedRecapMonthKey] = monthKey
    }

    fun getDismissedRecapMonth(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[dismissedRecapMonthKey]
    }

    suspend fun setLastActiveDay(epochDay: Long) = dataStore.edit { preferences ->
        preferences[lastActiveDayKey] = epochDay
    }

    fun getLastActiveDay(): Flow<Long?> = dataStore.data.map { preferences ->
        preferences[lastActiveDayKey]
    }

    suspend fun addNoSpendDay(epochDay: Long) = dataStore.edit { preferences ->
        val days = (preferences[noSpendDaysKey].orEmpty() + epochDay.toString())
            .mapNotNull { it.toLongOrNull() }
            .sortedDescending()
            .take(MAX_NO_SPEND_DAYS)
        preferences[noSpendDaysKey] = days.map { it.toString() }.toSet()
        preferences[lastActiveDayKey] = epochDay
    }

    fun getNoSpendDays(): Flow<Set<Long>> = dataStore.data.map { preferences ->
        preferences[noSpendDaysKey].orEmpty().mapNotNull { it.toLongOrNull() }.toSet()
    }

    suspend fun increaseTransactionCreatedCount() = dataStore.edit { preferences ->
        // Read the current value off the snapshot `edit` already gives us, rather than
        // re-subscribing to dataStore.data (via getTransactionCreatedCount()) from inside the
        // transform — DataStore serializes all access through the same lock the transform is
        // already holding, so a nested read from in here would deadlock, waiting on a write
        // that can't complete until this read return.
        val currentCount = preferences[transactionCreatedCount] ?: 0
        preferences[transactionCreatedCount] = currentCount + 1
    }

    fun getTransactionCreatedCount(): Flow<Int> = dataStore.data.map { preferences ->
        preferences[transactionCreatedCount] ?: 0
    }

    // How many times the in-app review was requested, and when the last request was.
    private val reviewRequestCountKey = intPreferencesKey("review_request_count")
    private val reviewRequestedAtKey = longPreferencesKey("review_requested_at")

    suspend fun setFeedbackDialogShown(shown: Boolean) = dataStore.edit { preferences ->
        if (shown) {
            // Read before writing the flag below. Installs from before the counter existed
            // already had exactly one request (the old flag was set).
            val previous = preferences[reviewRequestCountKey]
                ?: if (preferences[feedbackDialogShownKey] == true) 1 else 0
            preferences[reviewRequestCountKey] = previous + 1
            preferences[reviewRequestedAtKey] = System.currentTimeMillis()
        }
        preferences[feedbackDialogShownKey] = shown
    }

    /** Number of in-app review requests so far (0, 1, 2…). */
    fun getReviewRequestCount(): Flow<Int> = dataStore.data.map { preferences ->
        preferences[reviewRequestCountKey]
            ?: if (preferences[feedbackDialogShownKey] == true) 1 else 0
    }

    /** Epoch millis of the last review request, or 0 if unknown. */
    fun getReviewRequestedAt(): Flow<Long> = dataStore.data.map { preferences ->
        preferences[reviewRequestedAtKey] ?: 0L
    }

    fun isFeedbackDialogShown(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[feedbackDialogShownKey] ?: false
    }

    private companion object {
        // Enough history for any realistic streak display; keeps the stored set small.
        const val MAX_NO_SPEND_DAYS = 120
    }
}
