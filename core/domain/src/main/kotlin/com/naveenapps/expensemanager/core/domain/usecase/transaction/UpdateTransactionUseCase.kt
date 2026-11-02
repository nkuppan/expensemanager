package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.repository.AccountRepository
import com.naveenapps.expensemanager.core.repository.TransactionRepository
import java.util.Calendar
import java.util.Date

class UpdateTransactionUseCase(
    private val repository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val checkTransactionValidationUseCase: CheckTransactionValidationUseCase,
) {

    suspend operator fun invoke(transaction: Transaction): Resource<Boolean> {
        val previous = when (val response = repository.findTransactionById(transaction.id)) {
            is Resource.Error -> return response
            is Resource.Success -> response.data
        }
        if (touchesArchivedAccount(previous)) {
            return Resource.Error(Exception("Transactions of archived accounts can't be edited"))
        }

        val validation = checkTransactionValidationUseCase(transaction)
        if (validation is Resource.Error) return validation

        // A rate belongs to a date: moving a cross-currency transfer to another day must come
        // with a newly entered rate, not the one recorded for the old date.
        val keepsOldRate = transaction.rateSnapshot != null &&
            transaction.rateSnapshot?.id == previous.rateSnapshot?.id
        if (keepsOldRate && !isSameDay(previous.createdOn, transaction.createdOn)) {
            return Resource.Error(
                Exception("The date changed: enter the exchange rate for the new date"),
            )
        }

        return repository.updateTransaction(transaction)
    }

    private suspend fun touchesArchivedAccount(transaction: Transaction): Boolean = listOfNotNull(transaction.fromAccountId, transaction.toAccountId).any { id ->
        (accountRepository.findAccount(id) as? Resource.Success)?.data?.isArchived == true
    }

    private fun isSameDay(first: Date, second: Date): Boolean {
        val a = Calendar.getInstance().apply { time = first }
        val b = Calendar.getInstance().apply { time = second }
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
    }
}
