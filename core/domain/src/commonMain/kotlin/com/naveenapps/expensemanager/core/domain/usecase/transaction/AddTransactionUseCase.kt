package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.repository.TransactionRepository

class AddTransactionUseCase(
    private val repository: TransactionRepository,
) {

    suspend operator fun invoke(transaction: Transaction): Resource<Boolean> {
        if (transaction.id.isBlank()) {
            return Resource.Error(Exception("ID shouldn't be blank"))
        }

        if (transaction.categoryId.isBlank()) {
            return Resource.Error(Exception("Category type color shouldn't be blank"))
        }

        if (transaction.amount.amount < 0.0) {
            return Resource.Error(Exception("Amount should be greater than 0"))
        }

        return repository.addTransaction(transaction)
    }
}
