package com.nkuppan.expensemanager.data.usecase.account

import com.nkuppan.expensemanager.core.model.Account
import com.nkuppan.expensemanager.core.model.Resource
import com.nkuppan.expensemanager.data.repository.AccountRepository

class GetAccountByIdUseCase(private val repository: AccountRepository) {

    suspend operator fun invoke(accountId: String?): Resource<Account> {

        if (accountId.isNullOrEmpty()) {
            return Resource.Error(KotlinNullPointerException("ID shouldn't be null"))
        }

        return repository.findAccount(accountId)
    }
}