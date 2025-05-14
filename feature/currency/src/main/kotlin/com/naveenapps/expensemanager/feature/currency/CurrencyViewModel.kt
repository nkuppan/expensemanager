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

    private val _state = MutableStateFlow(
        CurrencyState(
            showCurrencySelection = false,
            currency = getDefaultCurrencyUseCase()
        )
    )
    val state = _state.asStateFlow()

    init {
        getCurrencyUseCase.invoke().onEach { currency ->
            _state.update { it.copy(currency = currency) }
        }.launchIn(viewModelScope)
    }

    private fun selectThisCurrency(currency: Currency?) {
        currency ?: return
        viewModelScope.launch {
            _state.update {
                it.copy(
                    currency = it.currency.copy(
                        name = currency.name,
                        symbol = currency.symbol
                    )
                )
            }
            saveSelectedCurrency()
        }
    }

    private fun setCurrencyPositionType(textPosition: TextPosition) {
        viewModelScope.launch {
            _state.update { it.copy(currency = it.currency.copy(position = textPosition)) }
            saveSelectedCurrency()
        }
    }

    private fun setTextFormatChange(textFormat: TextFormat) {
        viewModelScope.launch {
            _state.update { it.copy(currency = it.currency.copy(format = textFormat)) }
            saveSelectedCurrency()
        }
    }

    private fun saveSelectedCurrency() {
        viewModelScope.launch {
            val currency = _state.value.currency
            saveCurrencyUseCase.invoke(currency)
        }
    }

    private fun closePage() {
        appComposeNavigator.popBackStack()
    }

    fun processAction(action: CurrencyAction) {
        when (action) {
            CurrencyAction.ClosePage -> closePage()
            CurrencyAction.OpenCurrencySelection -> {
                _state.update { it.copy(showCurrencySelection = true) }
            }

            is CurrencyAction.ChangeCurrencyNumberFormat -> {
                setTextFormatChange(action.textFormat)
            }

            is CurrencyAction.ChangeCurrencyType -> {
                setCurrencyPositionType(action.textPosition)
            }

            is CurrencyAction.SelectCurrency -> {
                selectThisCurrency(action.currency)
            }
        }
    }
}
