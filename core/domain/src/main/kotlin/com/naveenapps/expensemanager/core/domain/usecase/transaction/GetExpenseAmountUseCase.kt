package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.naveenapps.expensemanager.core.model.netExpense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetExpenseAmountUseCase(
    private val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
) {

    operator fun invoke(): Flow<Double?> {
        return getTransactionWithFilterUseCase.invoke().map { transactions ->
            return@map transactions?.netExpense() ?: 0.0
        }
    }
}
