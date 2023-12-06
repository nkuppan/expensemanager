package com.naveenapps.expensemanager.core.domain.usecase.account

import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.AccountRepository
import javax.inject.Inject

class UpdateAllAccountUseCase @Inject constructor(
    private val repository: AccountRepository,
    private val checkAccountValidationUseCase: CheckAccountValidationUseCase,
) {

    suspend operator fun invoke(accounts: List<Account>): Resource<Boolean> {

        if (accounts.isEmpty()) {
            return Resource.Error(Exception("Accounts are empty"))
        }

        var failedResponse: Resource.Error? = null

        accounts.forEach {
            when (val response = checkAccountValidationUseCase(it)) {
                is Resource.Error -> failedResponse = response
                is Resource.Success -> Unit
            }
        }

        if (failedResponse != null) {
            return failedResponse as Resource.Error
        }

        return repository.updateAllAccount(accounts)
    }
}
