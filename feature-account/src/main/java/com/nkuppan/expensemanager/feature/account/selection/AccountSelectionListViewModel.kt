package com.nkuppan.expensemanager.feature.account.selection

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.nkuppan.expensemanager.core.model.Account
import com.nkuppan.expensemanager.core.model.PaymentMode
import com.nkuppan.expensemanager.core.model.Resource
import com.nkuppan.expensemanager.data.usecase.account.GetAllAccountsUseCase
import com.nkuppan.expensemanager.data.usecase.settings.account.GetSelectedAccountUseCase
import com.nkuppan.expensemanager.data.usecase.settings.account.UpdateSelectedAccountUseCase
import com.nkuppan.expensemanager.feature.account.list.getPaymentModeIcon
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountSelectionListViewModel @Inject constructor(
    getSelectedAccountUseCase: GetSelectedAccountUseCase,
    private val updateSelectedAccountUseCase: UpdateSelectedAccountUseCase,
    private val getAllAccountsUseCase: GetAllAccountsUseCase,
    private val appCoroutineDispatchers: AppCoroutineDispatchers
) : ViewModel() {

    private val _accounts = MutableStateFlow<List<AccountSelectionUIState>>(emptyList())
    val accounts = _accounts.asStateFlow()

    private var selectedAccountId = "-1"

    init {
        getSelectedAccountUseCase.invoke().onEach {
            selectedAccountId = it?.id ?: "-1"
            loadAccounts(it)
        }.launchIn(viewModelScope)
    }

    private fun loadAccounts(account: Account?) {

        viewModelScope.launch(appCoroutineDispatchers.io) {

            when (val response = getAllAccountsUseCase.invoke()) {
                is Resource.Error -> {
                    _accounts.value = (emptyList())
                }

                is Resource.Success -> {
                    val accounts = response.data.map {
                        AccountSelectionUIState(
                            it.id,
                            it.name,
                            it.backgroundColor,
                            getPaymentModeIcon(it.type),
                            account?.id == it.id
                        )
                    }.toMutableList()

                    accounts.add(
                        0,
                        AccountSelectionUIState(
                            "-1",
                            "All Accounts",
                            "#000000",
                            getPaymentModeIcon(PaymentMode.NONE),
                            account == null
                        )
                    )

                    _accounts.value = accounts
                }
            }
        }
    }

    fun selectThisAccount(account: AccountSelectionUIState) {
        viewModelScope.launch {
            updateSelectedAccountUseCase.invoke(account.id)
        }
    }
}

data class AccountSelectionUIState(
    val id: String,
    val name: String,
    val backgroundColor: String,
    @DrawableRes val accountIcon: Int,
    var isSelected: Boolean,
)