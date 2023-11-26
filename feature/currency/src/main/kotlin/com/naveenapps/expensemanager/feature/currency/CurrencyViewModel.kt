package com.naveenapps.expensemanager.feature.currency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetAllCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetDefaultCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.SaveCurrencyUseCase
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.TextPosition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CurrencyViewModel @Inject constructor(
    getDefaultCurrencyUseCase: GetDefaultCurrencyUseCase,
    getCurrencyUseCase: GetCurrencyUseCase,
    getAllCurrencyUseCase: GetAllCurrencyUseCase,
    private val saveCurrencyUseCase: SaveCurrencyUseCase,
) : ViewModel() {

    private val _currentCurrency = MutableStateFlow(getDefaultCurrencyUseCase())
    val currentCurrency = _currentCurrency.asStateFlow()

    private val _currencies = MutableStateFlow<List<Currency>>(emptyList())
    val currencies = _currencies.asStateFlow()

    init {
        getCurrencyUseCase.invoke().onEach {
            _currentCurrency.value = it
        }.launchIn(viewModelScope)

        _currencies.value = getAllCurrencyUseCase.invoke()
    }

    fun selectThisCurrency(currency: Currency?) {
        currency ?: return
        viewModelScope.launch {
            _currentCurrency.value = currency
        }
    }

    fun saveSelectedCurrency() {
        val currency = _currentCurrency.value
        viewModelScope.launch {
            saveCurrencyUseCase.invoke(currency)
        }
    }

    fun setCurrencyPositionType(textPosition: TextPosition) {
        _currentCurrency.value = _currentCurrency.value.copy(
            position = textPosition
        )
    }
}
