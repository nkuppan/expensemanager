package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.datastore.FeedbackDataStore
import com.naveenapps.expensemanager.core.repository.FeedbackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class FeedbackRepositoryImpl(
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
        return@combine transactionCount > 5 && !isFeedbackDialogShown
    }
}