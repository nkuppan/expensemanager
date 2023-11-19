package com.naveenapps.expensemanager.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.budget.BudgetUiModel
import com.naveenapps.expensemanager.core.domain.usecase.budget.GetBudgetsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.GetOnboardingStatusUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.SetOnboardingStatusUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetAmountStateUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetTransactionGroupByCategoryUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetTransactionWithFilterUseCase
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.AmountUiState
import com.naveenapps.expensemanager.core.model.CategoryTransactionUiModel
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.TransactionUiItem
import com.naveenapps.expensemanager.core.model.toTransactionUIModel
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.feature.account.list.toAccountUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class DashboardViewModel @Inject constructor(
    getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
    getCurrencyUseCase: GetCurrencyUseCase,
    getFormattedAmountUseCase: GetFormattedAmountUseCase,
    getAmountStateUseCase: GetAmountStateUseCase,
    getAccountsUseCase: GetAccountsUseCase,
    getTransactionGroupByCategoryUseCase: GetTransactionGroupByCategoryUseCase,
    getBudgetsUseCase: GetBudgetsUseCase,
    private val getOnboardingStatusUseCase: GetOnboardingStatusUseCase,
    private val setOnboardingStatusUseCase: SetOnboardingStatusUseCase,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val _showToolTip = MutableStateFlow(false)
    val showToolTip = _showToolTip.asStateFlow()

    private val _amountUiState = MutableStateFlow(AmountUiState())
    val amountUiState = _amountUiState.asStateFlow()

    private val _transactions = MutableStateFlow<List<TransactionUiItem>>(emptyList())
    val transactions = _transactions.asStateFlow()

    private val _budgets = MutableStateFlow<List<BudgetUiModel>>(emptyList())
    val budgets = _budgets.asStateFlow()

    private val _accounts = MutableStateFlow<List<AccountUiModel>>(emptyList())
    val accounts = _accounts.asStateFlow()

    private val _categoryTransaction = MutableStateFlow(
        CategoryTransactionUiModel(
            pieChartData = listOf(),
            totalAmount = Amount(0.0),
            categoryTransactions = emptyList()
        )
    )
    val categoryTransaction = _categoryTransaction.asStateFlow()

    init {
        combine(
            getCurrencyUseCase.invoke(),
            getTransactionWithFilterUseCase.invoke()
        ) { currency, response ->

            _transactions.value = ((response?.map {
                it.toTransactionUIModel(
                    getFormattedAmountUseCase.invoke(
                        it.amount.amount,
                        currency
                    )
                )
            } ?: emptyList()).take(MAX_TRANSACTIONS_IN_LIST))

        }.launchIn(viewModelScope)

        getAmountStateUseCase.invoke().onEach {
            _amountUiState.value = it
        }.launchIn(viewModelScope)

        combine(
            getCurrencyUseCase.invoke(),
            getAccountsUseCase.invoke()
        ) { currency, accounts ->
            _accounts.value = accounts.map {
                it.toAccountUiModel(
                    getFormattedAmountUseCase.invoke(
                        it.amount,
                        currency
                    )
                )
            }
        }.launchIn(viewModelScope)

        getTransactionGroupByCategoryUseCase.invoke(CategoryType.EXPENSE).onEach {
            _categoryTransaction.value = it.copy(
                pieChartData = it.pieChartData.take(4),
                categoryTransactions = it.categoryTransactions.take(4)
            )
        }.launchIn(viewModelScope)

        getBudgetsUseCase.invoke().onEach {
            _budgets.value = it
        }.launchIn(viewModelScope)

        loadTooltipState()
    }

    private fun loadTooltipState() {
        viewModelScope.launch {
            _showToolTip.value = getOnboardingStatusUseCase.invoke().not()
        }
    }

    fun closeToolTip() {
        viewModelScope.launch {
            setOnboardingStatusUseCase.invoke(true)
            loadTooltipState()
        }
    }

    fun openSettings() {
        appComposeNavigator.navigate(ExpenseManagerScreens.Settings.route)
    }

    fun openAccountList() {
        appComposeNavigator.navigate(ExpenseManagerScreens.AccountList.route)
    }

    fun openAccountCreate(accountId: String?) {
        appComposeNavigator.navigate(
            ExpenseManagerScreens.AccountCreate.createRoute(accountId ?: "")
        )
    }

    fun openBudgetList() {
        appComposeNavigator.navigate(ExpenseManagerScreens.BudgetList.route)
    }

    fun openBudgetCreate(budgetId: String?) {
        appComposeNavigator.navigate(
            ExpenseManagerScreens.BudgetDetails.createRoute(budgetId ?: "")
        )
    }

    fun openTransactionList() {
        appComposeNavigator.navigate(ExpenseManagerScreens.TransactionList.route)
    }

    fun openTransactionCreate(transactionId: String?) {
        appComposeNavigator.navigate(
            ExpenseManagerScreens.TransactionCreate.createRoute(transactionId ?: "")
        )
    }

    companion object {
        private const val MAX_TRANSACTIONS_IN_LIST = 10
    }
}