package com.naveenapps.expensemanager.domain.usecase.account

import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.domain.repository.AccountRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class GetAllAccountsUseCase @Inject constructor(private val repository: AccountRepository) {
    suspend operator fun invoke(): Resource<List<Account>> {
        return Resource.Success(repository.getAccounts().firstOrNull() ?: emptyList())
    }
}