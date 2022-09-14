package com.nkuppan.expensemanager.data.usecase.transaction

import com.nkuppan.expensemanager.core.model.Category
import com.nkuppan.expensemanager.core.model.Resource
import com.nkuppan.expensemanager.core.model.Transaction

class GetTransactionGroupByCategoryUseCase(
    private val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase
) {
    suspend fun invoke(): Resource<Map<Category, List<Transaction>>> {
        return when (val response = getTransactionWithFilterUseCase.invoke()) {
            is Resource.Error -> {
                Resource.Error(response.exception)
            }
            is Resource.Success -> {
                val data = response.data

                Resource.Success(
                    if (data.isNotEmpty()) {
                        data.groupBy { it.categoryId }
                            .map { it.value[0].category to it.value }.toMap()
                    } else {
                        emptyMap()
                    }
                )
            }
        }
    }
}