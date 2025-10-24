package com.naveenapps.expensemanager.core.domain.usecase.settings.filter.account

import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.SettingsRepository

class UpdateSelectedAccountUseCase(
    private val settingsRepository: SettingsRepository,
) {

    suspend operator fun invoke(accountId: List<String>?): Resource<Boolean> {
        return settingsRepository.setAccounts(accountId)
    }
}
