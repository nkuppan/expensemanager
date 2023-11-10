package com.naveenapps.expensemanager.domain.usecase.settings.currency

import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.Resource
import javax.inject.Inject

class SaveCurrencyUseCase @Inject constructor(
    private val repository: com.naveenapps.expensemanager.core.data.repository.CurrencyRepository
) {
    suspend operator fun invoke(currency: Currency): Resource<Boolean> {
        return Resource.Success(repository.saveCurrency(currency))
    }
}