package com.naveenapps.expensemanager.domain.usecase.account

import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.Resource
import javax.inject.Inject

class FindAccountByIdUseCase @Inject constructor(
    private val repository: com.naveenapps.expensemanager.core.data.repository.AccountRepository
) {

    suspend operator fun invoke(accountId: String?): Resource<Account> {

        if (accountId.isNullOrBlank()) {
            return Resource.Error(Exception("Provide valid account id value"))
        }

        return repository.findAccount(accountId)
    }
}