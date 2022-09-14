package com.nkuppan.expensemanager.data.usecase.account

import com.nkuppan.expensemanager.core.model.Account
import com.nkuppan.expensemanager.core.model.Resource
import com.nkuppan.expensemanager.data.repository.AccountRepository

class GetAllAccountsUseCase(private val repository: AccountRepository) {

    suspend operator fun invoke(): Resource<List<Account>> {
        return repository.getAllAccount()
    }
}