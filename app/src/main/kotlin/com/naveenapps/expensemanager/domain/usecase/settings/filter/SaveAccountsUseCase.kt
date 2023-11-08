package com.naveenapps.expensemanager.domain.usecase.settings.filter

import com.naveenapps.expensemanager.domain.model.Resource
import com.naveenapps.expensemanager.domain.repository.SettingsRepository
import javax.inject.Inject

class SaveAccountsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(accounts: List<String>?): Resource<Boolean> {
        return settingsRepository.setAccounts(accounts)
    }
}