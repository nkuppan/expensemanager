package com.naveenapps.expensemanager.feature.account.reorder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.designsystem.components.swap
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.account.UpdateAllAccountUseCase
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class AccountReOrderViewModel(
    getAllAccountsUseCase: GetAllAccountsUseCase,
    private val updateAllAccountUseCase: UpdateAllAccountUseCase,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val _state = MutableStateFlow(
        AccountReOrderState(
            accounts = emptyList(),
            showSaveButton = false
        )
    )
    val state = _state.asStateFlow()

    init {
        getAllAccountsUseCase.invoke().onEach { accounts ->
            _state.update { it.copy(accounts = accounts, showSaveButton = false) }
        }.launchIn(viewModelScope)
    }

    private fun closePage() {
        appComposeNavigator.popBackStack()
    }

    private fun saveChanges() {
        viewModelScope.launch {
            updateAllAccountUseCase.invoke(
                _state.value.accounts.mapIndexed { index, item ->
                    item.copy(sequence = index)
                },
            )

            closePage()
        }
    }

    private fun swap(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            val updatedPomodoroList = _state.value.accounts.toMutableList()
            val swappedList = updatedPomodoroList.swap(fromIndex, toIndex)
            _state.update { it.copy(accounts = swappedList, showSaveButton = true) }
        }
    }

    fun processAction(action: AccountReOrderAction) {
        when (action) {
            AccountReOrderAction.ClosePage -> closePage()
            AccountReOrderAction.Save -> saveChanges()
            is AccountReOrderAction.Swap -> swap(action.fromIndex, action.toIndex)
        }
    }
}
