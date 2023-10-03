package com.nkuppan.expensemanager.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.domain.model.Currency
import com.nkuppan.expensemanager.domain.model.Theme
import com.nkuppan.expensemanager.domain.usecase.settings.currency.GetCurrencyUseCase
import com.nkuppan.expensemanager.domain.usecase.settings.theme.GetCurrentThemeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    getCurrencyUseCase: GetCurrencyUseCase,
    getCurrentThemeUseCase: GetCurrentThemeUseCase
) : ViewModel() {

    private val _currency: MutableStateFlow<Currency> = MutableStateFlow(
        Currency(
            R.string.default_currency_type,
            R.string.default_currency_name,
            R.drawable.currency_dollar
        )
    )
    val currency = _currency.asStateFlow()

    private val _theme = MutableStateFlow<Theme?>(null)
    val theme = _theme.asStateFlow()

    init {
        getCurrencyUseCase.invoke().onEach {
            _currency.value = it
        }.launchIn(viewModelScope)

        getCurrentThemeUseCase.invoke().onEach {
            _theme.value = it
        }.launchIn(viewModelScope)
    }
}