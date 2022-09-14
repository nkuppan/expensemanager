package com.nkuppan.expensemanager.data.usecase.settings.currency

import com.nkuppan.expensemanager.core.model.Currency
import com.nkuppan.expensemanager.core.model.Resource
import com.nkuppan.expensemanager.data.repository.CurrencyRepository

class SaveCurrencyUseCase(private val repository: CurrencyRepository) {
    suspend operator fun invoke(currency: Currency): Resource<Boolean> {
        return Resource.Success(repository.saveCurrency(currency))
    }
}