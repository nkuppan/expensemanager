package com.naveenapps.expensemanager.core.domain.usecase.account

import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetAllAccountsUseCase(private val repository: AccountRepository) {
    /**
     * Active accounts by default. Pass [includeArchived] only where archived accounts must still
     * appear: the accounts screen's archived section and filters over past transactions.
     */
    operator fun invoke(includeArchived: Boolean = false): Flow<List<Account>> = repository.getAccounts().map { accounts ->
        if (includeArchived) accounts else accounts.filterNot { it.isArchived }
    }
}
