package com.naveenapps.expensemanager.core.domain.usecase.settings.currency

import com.naveenapps.expensemanager.core.model.Currency
import javax.inject.Inject

class GetAllCurrencyUseCase @Inject constructor(
    private val repository: com.naveenapps.expensemanager.core.data.repository.CurrencyRepository
) {
    operator fun invoke(): List<Currency> {
        return repository.getAllCurrency()
    }
}