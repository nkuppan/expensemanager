package com.naveenapps.expensemanager.domain.usecase.settings.account

import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateSelectedAccountUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(accountId: List<String>?): Resource<Boolean> {
        return settingsRepository.setAccounts(accountId)
    }
}