package com.naveenapps.expensemanager.feature.settings.exchangemethod

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currencyapi.GetCurrencyApiProfileUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currencyapi.SaveStaticExchangeRateUseCase
import com.naveenapps.expensemanager.core.model.ExchangeMethod
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.StaticExchangeRate
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.core.repository.ExchangeRateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExchangeMethodViewModel(
    private val exchangeRateRepository: ExchangeRateRepository,
    private val saveStaticExchangeRateUseCase: SaveStaticExchangeRateUseCase,
    getCurrencyApiProfileUseCase: GetCurrencyApiProfileUseCase,
    getAllAccountsUseCase: GetAllAccountsUseCase,
    getCurrencyUseCase: GetCurrencyUseCase,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val _state = MutableStateFlow(ExchangeMethodState())
    val state = _state.asStateFlow()

    init {
        exchangeRateRepository.getMethod().onEach { method ->
            _state.update { it.copy(method = method) }
        }.launchIn(viewModelScope)

        exchangeRateRepository.getStaticRates().onEach { rates ->
            _state.update { it.copy(staticRates = rates) }
        }.launchIn(viewModelScope)

        getCurrencyApiProfileUseCase().onEach { profile ->
            _state.update { it.copy(apiProvider = profile?.preset?.name) }
        }.launchIn(viewModelScope)

        combine(
            getCurrencyUseCase.invoke(),
            getAllAccountsUseCase.invoke(includeArchived = true),
        ) { currency, accounts ->
            (listOf(currency.code) + accounts.map { it.currencyCode })
                .filter { it.isNotBlank() }
                .distinct()
        }.onEach { codes ->
            _state.update { it.copy(currencyCodes = codes) }
        }.launchIn(viewModelScope)
    }

    private fun selectMethod(method: ExchangeMethod) {
        viewModelScope.launch { exchangeRateRepository.setMethod(method) }
    }

    private fun openEditor(rate: StaticExchangeRate?) {
        val codes = _state.value.currencyCodes
        _state.update {
            it.copy(
                editor = if (rate != null) {
                    StaticRateEditorState(
                        original = rate,
                        baseCode = rate.baseCode,
                        quoteCode = rate.quoteCode,
                        buyText = rate.buy.toRateText(),
                        sellText = rate.sell.toRateText(),
                    )
                } else {
                    StaticRateEditorState(
                        baseCode = codes.getOrNull(1).orEmpty(),
                        quoteCode = codes.firstOrNull().orEmpty(),
                    )
                },
            )
        }
    }

    private fun updateEditor(change: (StaticRateEditorState) -> StaticRateEditorState) {
        _state.update { state ->
            state.copy(editor = state.editor?.let { change(it).copy(errorMessage = null) })
        }
    }

    private fun saveEditor() {
        val editor = _state.value.editor ?: return
        viewModelScope.launch {
            val response = saveStaticExchangeRateUseCase(
                rate = StaticExchangeRate(
                    baseCode = editor.baseCode.trim().uppercase(),
                    quoteCode = editor.quoteCode.trim().uppercase(),
                    buy = editor.buyText.toRate(),
                    sell = editor.sellText.toRate(),
                ),
                replacing = editor.original,
            )
            when (response) {
                is Resource.Error -> _state.update {
                    it.copy(editor = editor.copy(errorMessage = response.exception.message))
                }

                is Resource.Success -> _state.update { it.copy(editor = null) }
            }
        }
    }

    private fun deleteStaticRate(rate: StaticExchangeRate) {
        viewModelScope.launch {
            exchangeRateRepository.deleteStaticRate(rate.baseCode, rate.quoteCode)
        }
    }

    /** Blank or unreadable text becomes 0, which the save validation rejects. */
    private fun String.toRate(): Double = replace(",", "").trim().toDoubleOrNull() ?: 0.0

    private fun Double.toRateText(): String = if (this == toLong().toDouble()) toLong().toString() else toString()

    fun processAction(action: ExchangeMethodAction) {
        when (action) {
            ExchangeMethodAction.ClosePage -> appComposeNavigator.popBackStack()

            is ExchangeMethodAction.SelectMethod -> selectMethod(action.method)

            ExchangeMethodAction.OpenApiProfile ->
                appComposeNavigator.navigate(ExpenseManagerScreens.CurrencyApiProfileScreen)

            ExchangeMethodAction.AddStaticRate -> openEditor(null)

            is ExchangeMethodAction.EditStaticRate -> openEditor(action.rate)

            is ExchangeMethodAction.DeleteStaticRate -> deleteStaticRate(action.rate)

            is ExchangeMethodAction.ChangeEditorBase -> updateEditor { it.copy(baseCode = action.code) }

            is ExchangeMethodAction.ChangeEditorQuote -> updateEditor { it.copy(quoteCode = action.code) }

            is ExchangeMethodAction.ChangeEditorBuy -> updateEditor { it.copy(buyText = action.text) }

            is ExchangeMethodAction.ChangeEditorSell -> updateEditor { it.copy(sellText = action.text) }

            ExchangeMethodAction.SaveEditor -> saveEditor()

            ExchangeMethodAction.DismissEditor -> _state.update { it.copy(editor = null) }
        }
    }
}
