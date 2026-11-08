package com.naveenapps.expensemanager.core.data.repository

import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.naveenapps.expensemanager.core.datastore.FeedbackDataStore
import com.naveenapps.expensemanager.core.model.Insight
import com.naveenapps.expensemanager.core.model.InsightKind
import com.naveenapps.expensemanager.core.repository.FeedbackRepository
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

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

    override fun getShareBannerHiddenUntil(): Flow<Long> = feedbackDataStore.getShareBannerHiddenUntil()

    override suspend fun hideShareBanner(until: Long) {
        feedbackDataStore.hideShareBanner(until)
    }

    override fun getInsights(): Flow<List<Insight>> = feedbackDataStore.getInsights().map { stored ->
        stored.map { Insight(InsightKind.fromStored(it.kind), it.title, it.message, it.postedAt) }
    }

    override suspend fun addInsight(kind: InsightKind, title: String, message: String, postedAt: Long) {
        feedbackDataStore.addInsight(kind.name, title, message, postedAt)
    }

    override fun getNotificationCentreOpenedAt(): Flow<Long> = feedbackDataStore.getNotificationCentreOpenedAt()

    override suspend fun setNotificationCentreOpenedAt(time: Long) {
        feedbackDataStore.setNotificationCentreOpenedAt(time)
    }

    override fun getReminderBannerHiddenUntil(): Flow<Long> = feedbackDataStore.getReminderBannerHiddenUntil()

    override fun getReminderBannerDismissCount(): Flow<Int> = feedbackDataStore.getReminderBannerDismissCount()

    override suspend fun dismissReminderBanner(hiddenUntil: Long) {
        feedbackDataStore.dismissReminderBanner(hiddenUntil)
    }

    override fun getNotificationPermissionAsks(): Flow<Int> = feedbackDataStore.getNotificationPermissionAsks()

    override suspend fun recordNotificationPermissionAsk() {
        feedbackDataStore.recordNotificationPermissionAsk()
    }

    override fun getFirstInstallTime(): Long = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
    }.getOrDefault(0L)

    override fun getLastSeenWhatsNew(): Flow<String?> = feedbackDataStore.getLastSeenWhatsNew()

    override suspend fun setLastSeenWhatsNew(version: String) {
        feedbackDataStore.setLastSeenWhatsNew(version)
    }

    // An update moves lastUpdateTime past firstInstallTime; a fresh install leaves them equal.
    override fun isFreshInstall(): Boolean = runCatching {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        info.lastUpdateTime == info.firstInstallTime
    }.getOrDefault(false)

    override fun getSentBudgetAlerts(): Flow<Set<String>> = feedbackDataStore.getSentBudgetAlerts()

    override suspend fun markBudgetAlertSent(key: String) {
        feedbackDataStore.addSentBudgetAlert(key)
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

            1 ->
                transactionCount >= MIN_TRANSACTIONS_BEFORE_REASK &&
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
