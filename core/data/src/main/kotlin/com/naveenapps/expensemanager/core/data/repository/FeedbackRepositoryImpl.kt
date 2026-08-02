package com.naveenapps.expensemanager.core.data.repository

import android.content.Context
import com.naveenapps.expensemanager.core.datastore.FeedbackDataStore
import com.naveenapps.expensemanager.core.repository.FeedbackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.concurrent.TimeUnit

class FeedbackRepositoryImpl(
    private val context: Context,
    private val feedbackDataStore: FeedbackDataStore
) : FeedbackRepository {

    override suspend fun setTransactionCreated(created: Boolean) {
        feedbackDataStore.increaseTransactionCreatedCount()
    }

    override suspend fun setFeedbackDialogShown(shown: Boolean) {
        feedbackDataStore.setFeedbackDialogShown(shown)
    }

    override fun shouldShowFeedbackDialog(): Flow<Boolean> = combine(
        feedbackDataStore.getTransactionCreatedCount(),
        feedbackDataStore.isFeedbackDialogShown()
    ) { transactionCount, isFeedbackDialogShown ->
        return@combine transactionCount > MIN_TRANSACTIONS_BEFORE_PROMPT &&
            !isFeedbackDialogShown &&
            hasBeenInstalledLongEnough()
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
    }
}