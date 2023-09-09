package com.nkuppan.expensemanager.data.usecase.account

import com.nkuppan.expensemanager.domain.model.Account
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.repository.AccountRepository
import javax.inject.Inject

class GetAllAccountsUseCase @Inject constructor(
    private val repository: AccountRepository
) {

    suspend operator fun invoke(): Resource<List<Account>> {
        return repository.getAllAccount()
    }
}