package com.naveenapps.expensemanager.feature.settings.advanced

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.core.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AdvancedSettingsViewModel(
    private val appComposeNavigator: AppComposeNavigator,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AdvancedSettingState())
    val state = _state.asStateFlow()

    init {
        settingsRepository.isAutoOpenCalculator().onEach { enabled ->
            _state.update { it.copy(isAutoOpenCalculator = enabled) }
        }.launchIn(viewModelScope)
    }

    private fun closePage() {
        appComposeNavigator.popBackStack()
    }

    private fun openAccountsReOrder() {
        appComposeNavigator.navigate(ExpenseManagerScreens.AccountReOrderScreen)
    }

    fun processAction(action: AdvancedSettingAction) {
        viewModelScope.launch {
            when (action) {
                AdvancedSettingAction.ClosePage -> {
                    closePage()
                }

                AdvancedSettingAction.OpenAccountReOrder -> {
                    openAccountsReOrder()
                }

                AdvancedSettingAction.DismissDateFilterDialog -> {
                    _state.update { it.copy(showDateFilter = false) }
                }

                AdvancedSettingAction.ShowDateFilterDialog -> {
                    _state.update { it.copy(showDateFilter = true) }
                }

                AdvancedSettingAction.ToggleAutoOpenCalculator -> {
                    val newValue = !_state.value.isAutoOpenCalculator
                    settingsRepository.setAutoOpenCalculator(newValue)
                    _state.update { it.copy(isAutoOpenCalculator = newValue) }
                }
            }
        }
    }
}
