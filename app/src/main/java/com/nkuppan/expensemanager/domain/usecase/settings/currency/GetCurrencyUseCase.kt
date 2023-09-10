package com.nkuppan.expensemanager.domain.usecase.settings.currency

import com.nkuppan.expensemanager.domain.model.Currency
import com.nkuppan.expensemanager.domain.repository.CurrencyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrencyUseCase @Inject constructor(
    private val repository: CurrencyRepository
) {
    operator fun invoke(): Flow<Currency> {
        return repository.getSelectedCurrency()
    }
}