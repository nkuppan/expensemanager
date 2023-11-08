package com.naveenapps.expensemanager.domain.usecase.settings.currency

import com.naveenapps.expensemanager.domain.model.Currency
import com.naveenapps.expensemanager.domain.repository.CurrencyRepository
import javax.inject.Inject

class GetAllCurrencyUseCase @Inject constructor(
    private val repository: CurrencyRepository
) {
    operator fun invoke(): List<Currency> {
        return repository.getAllCurrency()
    }
}