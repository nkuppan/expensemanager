package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.repository.AccountRepository
import com.naveenapps.expensemanager.core.repository.TransactionRepository

class DeleteTransactionUseCase(
    private val repository: TransactionRepository,
    private val accountRepository: AccountRepository,
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

        val touchesArchived = listOfNotNull(transaction.fromAccountId, transaction.toAccountId)
            .any { id ->
                (accountRepository.findAccount(id) as? Resource.Success)?.data?.isArchived == true
            }
        if (touchesArchived) {
            return Resource.Error(Exception("Transactions of archived accounts can't be deleted"))
        }

        return repository.deleteTransaction(transaction)
    }
}
