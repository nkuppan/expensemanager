package com.naveenapps.expensemanager.core.domain.usecase.account

import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.LastAccountDeletionException
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.AccountRepository

/**
 * Only an account without transactions can be deleted. Deleting one with history would cascade
 * its transactions away, including transfers, without reversing the other account's side, so
 * such accounts must be archived instead ([ArchiveAccountUseCase]).
 */
class DeleteAccountUseCase(
    private val repository: AccountRepository,
    private val checkAccountValidationUseCase: CheckAccountValidationUseCase,
) {

    suspend operator fun invoke(account: Account): Resource<Boolean> {
        val validationResult = checkAccountValidationUseCase(account)
        if (validationResult is Resource.Error) return validationResult

        if (repository.hasTransactions(account.id)) {
            return Resource.Error(AccountHasTransactionsException())
        }

        // Without any account, new transactions fall back to a placeholder id and fail to save.
        return when (val impact = repository.getAccountDeleteImpact(account.id)) {
            is Resource.Error -> impact

            is Resource.Success -> if (impact.data.isLastAccount) {
                Resource.Error(LastAccountDeletionException())
            } else {
                repository.deleteAccount(account)
            }
        }
    }
}

class AccountHasTransactionsException : Exception("This account has transactions, so it can only be archived, not deleted")
