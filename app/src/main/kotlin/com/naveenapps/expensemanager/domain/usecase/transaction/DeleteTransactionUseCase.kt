package com.naveenapps.expensemanager.domain.usecase.transaction

import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.Transaction
import javax.inject.Inject

class DeleteTransactionUseCase @Inject constructor(
    private val repository: com.naveenapps.expensemanager.core.data.repository.TransactionRepository
) {

    suspend operator fun invoke(transaction: Transaction): Resource<Boolean> {

        if (transaction.id.isBlank()) {
            return Resource.Error(Exception("ID shouldn't be blank"))
        }

        if (transaction.categoryId.isBlank()) {
            return Resource.Error(Exception("Category type shouldn't be blank"))
        }

        if (transaction.fromAccountId.isBlank()) {
            return Resource.Error(Exception("From account shouldn't be blank"))
        }

        return repository.deleteTransaction(transaction)
    }
}