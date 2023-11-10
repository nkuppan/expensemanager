package com.naveenapps.expensemanager.domain.usecase.transaction

import com.naveenapps.expensemanager.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetIncomeAmountUseCase @Inject constructor(
    private val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase
) {

    operator fun invoke(): Flow<Double?> {
        return getTransactionWithFilterUseCase.invoke().map { transactions ->
            return@map transactions?.filter { it.type == TransactionType.INCOME }?.sumOf {
                it.amount.amount
            } ?: 0.0
        }
    }
}