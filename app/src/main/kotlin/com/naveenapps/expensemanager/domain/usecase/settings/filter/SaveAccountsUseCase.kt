package com.naveenapps.expensemanager.domain.usecase.settings.filter

import com.naveenapps.expensemanager.core.model.Resource
import javax.inject.Inject

class SaveAccountsUseCase @Inject constructor(
    private val settingsRepository: com.naveenapps.expensemanager.core.data.repository.SettingsRepository
) {

    suspend operator fun invoke(accounts: List<String>?): Resource<Boolean> {
        return settingsRepository.setAccounts(accounts)
    }
}