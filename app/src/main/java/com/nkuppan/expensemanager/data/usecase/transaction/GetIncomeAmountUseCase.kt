package com.nkuppan.expensemanager.data.usecase.transaction

import com.nkuppan.expensemanager.domain.model.CategoryType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetIncomeAmountUseCase @Inject constructor(
    private val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase
) {

    operator fun invoke(): Flow<Double?> {
        return getTransactionWithFilterUseCase.invoke().map { transactions ->
            return@map transactions?.sumOf {
                if (it.category.type == CategoryType.INCOME) {
                    it.amount
                } else {
                    0.0
                }
            } ?: 0.0
        }
    }
}