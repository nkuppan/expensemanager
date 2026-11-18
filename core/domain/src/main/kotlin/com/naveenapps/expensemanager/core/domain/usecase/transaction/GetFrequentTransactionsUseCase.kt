package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.naveenapps.expensemanager.core.model.FrequentTransaction
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.repository.TransactionRepository
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.Flow

/**
 * The expenses and incomes the user has logged repeatedly with identical details in the last
 * [LOOKBACK_DAYS] days, most used first. Transfers are left out: they need a second account and
 * rarely repeat with the same amount.
 */
class GetFrequentTransactionsUseCase(
    private val repository: TransactionRepository,
) {
    fun invoke(now: Long = System.currentTimeMillis()): Flow<List<FrequentTransaction>> = repository.getFrequentTransactions(
        transactionTypes = listOf(TransactionType.EXPENSE, TransactionType.INCOME),
        since = now - TimeUnit.DAYS.toMillis(LOOKBACK_DAYS),
        minCount = MIN_USES,
        limit = MAX_PICKS,
    )

    companion object {
        const val LOOKBACK_DAYS = 90L
        const val MIN_USES = 2
        const val MAX_PICKS = 6
    }
}
