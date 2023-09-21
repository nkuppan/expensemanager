package com.nkuppan.expensemanager.presentation.settings.currency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.domain.model.Currency
import com.nkuppan.expensemanager.domain.model.CurrencySymbolPosition
import com.nkuppan.expensemanager.domain.usecase.settings.currency.GetAllCurrencyUseCase
import com.nkuppan.expensemanager.domain.usecase.settings.currency.GetCurrencyUseCase
import com.nkuppan.expensemanager.domain.usecase.settings.currency.SaveCurrencyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CurrencyViewModel @Inject constructor(
    getCurrencyUseCase: GetCurrencyUseCase,
    getAllCurrencyUseCase: GetAllCurrencyUseCase,
    private val saveCurrencyUseCase: SaveCurrencyUseCase,
) : ViewModel() {

    private val _currentCurrency: MutableStateFlow<Currency> = MutableStateFlow(
        Currency(
            R.string.dollar_type,
            R.string.dollar_name,
            R.drawable.currency_dollar
        )
    )
    val currentCurrency = _currentCurrency.asStateFlow()

    private val _currencies = MutableStateFlow<List<Currency>>(emptyList())
    val currencies = _currencies.asStateFlow()

    init {
        getCurrencyUseCase.invoke().onEach {
            _currentCurrency.value = it
        }.launchIn(viewModelScope)

        _currencies.value = getAllCurrencyUseCase.invoke()
    }

    fun setCurrency(currency: Currency?) {
        currency ?: return
        viewModelScope.launch {
            saveCurrencyUseCase.invoke(currency)
        }
    }

    fun setCurrencyPositionType(currencySymbolPosition: CurrencySymbolPosition) {
        _currentCurrency.value = _currentCurrency.value.copy(
            position = currencySymbolPosition
        )
    }
}
