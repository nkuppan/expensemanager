package com.nkuppan.expensemanager.data.usecase.account

import com.nkuppan.expensemanager.domain.model.Account
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.repository.AccountRepository
import javax.inject.Inject

class GetAccountByIdUseCase @Inject constructor(
    private val repository: AccountRepository
) {

    suspend operator fun invoke(accountId: String?): Resource<Account> {

        if (accountId.isNullOrEmpty()) {
            return Resource.Error(KotlinNullPointerException("ID shouldn't be null"))
        }

        return repository.findAccount(accountId)
    }
}