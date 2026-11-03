package com.naveenapps.expensemanager.core.domain.usecase.settings.currencyapi

import com.naveenapps.expensemanager.core.repository.CurrencyApiRepository

class FetchLatestExchangeRatesUseCase(
    private val repository: CurrencyApiRepository,
) {
    suspend operator fun invoke(baseCurrencyCode: String): Result<Map<String, Double>> = repository.fetchLatestRates(baseCurrencyCode)
}
