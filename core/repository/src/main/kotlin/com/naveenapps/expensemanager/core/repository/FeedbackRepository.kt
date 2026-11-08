package com.naveenapps.expensemanager.core.repository

import com.naveenapps.expensemanager.core.model.Insight
import com.naveenapps.expensemanager.core.model.InsightKind
import kotlinx.coroutines.flow.Flow

interface FeedbackRepository {

    suspend fun setTransactionCreated(created: Boolean)

    suspend fun setFeedbackDialogShown(shown: Boolean)

    fun shouldShowFeedbackDialog(): Flow<Boolean>

    /** Number of transactions created over the life of the install (edits not counted). */
    fun getTransactionCreatedCount(): Flow<Int>

    /** Epoch day of the last day something was logged (a transaction or a no-spend day). */
    fun getLastActiveDay(): Flow<Long?>

    /** "Nothing spent today" from the reminder: counts as logging for that day. */
    suspend fun recordNoSpendDay(epochDay: Long)

    /** Home's share banner is hidden until this time (epoch millis); 0 when never hidden. */
    fun getShareBannerHiddenUntil(): Flow<Long>

    suspend fun hideShareBanner(until: Long)

    /** Insights pushed as notifications, newest first (the notification centre's list). */
    fun getInsights(): Flow<List<Insight>>

    /** Saves a pushed insight for the notification centre, whether or not it could be shown. */
    suspend fun addInsight(
        kind: InsightKind,
        title: String,
        message: String,
        postedAt: Long = System.currentTimeMillis(),
    )

    /** When the notification centre was last opened (epoch millis; 0 = never). */
    fun getNotificationCentreOpenedAt(): Flow<Long>

    suspend fun setNotificationCentreOpenedAt(time: Long)

    /** Home's "Daily reminders are off" banner: hidden until this time (epoch millis; 0 = not). */
    fun getReminderBannerHiddenUntil(): Flow<Long>

    fun getReminderBannerDismissCount(): Flow<Int>

    /** "Not now" on the banner: hides it until [hiddenUntil] and counts the dismissal. */
    suspend fun dismissReminderBanner(hiddenUntil: Long)

    /** Times the system notification-permission dialog has been requested. */
    fun getNotificationPermissionAsks(): Flow<Int>

    suspend fun recordNotificationPermissionAsk()

    /** When the app was first installed (epoch millis), or 0 if unknown. */
    fun getFirstInstallTime(): Long

    /** Version of the "What's new" notes last opened from Home (null: never). */
    fun getLastSeenWhatsNew(): Flow<String?>

    suspend fun setLastSeenWhatsNew(version: String)

    /** True when this install has never been updated: everything in it is new to the user. */
    fun isFreshInstall(): Boolean

    /** Keys of budget alerts already sent ("<budgetId>:<period>:<threshold>"). */
    fun getSentBudgetAlerts(): Flow<Set<String>>

    suspend fun markBudgetAlertSent(key: String)

    /** True between saving the install's first transaction and Home asking about reminders. */
    fun isFirstSaveCelebrationPending(): Flow<Boolean>

    suspend fun setFirstSaveCelebrationPending(pending: Boolean)

    /** Home's "Get started" checklist was closed by the user. */
    fun isGettingStartedDismissed(): Flow<Boolean>

    suspend fun setGettingStartedDismissed()

    /**
     * True if the app crashed the last time it ran. Backed by Crashlytics' own per-launch flag,
     * so it resets itself naturally on the next clean run — no extra persisted state needed.
     * Used to suppress the review prompt right after a crash, since asking someone who just hit
     * a fatal error to leave a good review is bad timing.
     */
    fun didCrashOnPreviousExecution(): Boolean
}
