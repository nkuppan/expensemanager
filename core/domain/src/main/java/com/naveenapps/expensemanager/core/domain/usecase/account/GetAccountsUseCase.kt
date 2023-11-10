package com.naveenapps.expensemanager.core.domain.usecase.account

import com.naveenapps.expensemanager.core.model.Account
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAccountsUseCase @Inject constructor(private val repository: com.naveenapps.expensemanager.core.data.repository.AccountRepository) {
    operator fun invoke(): Flow<List<Account>> {
        return repository.getAccounts()
    }
}