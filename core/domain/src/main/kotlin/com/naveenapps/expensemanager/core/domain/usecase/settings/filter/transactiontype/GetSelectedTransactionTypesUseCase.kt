package com.naveenapps.expensemanager.core.domain.usecase.settings.filter.transactiontype

import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetSelectedTransactionTypesUseCase(
    private val settingsRepository: SettingsRepository,
) {

    operator fun invoke(): Flow<List<TransactionType>> {
        return settingsRepository.getTransactionTypes().map {
            it ?: emptyList()
        }
    }
}
