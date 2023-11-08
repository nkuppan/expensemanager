package com.naveenapps.expensemanager.domain.usecase.account

import com.naveenapps.expensemanager.domain.model.Account
import com.naveenapps.expensemanager.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAccountsUseCase @Inject constructor(private val repository: AccountRepository) {
    operator fun invoke(): Flow<List<Account>> {
        return repository.getAccounts()
    }
}