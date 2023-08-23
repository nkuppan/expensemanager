package com.nkuppan.expensemanager.data.usecase.transaction

import com.nkuppan.expensemanager.core.model.Resource
import com.nkuppan.expensemanager.core.model.Transaction
import com.nkuppan.expensemanager.data.repository.TransactionRepository
import javax.inject.Inject

class GetTransactionByIdUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend fun invoke(id: String?): Resource<Transaction> {
        if (id.isNullOrEmpty()) {
            return Resource.Error(KotlinNullPointerException("Id shouldn't be null"))
        }
        return repository.findTransactionById(id)
    }
}