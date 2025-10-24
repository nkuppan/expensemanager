package com.naveenapps.expensemanager.core.domain.usecase.settings.filter.transactiontype

import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.repository.SettingsRepository

class UpdateSelectedTransactionTypesUseCase(
    private val settingsRepository: SettingsRepository,
) {

    suspend operator fun invoke(categoryTypes: List<TransactionType>?): Resource<Boolean> {
        return settingsRepository.setTransactionTypes(categoryTypes)
    }
}
