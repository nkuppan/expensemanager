package com.naveenapps.expensemanager.core.data.repository

import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.naveenapps.expensemanager.core.datastore.FeedbackDataStore
import com.naveenapps.expensemanager.core.repository.FeedbackRepository
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class FeedbackRepositoryImpl(
    private val context: Context,
    private val feedbackDataStore: FeedbackDataStore,
    private val firebaseCrashlytics: FirebaseCrashlytics,
) : FeedbackRepository {

    override suspend fun setTransactionCreated(created: Boolean) {
        feedbackDataStore.increaseTransactionCreatedCount()
        feedbackDataStore.setLastActiveDay(LocalDate.now().toEpochDay())
    }

    override fun getLastActiveDay(): Flow<Long?> = feedbackDataStore.getLastActiveDay()

    override suspend fun recordNoSpendDay(epochDay: Long) {
        feedbackDataStore.addNoSpendDay(epochDay)
    }

    override fun getNoSpendDays(): Flow<Set<Long>> = feedbackDataStore.getNoSpendDays()

    override fun getSentBudgetAlerts(): Flow<Set<String>> = feedbackDataStore.getSentBudgetAlerts()

    override suspend fun markBudgetAlertSent(key: String) {
        feedbackDataStore.addSentBudgetAlert(key)
    }

    override fun getDismissedRecapMonth(): Flow<String?> = feedbackDataStore.getDismissedRecapMonth()

    override suspend fun setDismissedRecapMonth(monthKey: String) {
        feedbackDataStore.setDismissedRecapMonth(monthKey)
    }

    override suspend fun setFeedbackDialogShown(shown: Boolean) {
        feedbackDataStore.setFeedbackDialogShown(shown)
    }

    override fun didCrashOnPreviousExecution(): Boolean = runCatching { firebaseCrashlytics.didCrashOnPreviousExecution() }.getOrDefault(false)

    override fun getTransactionCreatedCount(): Flow<Int> = feedbackDataStore.getTransactionCreatedCount()

    override fun isFirstSaveCelebrationPending(): Flow<Boolean> = feedbackDataStore.isFirstSaveCelebrationPending()

    override suspend fun setFirstSaveCelebrationPending(pending: Boolean) {
        feedbackDataStore.setFirstSaveCelebrationPending(pending)
    }

    override fun isGettingStartedDismissed(): Flow<Boolean> = feedbackDataStore.isGettingStartedDismissed()

    override suspend fun setGettingStartedDismissed() {
        feedbackDataStore.setGettingStartedDismissed()
    }

    /**
     * First ask: after a few transactions and a few days. One re-ask, much later, once the user
     * is clearly engaged — people's opinion of an app changes as it becomes a habit, and Play
     * itself rate-limits the dialog, so this never turns into nagging. Never after a crash.
     */
    override fun shouldShowFeedbackDialog(): Flow<Boolean> = combine(
        feedbackDataStore.getTransactionCreatedCount(),
        feedbackDataStore.getReviewRequestCount(),
        feedbackDataStore.getReviewRequestedAt(),
    ) { transactionCount, requestCount, lastRequestedAt ->
        if (didCrashOnPreviousExecution()) return@combine false
        when (requestCount) {
            0 -> transactionCount > MIN_TRANSACTIONS_BEFORE_PROMPT && hasBeenInstalledLongEnough()
            1 -> transactionCount >= MIN_TRANSACTIONS_BEFORE_REASK &&
                System.currentTimeMillis() - lastRequestedAt >= TimeUnit.DAYS.toMillis(MIN_DAYS_BEFORE_REASK)
            else -> false
        }
    }

    // Read straight from PackageManager rather than tracking our own first-launch timestamp —
    // it's exactly "days since download", survives app updates, and needs no extra state.
    private fun hasBeenInstalledLongEnough(): Boolean {
        val firstInstallTime = runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
        }.getOrNull() ?: return false
        val minAge = TimeUnit.DAYS.toMillis(MIN_DAYS_SINCE_INSTALL)
        return System.currentTimeMillis() - firstInstallTime >= minAge
    }

    companion object {
        private const val MIN_TRANSACTIONS_BEFORE_PROMPT = 5
        private const val MIN_DAYS_SINCE_INSTALL = 3L
        private const val MIN_TRANSACTIONS_BEFORE_REASK = 30
        private const val MIN_DAYS_BEFORE_REASK = 90L
    }
}
