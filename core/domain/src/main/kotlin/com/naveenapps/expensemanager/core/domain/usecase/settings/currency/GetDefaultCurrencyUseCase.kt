package com.naveenapps.expensemanager.core.domain.usecase.settings.currency

import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.repository.CurrencyRepository

class GetDefaultCurrencyUseCase(private val repository: CurrencyRepository) {
    operator fun invoke(): Currency {
        return repository.getDefaultCurrency()
    }
}
