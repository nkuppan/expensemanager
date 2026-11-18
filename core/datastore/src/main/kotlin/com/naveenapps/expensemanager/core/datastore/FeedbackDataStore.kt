package com.naveenapps.expensemanager.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class FeedbackDataStore(private val dataStore: DataStore<Preferences>) {

    private val transactionCreatedCount = intPreferencesKey("transaction_created_count")

    private val feedbackDialogShownKey = booleanPreferencesKey("feedback_dialog_shown")

    // Set when the install's very first transaction is saved; cleared once Home has asked about
    // reminders (or found them already allowed). A one-shot flag (not "count == 1") so existing
    // users never see it. The key keeps its old "celebration" name so stored values still apply.
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

    // Local time of day (minutes after midnight) of the most recent saves, oldest first, comma
    // separated and capped at MAX_SAVE_TIMES: what the reminder time is suggested from.
    private val recentSaveMinutesKey = stringPreferencesKey("recent_save_minutes")

    suspend fun recordSaveTime(minuteOfDay: Int) = dataStore.edit { preferences ->
        val times = parseSaveMinutes(preferences[recentSaveMinutesKey]) + minuteOfDay
        preferences[recentSaveMinutesKey] = times.takeLast(MAX_SAVE_TIMES).joinToString(",")
    }

    fun getRecentSaveMinutes(): Flow<List<Int>> = dataStore.data.map { preferences ->
        parseSaveMinutes(preferences[recentSaveMinutesKey])
    }

    private fun parseSaveMinutes(value: String?): List<Int> = value.orEmpty()
        .split(',')
        .mapNotNull { it.trim().toIntOrNull() }
        .filter { it in 0 until MINUTES_PER_DAY }

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

    // Home's share banner stays hidden until this time (epoch millis): 30 days after "close",
    // and for good (Long.MAX_VALUE) once the user has shared.
    private val shareBannerHiddenUntilKey = longPreferencesKey("share_banner_hidden_until")

    suspend fun hideShareBanner(until: Long) = dataStore.edit { preferences ->
        preferences[shareBannerHiddenUntilKey] = until
    }

    fun getShareBannerHiddenUntil(): Flow<Long> = dataStore.data.map { preferences ->
        preferences[shareBannerHiddenUntilKey] ?: 0L
    }

    // Insights pushed as notifications (weekly summary, budget alerts), newest first, kept for the
    // notification centre: a JSON array of {kind, title, message, postedAt}, capped at
    // MAX_INSIGHTS so the preferences file stays small.
    private val insightsKey = stringPreferencesKey("insight_history")

    // When the notification centre was last opened (epoch millis); newer insights light the dot.
    private val notificationCentreOpenedAtKey = longPreferencesKey("notification_centre_opened_at")

    suspend fun addInsight(
        kind: String,
        title: String,
        message: String,
        postedAt: Long,
    ) = dataStore.edit { preferences ->
        val history = runCatching { JSONArray(preferences[insightsKey] ?: "[]") }.getOrDefault(JSONArray())
        val updated = JSONArray().put(
            JSONObject()
                .put("kind", kind)
                .put("title", title)
                .put("message", message)
                .put("postedAt", postedAt),
        )
        for (i in 0 until minOf(history.length(), MAX_INSIGHTS - 1)) updated.put(history.get(i))
        preferences[insightsKey] = updated.toString()
    }

    /** Newest first; entries that can't be read are skipped. */
    fun getInsights(): Flow<List<StoredInsight>> = dataStore.data.map { preferences ->
        val history = runCatching { JSONArray(preferences[insightsKey] ?: "[]") }.getOrDefault(JSONArray())
        (0 until history.length()).mapNotNull { i ->
            runCatching {
                val item = history.getJSONObject(i)
                StoredInsight(
                    kind = item.optString("kind"),
                    title = item.getString("title"),
                    message = item.getString("message"),
                    postedAt = item.getLong("postedAt"),
                )
            }.getOrNull()
        }
    }

    suspend fun setNotificationCentreOpenedAt(time: Long) = dataStore.edit { preferences ->
        preferences[notificationCentreOpenedAtKey] = time
    }

    fun getNotificationCentreOpenedAt(): Flow<Long> = dataStore.data.map { preferences ->
        preferences[notificationCentreOpenedAtKey] ?: 0L
    }

    // Home's "Daily reminders are off" banner: hidden until this time after "Not now", and how
    // many times it was dismissed (it stops asking after a few).
    private val reminderBannerHiddenUntilKey = longPreferencesKey("reminder_banner_hidden_until")
    private val reminderBannerDismissCountKey = intPreferencesKey("reminder_banner_dismiss_count")

    // How many times the app has shown the system notification-permission dialog. After a denial
    // Android may stop showing it; with a count above 0 and no rationale, the app opens Settings.
    private val notificationPermissionAsksKey = intPreferencesKey("notification_permission_asks")

    suspend fun dismissReminderBanner(hiddenUntil: Long) = dataStore.edit { preferences ->
        preferences[reminderBannerHiddenUntilKey] = hiddenUntil
        val dismissed = preferences[reminderBannerDismissCountKey] ?: 0
        preferences[reminderBannerDismissCountKey] = dismissed + 1
    }

    fun getReminderBannerHiddenUntil(): Flow<Long> = dataStore.data.map { preferences ->
        preferences[reminderBannerHiddenUntilKey] ?: 0L
    }

    fun getReminderBannerDismissCount(): Flow<Int> = dataStore.data.map { preferences ->
        preferences[reminderBannerDismissCountKey] ?: 0
    }

    suspend fun recordNotificationPermissionAsk() = dataStore.edit { preferences ->
        val asks = preferences[notificationPermissionAsksKey] ?: 0
        preferences[notificationPermissionAsksKey] = asks + 1
    }

    fun getNotificationPermissionAsks(): Flow<Int> = dataStore.data.map { preferences ->
        preferences[notificationPermissionAsksKey] ?: 0
    }

    // The "What's new" notes the user last opened from Home's bell, by their version (e.g.
    // "1.4.26"); the bell shows a dot while a newer set is unread.
    private val lastSeenWhatsNewKey = stringPreferencesKey("last_seen_whats_new")

    suspend fun setLastSeenWhatsNew(version: String) = dataStore.edit { preferences ->
        preferences[lastSeenWhatsNewKey] = version
    }

    fun getLastSeenWhatsNew(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[lastSeenWhatsNewKey]
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
        private const val MAX_SAVE_TIMES = 20
        private const val MINUTES_PER_DAY = 24 * 60

        // Enough history for any realistic streak display; keeps the stored set small.
        const val MAX_NO_SPEND_DAYS = 120
    }
}

/** One saved insight as stored; the repository maps it to the domain model. */
data class StoredInsight(
    val kind: String,
    val title: String,
    val message: String,
    val postedAt: Long,
)

/** How many insights the notification centre keeps; older ones roll off. */
const val MAX_INSIGHTS = 50
