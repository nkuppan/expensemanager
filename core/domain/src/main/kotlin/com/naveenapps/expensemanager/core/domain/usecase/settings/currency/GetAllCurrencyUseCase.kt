package com.naveenapps.expensemanager.core.domain.usecase.settings.currency

import com.naveenapps.expensemanager.core.domain.repository.CurrencyRepository
import com.naveenapps.expensemanager.core.model.Currency
import javax.inject.Inject

class GetAllCurrencyUseCase @Inject constructor(
    private val repository: CurrencyRepository
) {
    operator fun invoke(): List<Currency> {
        return repository.getAllCurrency()
    }
}