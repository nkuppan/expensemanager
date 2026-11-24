package com.naveenapps.expensemanager.core.domain.usecase.account

import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.AccountRepository

class FindAccountByIdUseCase(private val repository: AccountRepository) {

    suspend operator fun invoke(accountId: String?): Resource<Account> {
        if (accountId.isNullOrBlank()) {
            return Resource.Error(Exception("Provide valid account id value"))
        }

        return repository.findAccount(accountId)
    }
}
