package com.naveenapps.expensemanager.domain.usecase.settings.account

import com.naveenapps.expensemanager.core.model.Resource
import javax.inject.Inject

class UpdateSelectedAccountUseCase @Inject constructor(
    private val settingsRepository: com.naveenapps.expensemanager.core.data.repository.SettingsRepository
) {

    suspend operator fun invoke(accountId: List<String>?): Resource<Boolean> {
        return settingsRepository.setAccounts(accountId)
    }
}