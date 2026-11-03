package com.naveenapps.expensemanager.core.domain.usecase.settings.currency

import com.naveenapps.expensemanager.core.domain.usecase.settings.currencyapi.ConvertedTotal
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.repository.CurrencyRepository

class GetFormattedAmountUseCase(private val repository: CurrencyRepository) {
    operator fun invoke(amount: Double, currency: Currency): Amount = repository.getFormattedCurrency(
        Amount(amount = amount, currency = currency),
    )

    /**
     * Formats a converted total. When it couldn't be produced the [Amount]'s text says why
     * instead: which rate is unavailable, or — with conversion disabled — each currency's own
     * sum side by side, so no screen can show an unconverted number as if converted.
     */
    operator fun invoke(total: ConvertedTotal, currency: Currency): Amount {
        val value = total.value
        if (value != null) return invoke(value, currency)
        if (total.conversionDisabled) {
            val shown = total.parts.filter { it.amount != 0.0 }.ifEmpty { total.parts.take(1) }
            return Amount(
                amount = 0.0,
                amountString = shown.joinToString(PER_CURRENCY_SEPARATOR) {
                    repository.getFormattedCurrency(it).amountString.orEmpty()
                },
                currency = currency,
                isPerCurrency = true,
            )
        }
        return Amount(
            amount = 0.0,
            amountString = "Rate unavailable (${total.missingPairs.joinToString()})",
            currency = currency,
            unavailableRates = total.missingPairs,
        )
    }

    companion object {
        const val PER_CURRENCY_SEPARATOR = " · "
    }
}
