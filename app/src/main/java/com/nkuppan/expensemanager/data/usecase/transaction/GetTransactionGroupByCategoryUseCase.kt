package com.nkuppan.expensemanager.data.usecase.transaction

import com.nkuppan.expensemanager.domain.model.Category
import com.nkuppan.expensemanager.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetTransactionGroupByCategoryUseCase @Inject constructor(
    private val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase
) {
    fun invoke(): Flow<Map<Category, List<Transaction>>> {
        return getTransactionWithFilterUseCase.invoke().map { data ->
            if (data?.isNotEmpty() == true) {
                data.groupBy { it.categoryId }
                    .map { it.value[0].category to it.value }.toMap()
            } else {
                emptyMap()
            }
        }
    }
}