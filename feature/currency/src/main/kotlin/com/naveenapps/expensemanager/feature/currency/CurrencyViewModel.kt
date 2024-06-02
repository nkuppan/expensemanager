package com.naveenapps.expensemanager.feature.currency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetDefaultCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.SaveCurrencyUseCase
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.TextFormat
import com.naveenapps.expensemanager.core.model.TextPosition
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CurrencyViewModel @Inject constructor(
    getDefaultCurrencyUseCase: GetDefaultCurrencyUseCase,
    getCurrencyUseCase: GetCurrencyUseCase,
    private val saveCurrencyUseCase: SaveCurrencyUseCase,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val _currentCurrency = MutableStateFlow(getDefaultCurrencyUseCase())
    val currentCurrency = _currentCurrency.asStateFlow()

    init {
        getCurrencyUseCase.invoke().onEach {
            _currentCurrency.value = it
        }.launchIn(viewModelScope)
    }

    fun selectThisCurrency(currency: Currency?) {
        currency ?: return
        viewModelScope.launch {
            _currentCurrency.update { it.copy(name = currency.name, symbol = currency.symbol) }
            saveSelectedCurrency()
        }
    }

    fun setCurrencyPositionType(textPosition: TextPosition) {
        viewModelScope.launch {
            _currentCurrency.update { it.copy(position = textPosition) }
            saveSelectedCurrency()
        }
    }

    fun setTextFormatChange(textFormat: TextFormat) {
        viewModelScope.launch {
            _currentCurrency.update { it.copy(format = textFormat) }
            saveSelectedCurrency()
        }
    }

    private fun saveSelectedCurrency() {
        viewModelScope.launch {
            val currency = _currentCurrency.value
            saveCurrencyUseCase.invoke(currency)
        }
    }

    fun closePage() {
        appComposeNavigator.popBackStack()
    }
}
