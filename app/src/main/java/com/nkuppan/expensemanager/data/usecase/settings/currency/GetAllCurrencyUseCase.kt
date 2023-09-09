package com.nkuppan.expensemanager.data.usecase.settings.currency

import com.nkuppan.expensemanager.domain.model.Currency
import com.nkuppan.expensemanager.domain.repository.CurrencyRepository
import javax.inject.Inject

class GetAllCurrencyUseCase @Inject constructor(
    private val repository: CurrencyRepository
) {
    operator fun invoke(): List<Currency> {
        return repository.getAllCurrency()
    }
}