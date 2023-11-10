package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.Transaction
import javax.inject.Inject

class FindTransactionByIdUseCase @Inject constructor(
    private val repository: com.naveenapps.expensemanager.core.data.repository.TransactionRepository
) {
    suspend fun invoke(id: String?): Resource<Transaction> {
        if (id.isNullOrEmpty()) {
            return Resource.Error(KotlinNullPointerException("Id shouldn't be null"))
        }
        return repository.findTransactionById(id)
    }
}