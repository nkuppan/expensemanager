package com.naveenapps.expensemanager.core.domain.usecase.account

import com.naveenapps.expensemanager.core.model.AccountDeleteImpact
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.AccountRepository

class GetAccountDeleteImpactUseCase(private val repository: AccountRepository) {

    suspend operator fun invoke(accountId: String): Resource<AccountDeleteImpact> = repository.getAccountDeleteImpact(accountId)
}
