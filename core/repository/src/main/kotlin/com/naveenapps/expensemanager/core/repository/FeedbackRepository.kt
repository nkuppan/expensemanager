package com.naveenapps.expensemanager.core.repository

import kotlinx.coroutines.flow.Flow

interface FeedbackRepository {

    suspend fun setTransactionCreated(created: Boolean)

    suspend fun setFeedbackDialogShown(shown: Boolean)

    fun shouldShowFeedbackDialog(): Flow<Boolean>
}