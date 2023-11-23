package com.naveenapps.expensemanager.core.domain.usecase.settings.currency

import com.naveenapps.expensemanager.core.domain.repository.CurrencyRepository
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Currency
import javax.inject.Inject

class GetFormattedAmountUseCase @Inject constructor(
    private val repository: CurrencyRepository
) {
    operator fun invoke(amount: Double, currency: Currency): Amount {
        return repository.getFormattedCurrency(
            Amount(amount = amount, currency = currency)
        )
    }
}