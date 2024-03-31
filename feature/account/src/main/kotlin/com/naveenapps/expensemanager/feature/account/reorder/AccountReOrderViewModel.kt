package com.naveenapps.expensemanager.feature.account.reorder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.designsystem.components.swap
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.account.UpdateAllAccountUseCase
import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _showActionButton = MutableStateFlow<Boolean>(false)
    val showActionButton = _showActionButton.asStateFlow()

    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts = _accounts.asStateFlow()

    init {
        getAllAccountsUseCase.invoke().onEach {
            _accounts.value = it
        }.launchIn(viewModelScope)
    }

    fun closePage() {
        appComposeNavigator.popBackStack()
    }

    fun saveChanges() {
        viewModelScope.launch {
            updateAllAccountUseCase.invoke(
                _accounts.value.mapIndexed { index, item ->
                    item.copy(sequence = index)
                },
            )

            closePage()
        }
    }

    fun swap(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            val updatedPomodoroList = _accounts.value.toMutableList()
            val swappedList = updatedPomodoroList.swap(fromIndex, toIndex)
            _accounts.value = swappedList
            _showActionButton.value = true
        }
    }
}
