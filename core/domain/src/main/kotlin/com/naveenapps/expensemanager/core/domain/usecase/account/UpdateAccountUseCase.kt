package com.naveenapps.expensemanager.core.domain.usecase.account

import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.AccountRepository

/**
 * Accounts are immutable after creation except for their appearance (icon, color, custom image).
 * A change to any other field is rejected rather than silently dropped, so a caller can never
 * believe it changed a name, type, balance, credit limit or currency.
 */
class UpdateAccountUseCase(
    private val repository: AccountRepository,
    private val checkAccountValidationUseCase: CheckAccountValidationUseCase,
) {

    suspend operator fun invoke(account: Account): Resource<Boolean> {
        val validationResult = checkAccountValidationUseCase(account)
        if (validationResult is Resource.Error) return validationResult

        val existing = when (val response = repository.findAccount(account.id)) {
            is Resource.Error -> return response
            is Resource.Success -> response.data
        }

        if (existing.isArchived) {
            return Resource.Error(Exception("Archived accounts can't be edited"))
        }

        val changedLockedFields = buildList {
            if (account.name != existing.name) add("name")
            if (account.type != existing.type) add("type")
            if (account.amount != existing.amount) add("balance")
            if (account.creditLimit != existing.creditLimit) add("credit limit")
            if (account.currencyCode != existing.currencyCode) add("currency")
        }
        if (changedLockedFields.isNotEmpty()) {
            return Resource.Error(
                Exception(
                    "Only an account's icon and color can be changed " +
                        "(tried to change: ${changedLockedFields.joinToString()})",
                ),
            )
        }

        return repository.updateAccount(account)
    }
}
