package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.naveenapps.expensemanager.core.model.netIncome
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetIncomeAmountUseCase(
    private val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
) {

    operator fun invoke(): Flow<Double?> {
        return getTransactionWithFilterUseCase.invoke().map { transactions ->
            return@map transactions?.netIncome() ?: 0.0
        }
    }
}
