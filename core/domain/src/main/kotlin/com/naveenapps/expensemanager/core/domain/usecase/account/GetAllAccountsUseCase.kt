package com.naveenapps.expensemanager.core.domain.usecase.account

import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.repository.AccountRepository
import kotlinx.coroutines.flow.Flow

class GetAllAccountsUseCase(private val repository: AccountRepository) {
    operator fun invoke(): Flow<List<Account>> {
        return repository.getAccounts()
    }
}
