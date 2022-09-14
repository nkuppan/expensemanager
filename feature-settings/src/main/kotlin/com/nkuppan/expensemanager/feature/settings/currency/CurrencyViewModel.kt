package com.nkuppan.expensemanager.feature.settings.currency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.core.model.Currency
import com.nkuppan.expensemanager.data.repository.CurrencyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CurrencyViewModel @Inject constructor(
    private val repository: CurrencyRepository
) : ViewModel() {

    fun getCurrencies() = repository.getAllCurrency()

    fun setCurrency(theme: Currency) {
        viewModelScope.launch {
            repository.saveCurrency(theme)
        }
    }
}
