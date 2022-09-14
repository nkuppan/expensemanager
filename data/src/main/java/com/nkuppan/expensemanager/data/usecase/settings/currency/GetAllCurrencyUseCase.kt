package com.nkuppan.expensemanager.data.usecase.settings.currency

import com.nkuppan.expensemanager.core.model.Currency
import com.nkuppan.expensemanager.data.repository.CurrencyRepository

class GetAllCurrencyUseCase(private val repository: CurrencyRepository) {
    operator fun invoke(): List<Currency> {
        return repository.getAllCurrency()
    }
}