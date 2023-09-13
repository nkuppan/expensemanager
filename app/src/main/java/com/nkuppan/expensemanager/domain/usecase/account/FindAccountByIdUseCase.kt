package com.nkuppan.expensemanager.domain.usecase.account

import com.nkuppan.expensemanager.domain.model.Account
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.repository.AccountRepository
import javax.inject.Inject

class FindAccountByIdUseCase @Inject constructor(
    private val repository: AccountRepository
) {

    suspend operator fun invoke(accountId: String?): Resource<Account> {

        if (accountId.isNullOrBlank()) {
            return Resource.Error(Exception("Provide valid account id value"))
        }

        return repository.findAccount(accountId)
    }
}