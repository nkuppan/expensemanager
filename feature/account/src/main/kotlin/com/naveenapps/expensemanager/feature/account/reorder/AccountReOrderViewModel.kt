package com.naveenapps.expensemanager.feature.account.reorder

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.designsystem.components.swap
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.account.UpdateAllAccountUseCase
import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountReOrderViewModel @Inject constructor(
    getAllAccountsUseCase: GetAllAccountsUseCase,
    private val updateAllAccountUseCase: UpdateAllAccountUseCase,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    var showActionButton by mutableStateOf(false)
        private set

    var accounts by mutableStateOf(emptyList<Account>())
        private set

    init {
        getAllAccountsUseCase.invoke().onEach {
            accounts = it
        }.launchIn(viewModelScope)
    }

    fun closePage() {
        appComposeNavigator.popBackStack()
    }

    fun saveChanges() {
        viewModelScope.launch {
            updateAllAccountUseCase.invoke(
                accounts.mapIndexed { index, item ->
                    item.copy(sequence = index)
                },
            )

            closePage()
        }
    }

    fun swap(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            val updatedPomodoroList = accounts.toMutableList()
            val swappedList = updatedPomodoroList.swap(fromIndex, toIndex)
            accounts = swappedList
            showActionButton = true
        }
    }
}
