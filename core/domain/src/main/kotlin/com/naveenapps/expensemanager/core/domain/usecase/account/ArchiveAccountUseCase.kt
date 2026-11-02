package com.naveenapps.expensemanager.core.domain.usecase.account

import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.AccountRepository

/** Hides an account from lists and pickers (or restores it) while keeping its history. */
class ArchiveAccountUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(accountId: String, archived: Boolean): Resource<Boolean> {
        if (accountId.isBlank()) return Resource.Error(Exception("ID shouldn't be blank"))
        return repository.setAccountArchived(accountId, archived)
    }
}
