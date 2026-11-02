package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.repository.TransactionRepository

class AddTransactionUseCase(
    private val repository: TransactionRepository,
    private val checkTransactionValidationUseCase: CheckTransactionValidationUseCase,
) {

    suspend operator fun invoke(transaction: Transaction): Resource<Boolean> {
        val validation = checkTransactionValidationUseCase(transaction)
        if (validation is Resource.Error) return validation
        return repository.addTransaction(transaction)
    }
}
