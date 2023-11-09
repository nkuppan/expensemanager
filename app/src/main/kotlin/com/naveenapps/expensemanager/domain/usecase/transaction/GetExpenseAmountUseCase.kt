package com.naveenapps.expensemanager.domain.usecase.transaction

import com.naveenapps.expensemanager.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetExpenseAmountUseCase @Inject constructor(
    private val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase
) {

    operator fun invoke(): Flow<Double?> {
        return getTransactionWithFilterUseCase.invoke().map { transactions ->
            return@map transactions?.filter { it.type == TransactionType.EXPENSE }?.sumOf {
                it.amount
            } ?: 0.0
        }
    }
}