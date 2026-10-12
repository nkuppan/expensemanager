package com.naveenapps.expensemanager.core.domain.usecase.account

import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.LastAccountDeletionException
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.AccountRepository

class DeleteAccountUseCase(
    private val repository: AccountRepository,
    private val checkAccountValidationUseCase: CheckAccountValidationUseCase,
) {

    suspend operator fun invoke(account: Account): Resource<Boolean> {
        val validationResult = checkAccountValidationUseCase(account)
        if (validationResult is Resource.Error) {
            return validationResult
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
