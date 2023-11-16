package com.naveenapps.expensemanager.core.domain.usecase.settings.account

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFilteredAccountsUseCase @Inject constructor(private val repository: com.naveenapps.expensemanager.core.data.repository.SettingsRepository) {
    operator fun invoke(): Flow<List<String>?> {
        return repository.getAccounts()
    }
}