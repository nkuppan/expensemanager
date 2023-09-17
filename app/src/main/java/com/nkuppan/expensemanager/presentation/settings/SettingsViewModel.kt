package com.nkuppan.expensemanager.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.domain.model.Currency
import com.nkuppan.expensemanager.domain.usecase.settings.currency.GetCurrencyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    getCurrencyUseCase: GetCurrencyUseCase,
) : ViewModel() {

    private val _currency: MutableStateFlow<Currency> = MutableStateFlow(
        Currency(
            R.string.default_currency_type,
            R.string.default_currency_name,
            R.drawable.currency_dollar
        )
    )
    val currency = _currency.asStateFlow()

    init {
        viewModelScope.launch {
            getCurrencyUseCase.invoke().collectLatest {
                _currency.value = it
            }
        }
    }
}