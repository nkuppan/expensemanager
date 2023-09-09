package com.nkuppan.expensemanager.data.usecase.account

import com.nkuppan.expensemanager.domain.model.Account
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.repository.AccountRepository
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