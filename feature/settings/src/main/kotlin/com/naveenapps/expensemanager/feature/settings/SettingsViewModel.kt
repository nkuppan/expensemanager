package com.naveenapps.expensemanager.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetDefaultCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.theme.GetCurrentThemeUseCase
import com.naveenapps.expensemanager.core.model.Theme
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    getDefaultCurrencyUseCase: GetDefaultCurrencyUseCase,
    getCurrencyUseCase: GetCurrencyUseCase,
    getCurrentThemeUseCase: GetCurrentThemeUseCase,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val _currency = MutableStateFlow(getDefaultCurrencyUseCase())
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

    fun openCurrencyCustomiseScreen() {
        appComposeNavigator.navigate(ExpenseManagerScreens.CurrencyCustomiseScreen)
    }

    fun openExportScreen() {
        appComposeNavigator.navigate(ExpenseManagerScreens.ExportScreen)
    }

    fun openReminderScreen() {
        appComposeNavigator.navigate(ExpenseManagerScreens.ReminderScreen)
    }

    fun closePage() {
        appComposeNavigator.popBackStack()
    }

    fun openAboutUs() {
        appComposeNavigator.navigate(ExpenseManagerScreens.AboutUsScreen)
    }

    fun openAdvancedSettings() {
        appComposeNavigator.navigate(ExpenseManagerScreens.AdvancedSettingsScreen)
    }
}
