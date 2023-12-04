package com.naveenapps.expensemanager.feature.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class AdvancedSettingsViewModel @Inject constructor(
    getAllAccountsUseCase: GetAllAccountsUseCase,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    var accounts by mutableStateOf<List<Account>>(emptyList())
        private set

    init {
        getAllAccountsUseCase.invoke().onEach {
            accounts = it
        }.launchIn(viewModelScope)
    }

    fun closePage() {
        appComposeNavigator.popBackStack()
    }
}