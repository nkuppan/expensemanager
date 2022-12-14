package com.nkuppan.expensemanager.data.usecase.settings.currency

import com.nkuppan.expensemanager.core.model.Currency
import com.nkuppan.expensemanager.data.repository.CurrencyRepository
import kotlinx.coroutines.flow.Flow

class GetCurrencyUseCase(private val repository: CurrencyRepository) {
    operator fun invoke(): Flow<Currency> {
        return repository.getSelectedCurrency()
    }
}