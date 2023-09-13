package com.nkuppan.expensemanager.domain.usecase.account

import com.nkuppan.expensemanager.domain.model.Account
import com.nkuppan.expensemanager.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAccountsUseCase @Inject constructor(private val repository: AccountRepository) {
    operator fun invoke(): Flow<List<Account>> {
        return repository.getAccounts()
    }
}