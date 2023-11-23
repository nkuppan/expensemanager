package com.naveenapps.expensemanager.core.domain.usecase.settings.filter

import com.naveenapps.expensemanager.core.domain.repository.SettingsRepository
import com.naveenapps.expensemanager.core.model.Resource
import javax.inject.Inject

class SaveAccountsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(accounts: List<String>?): Resource<Boolean> {
        return settingsRepository.setAccounts(accounts)
    }
}