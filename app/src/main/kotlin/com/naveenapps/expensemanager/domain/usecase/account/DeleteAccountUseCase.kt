package com.naveenapps.expensemanager.domain.usecase.account

import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.domain.repository.AccountRepository
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val repository: AccountRepository,
    private val checkAccountValidationUseCase: CheckAccountValidationUseCase
) {

    suspend operator fun invoke(account: Account): Resource<Boolean> {
        return when (val validationResult = checkAccountValidationUseCase(account)) {
            is Resource.Error -> {
                validationResult
            }

            is Resource.Success -> {
                repository.deleteAccount(account)
            }
        }
    }
}