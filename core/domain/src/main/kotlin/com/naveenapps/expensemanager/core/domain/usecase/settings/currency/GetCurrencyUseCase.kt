package com.naveenapps.expensemanager.core.domain.usecase.settings.currency

import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.repository.CurrencyRepository
import kotlinx.coroutines.flow.Flow

class GetCurrencyUseCase(private val repository: CurrencyRepository) {
    operator fun invoke(): Flow<Currency> {
        return repository.getSelectedCurrency()
    }
}
