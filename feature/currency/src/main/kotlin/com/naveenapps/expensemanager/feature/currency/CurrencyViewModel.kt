package com.naveenapps.expensemanager.feature.currency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetDefaultCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.SaveCurrencyUseCase
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.CurrencyPosition
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.settings.domain.model.NumberFormatType
import com.naveenapps.expensemanager.core.settings.domain.repository.NumberFormatSettingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class CurrencyViewModel(
    getDefaultCurrencyUseCase: GetDefaultCurrencyUseCase,
    getCurrencyUseCase: GetCurrencyUseCase,
    private val saveCurrencyUseCase: SaveCurrencyUseCase,
    private val numberFormatSettingRepository: NumberFormatSettingRepository,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val _state = MutableStateFlow(
        CurrencyState(
            showCurrencySelection = false,
            numberFormatType = NumberFormatType.WITHOUT_ANY_SEPARATOR,
            currency = getDefaultCurrencyUseCase()
        )
    )
    val state = _state.asStateFlow()

    init {
        getCurrencyUseCase.invoke().onEach { currency ->
            _state.update { it.copy(currency = currency) }
        }.launchIn(viewModelScope)

        numberFormatSettingRepository.getNumberFormatType().onEach { numberFormatType ->
            _state.update { it.copy(numberFormatType = numberFormatType) }
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
                    ),
                    showCurrencySelection = false
                )
            }
            saveSelectedCurrency()
        }
    }

    private fun setCurrencyPositionType(currencyPosition: CurrencyPosition) {
        _state.update { it.copy(currency = it.currency.copy(position = currencyPosition)) }
        saveSelectedCurrency()
    }

    private fun setTextFormatChange(textFormat: NumberFormatType) {
        _state.update { it.copy(numberFormatType = textFormat) }
        saveSelectedCurrency()
    }

    private fun saveSelectedCurrency() {
        viewModelScope.launch {
            val currency = _state.value.currency
            saveCurrencyUseCase.invoke(currency)
            numberFormatSettingRepository.saveNumberFormatType(_state.value.numberFormatType)
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

            CurrencyAction.DismissCurrencySelection -> {
                _state.update { it.copy(showCurrencySelection = false) }
            }

            is CurrencyAction.ChangeCurrencyNumberFormat -> {
                setTextFormatChange(action.numberFormatType)
            }

            is CurrencyAction.ChangeCurrencyType -> {
                setCurrencyPositionType(action.currencyPosition)
            }

            is CurrencyAction.SelectCurrency -> {
                selectThisCurrency(action.country.currency)
            }
        }
    }
}
