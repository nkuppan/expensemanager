package com.nkuppan.expensemanager.data.usecase.settings.account

import com.nkuppan.expensemanager.core.model.Resource
import com.nkuppan.expensemanager.data.repository.SettingsRepository

class UpdateSelectedAccountUseCase(
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(accountId: String?): Resource<Boolean> {
        return settingsRepository.setAccountId(accountId)
    }
}