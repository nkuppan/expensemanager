package com.naveenapps.expensemanager.core.domain.usecase.settings.currency

import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.CurrencyRepository

class SaveCurrencyUseCase(
    private val repository: CurrencyRepository,
) {
    suspend operator fun invoke(currency: Currency): Resource<Boolean> {
        if (currency.symbol.isBlank()) {
            return Resource.Error(Exception("Please provide valid currency symbol"))
        }

        if (currency.name.isBlank()) {
            return Resource.Error(Exception("Please provide valid currency name"))
        }

        return Resource.Success(repository.saveCurrency(currency))
    }
}
