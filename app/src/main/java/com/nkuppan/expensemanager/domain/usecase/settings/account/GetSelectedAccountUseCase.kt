package com.nkuppan.expensemanager.domain.usecase.settings.account

import com.nkuppan.expensemanager.domain.model.Account
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.repository.AccountRepository
import com.nkuppan.expensemanager.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetSelectedAccountUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val accountRepository: AccountRepository
) {

    operator fun invoke(): Flow<Account?> {
        return settingsRepository.getAccountId().map {
            it?.let { accountId ->
                when (val response = accountRepository.findAccount(accountId)) {
                    is Resource.Error -> {
                        null
                    }

                    is Resource.Success -> {
                        response.data
                    }
                }
            }
        }
    }
}