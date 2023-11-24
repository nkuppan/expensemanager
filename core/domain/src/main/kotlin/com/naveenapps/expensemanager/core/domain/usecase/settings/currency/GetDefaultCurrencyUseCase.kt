package com.naveenapps.expensemanager.core.domain.usecase.settings.currency

import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.repository.CurrencyRepository
import javax.inject.Inject

class GetDefaultCurrencyUseCase @Inject constructor(private val repository: CurrencyRepository) {
    operator fun invoke(): Currency {
        return repository.getDefaultCurrency()
    }
}