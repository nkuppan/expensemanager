package com.naveenapps.expensemanager.feature.settings.currencyapi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.settings.currencyapi.GetCurrencyApiProfileUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currencyapi.SaveCurrencyApiProfileUseCase
import com.naveenapps.expensemanager.core.model.CurrencyApiProfile
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CurrencyApiProfileViewModel(
    private val getCurrencyApiProfileUseCase: GetCurrencyApiProfileUseCase,
    private val saveCurrencyApiProfileUseCase: SaveCurrencyApiProfileUseCase,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val _state = MutableStateFlow(CurrencyApiProfileState())
    val state = _state.asStateFlow()

    init {
        getCurrencyApiProfileUseCase().onEach { profile ->
            if (profile != null) {
                _state.update {
                    it.copy(
                        preset = profile.preset,
                        baseUrl = profile.baseUrl,
                        apiKey = profile.apiKey,
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun save() {
        viewModelScope.launch {
            val currentState = _state.value
            val profile = CurrencyApiProfile(
                preset = currentState.preset,
                baseUrl = currentState.baseUrl,
                apiKey = currentState.apiKey,
            )
            when (val response = saveCurrencyApiProfileUseCase(profile)) {
                is Resource.Error -> {
                    _state.update { it.copy(errorMessage = response.exception.message) }
                }

                is Resource.Success -> {
                    _state.update { it.copy(errorMessage = null, isSaved = true) }
                    appComposeNavigator.popBackStack()
                }
            }
        }
    }

    private fun closePage() {
        appComposeNavigator.popBackStack()
    }

    fun processAction(action: CurrencyApiProfileAction) {
        when (action) {
            CurrencyApiProfileAction.ClosePage -> closePage()

            CurrencyApiProfileAction.Save -> save()

            is CurrencyApiProfileAction.SelectPreset -> {
                _state.update {
                    it.copy(
                        preset = action.preset,
                        baseUrl = action.preset.defaultBaseUrl.ifBlank { it.baseUrl },
                    )
                }
            }

            is CurrencyApiProfileAction.ChangeBaseUrl -> {
                _state.update { it.copy(baseUrl = action.baseUrl) }
            }

            is CurrencyApiProfileAction.ChangeApiKey -> {
                _state.update { it.copy(apiKey = action.apiKey) }
            }
        }
    }
}
