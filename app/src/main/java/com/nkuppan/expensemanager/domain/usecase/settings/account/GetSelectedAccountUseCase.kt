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

    operator fun invoke(): Flow<List<Account>?> {
        return settingsRepository.getAccounts().map { accounts ->
            return@map buildList<Account> {
                if (accounts != null) {
                    repeat(accounts.size) {
                        val account = accounts[it]
                        when (val response = accountRepository.findAccount(account)) {
                            is Resource.Error -> Unit
                            is Resource.Success -> {
                                add(response.data)
                            }
                        }
                    }
                }
            }
        }
    }
}