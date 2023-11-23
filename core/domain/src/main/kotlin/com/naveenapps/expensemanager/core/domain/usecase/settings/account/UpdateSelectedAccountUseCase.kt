package com.naveenapps.expensemanager.core.domain.usecase.settings.account

import com.naveenapps.expensemanager.core.domain.repository.SettingsRepository
import com.naveenapps.expensemanager.core.model.Resource
import javax.inject.Inject

class UpdateSelectedAccountUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(accountId: List<String>?): Resource<Boolean> {
        return settingsRepository.setAccounts(accountId)
    }
}