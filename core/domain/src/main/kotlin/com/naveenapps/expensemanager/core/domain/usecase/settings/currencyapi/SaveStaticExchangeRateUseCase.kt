package com.naveenapps.expensemanager.core.domain.usecase.settings.currencyapi

import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.StaticExchangeRate
import com.naveenapps.expensemanager.core.repository.ExchangeRateRepository

/**
 * Saves the user's fixed rate for a pair. A pair has one record, so saving replaces whatever
 * exists for it in either direction; [replacing] is the record being edited, removed first when
 * the edit changed its currencies.
 */
class SaveStaticExchangeRateUseCase(
    private val repository: ExchangeRateRepository,
) {
    suspend operator fun invoke(
        rate: StaticExchangeRate,
        replacing: StaticExchangeRate? = null,
    ): Resource<Boolean> {
        if (rate.baseCode.isBlank() || rate.quoteCode.isBlank()) {
            return error("Choose both currencies")
        }
        if (rate.baseCode == rate.quoteCode) return error("Choose two different currencies")
        if (!rate.buy.isFinite() || rate.buy <= 0.0) return error("Enter a buy rate greater than 0")
        if (!rate.sell.isFinite() || rate.sell <= 0.0) {
            return error("Enter a sell rate greater than 0")
        }
        if (replacing != null && !replacing.isSamePair(rate.baseCode, rate.quoteCode)) {
            repository.deleteStaticRate(replacing.baseCode, replacing.quoteCode)
        }
        repository.saveStaticRate(rate)
        return Resource.Success(true)
    }

    private fun error(message: String) = Resource.Error(Exception(message))
}
