package com.naveenapps.expensemanager.core.repository

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

    fun getNoSpendDays(): Flow<Set<Long>>

    /** Keys of budget alerts already sent ("<budgetId>:<period>:<threshold>"). */
    fun getSentBudgetAlerts(): Flow<Set<String>>

    suspend fun markBudgetAlertSent(key: String)

    /** "yyyyMM" of the monthly recap card the user closed. */
    fun getDismissedRecapMonth(): Flow<String?>

    suspend fun setDismissedRecapMonth(monthKey: String)

    /** True between saving the install's first transaction and Home celebrating it. */
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
