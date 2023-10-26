package com.nkuppan.expensemanager.domain.usecase.account

import com.nkuppan.expensemanager.domain.model.Account
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.repository.AccountRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class GetAllAccountsUseCase @Inject constructor(private val repository: AccountRepository) {
    suspend operator fun invoke(): Resource<List<Account>> {
        return Resource.Success(repository.getAccounts().firstOrNull() ?: emptyList())
    }
}