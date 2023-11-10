package com.naveenapps.expensemanager.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.Theme
import com.naveenapps.expensemanager.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.domain.usecase.settings.theme.GetCurrentThemeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    getCurrencyUseCase: GetCurrencyUseCase,
    getCurrentThemeUseCase: GetCurrentThemeUseCase,
) : ViewModel() {

    private val _currency: MutableStateFlow<Currency> = MutableStateFlow(
        Currency(
            com.naveenapps.expensemanager.core.data.R.string.default_currency_type,
            com.naveenapps.expensemanager.core.data.R.string.default_currency_name,
            com.naveenapps.expensemanager.core.data.R.drawable.currency_dollar
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