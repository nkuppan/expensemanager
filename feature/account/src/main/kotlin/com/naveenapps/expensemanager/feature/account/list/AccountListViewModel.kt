package com.naveenapps.expensemanager.feature.account.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.common.utils.getAmountTextColor
import com.naveenapps.expensemanager.core.domain.usecase.account.ArchiveAccountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.FormatAmountInCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currencyapi.ConvertAmountUseCase
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.getAvailableCreditLimit
import com.naveenapps.expensemanager.core.model.toAccountUiModel
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccountListViewModel(
    getAllAccountsUseCase: GetAllAccountsUseCase,
    getCurrencyUseCase: GetCurrencyUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val convertAmountUseCase: ConvertAmountUseCase,
    private val formatAmountInCurrencyUseCase: FormatAmountInCurrencyUseCase,
    private val archiveAccountUseCase: ArchiveAccountUseCase,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val _state = MutableStateFlow(
        AccountListState(
            accounts = emptyList(),
            showReOrder = false,
        ),
    )
    val state = _state.asStateFlow()

    init {
        combine(
            getCurrencyUseCase.invoke(),
            getAllAccountsUseCase.invoke(includeArchived = true),
        ) { currency, accounts ->
            // Each balance is shown in the account's own currency.
            val list = accounts.map {
                it.toAccountUiModel(
                    formatAmountInCurrencyUseCase(it.amount, it.currencyCode),
                    if (it.type == AccountType.CREDIT) {
                        formatAmountInCurrencyUseCase(it.getAvailableCreditLimit(), it.currencyCode)
                    } else {
                        null
                    },
                ) to it.isArchived
            }
            val active = list.filterNot { it.second }.map { it.first }

            // The summary covers active accounts only, each converted to the display currency
            // before summing, so balances in different currencies are never added as one unit.
            val activeAccounts = accounts.filterNot { it.isArchived }
            val assets = convertAmountUseCase.total(
                activeAccounts.filter { it.amount > 0 }.map { it.amount to it.currencyCode },
                currency.code,
            )
            val liabilities = convertAmountUseCase.total(
                activeAccounts.filter { it.amount < 0 }.map { -it.amount to it.currencyCode },
                currency.code,
            )
            val total = assets - liabilities

            _state.update {
                it.copy(
                    accounts = active,
                    archivedAccounts = list.filter { item -> item.second }.map { item -> item.first },
                    assetsAmount = getFormattedAmountUseCase(assets, currency),
                    liabilitiesAmount = getFormattedAmountUseCase(liabilities, currency),
                    totalAmount = getFormattedAmountUseCase(total, currency),
                    totalAmountTextColor = total.colorSign().getAmountTextColor(),
                    showReOrder = active.size > 1,
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun openCreateScreen(account: AccountUiModel?) {
        appComposeNavigator.navigate(
            ExpenseManagerScreens.AccountCreate(account?.id),
        )
    }

    private fun closePage() {
        appComposeNavigator.popBackStack()
    }

    private fun openAccountReOrderScreen() {
        appComposeNavigator.navigate(ExpenseManagerScreens.AccountReOrderScreen)
    }

    fun processAction(action: AccountListAction) {
        when (action) {
            AccountListAction.ClosePage -> closePage()

            AccountListAction.CreateAccount -> openCreateScreen(null)

            is AccountListAction.EditAccount -> openCreateScreen(action.accountUiModel)

            AccountListAction.OpenReOrder -> openAccountReOrderScreen()

            is AccountListAction.Unarchive -> viewModelScope.launch {
                archiveAccountUseCase(action.accountUiModel.id, archived = false)
            }
        }
    }
}
