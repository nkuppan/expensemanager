package com.naveenapps.expensemanager.domain.usecase.settings.currency

import com.naveenapps.expensemanager.domain.model.Currency
import com.naveenapps.expensemanager.domain.model.Resource
import com.naveenapps.expensemanager.domain.repository.CurrencyRepository
import javax.inject.Inject

class SaveCurrencyUseCase @Inject constructor(
    private val repository: CurrencyRepository
) {
    suspend operator fun invoke(currency: Currency): Resource<Boolean> {
        return Resource.Success(repository.saveCurrency(currency))
    }
}