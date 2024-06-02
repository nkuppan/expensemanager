package com.naveenapps.expensemanager.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.budget.GetBudgetsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetTransactionGroupByCategoryUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetTransactionWithFilterUseCase
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.CategoryTransactionState
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.ExpenseFlowState
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.model.getAvailableCreditLimit
import com.naveenapps.expensemanager.core.model.toAccountUiModel
import com.naveenapps.expensemanager.core.model.toTransactionUIModel
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
    getCurrencyUseCase: GetCurrencyUseCase,
    getFormattedAmountUseCase: GetFormattedAmountUseCase,
    getAllAccountsUseCase: GetAllAccountsUseCase,
    getTransactionGroupByCategoryUseCase: GetTransactionGroupByCategoryUseCase,
    getBudgetsUseCase: GetBudgetsUseCase,
    appCoroutineDispatchers: AppCoroutineDispatchers,
    private val appComposeNavigator: AppComposeNavigator
) : ViewModel() {

    private val _state = MutableStateFlow(
        DashboardState(
            expenseFlowState = ExpenseFlowState(),
            transactions = emptyList(),
            budgets = emptyList(),
            accounts = emptyList(),
            categoryTransaction = CategoryTransactionState(
                pieChartData = listOf(),
                totalAmount = Amount(0.0),
                categoryTransactions = emptyList(),
            ),
        )
    )
    val state = _state.asStateFlow()

    init {
        combine(
            getCurrencyUseCase.invoke(),
            getTransactionWithFilterUseCase.invoke(),
            getAllAccountsUseCase.invoke(),
        ) { currency, transactions, accounts ->

            val filteredTransactions = (transactions?.map {
                it.toTransactionUIModel(
                    getFormattedAmountUseCase.invoke(
                        it.amount.amount,
                        currency,
                    ),
                )
            } ?: emptyList()).take(MAX_TRANSACTIONS_IN_LIST)

            val accountsConverted = accounts.map {
                it.toAccountUiModel(
                    getFormattedAmountUseCase.invoke(
                        it.amount,
                        currency,
                    ),
                    if (it.type == AccountType.CREDIT) {
                        getFormattedAmountUseCase.invoke(
                            it.getAvailableCreditLimit(),
                            currency
                        )
                    } else {
                        null
                    }
                )
            }

            val incomeValue = transactions?.filter { it.type == TransactionType.INCOME }?.sumOf {
                it.amount.amount
            } ?: 0.0

            val expenseValue = transactions?.filter { it.type == TransactionType.EXPENSE }?.sumOf {
                it.amount.amount
            } ?: 0.0


            _state.update {
                it.copy(
                    expenseFlowState = it.expenseFlowState.copy(
                        income = getFormattedAmountUseCase.invoke(
                            incomeValue,
                            currency,
                        ).amountString.orEmpty(),
                        expense = getFormattedAmountUseCase.invoke(
                            expenseValue,
                            currency,
                        ).amountString.orEmpty(),
                        balance = getFormattedAmountUseCase.invoke(
                            (incomeValue - expenseValue),
                            currency,
                        ).amountString.orEmpty(),
                    ),
                    transactions = filteredTransactions,
                    accounts = accountsConverted
                )
            }
        }.flowOn(appCoroutineDispatchers.computation)
            .launchIn(viewModelScope)

        getTransactionGroupByCategoryUseCase.invoke(CategoryType.EXPENSE).onEach {
            val categoryTransaction = it.copy(
                pieChartData = it.pieChartData.take(4),
                categoryTransactions = it.categoryTransactions.take(4),
            )
            _state.update { it.copy(categoryTransaction = categoryTransaction) }
        }.launchIn(viewModelScope)

        getBudgetsUseCase.invoke().onEach { budgets ->
            _state.update { it.copy(budgets = budgets) }
        }.launchIn(viewModelScope)
    }

    private fun openSettings() {
        appComposeNavigator.navigate(ExpenseManagerScreens.Settings)
    }

    private fun openAccountList() {
        appComposeNavigator.navigate(ExpenseManagerScreens.AccountList)
    }

    private fun openAccountCreate(accountId: String?) {
        appComposeNavigator.navigate(ExpenseManagerScreens.AccountCreate(accountId))
    }

    private fun openBudgetList() {
        appComposeNavigator.navigate(ExpenseManagerScreens.BudgetList)
    }

    private fun openBudgetDetails(budgetId: String?) {
        appComposeNavigator.navigate(ExpenseManagerScreens.BudgetDetails(budgetId))
    }

    private fun openTransactionList() {
        appComposeNavigator.navigate(ExpenseManagerScreens.TransactionList)
    }

    private fun openTransactionCreate(transactionId: String? = null) {
        appComposeNavigator.navigate(ExpenseManagerScreens.TransactionCreate(transactionId))
    }

    fun processAction(action: DashboardAction) {
        when (action) {
            is DashboardAction.OpenAccountEdit -> openAccountCreate(action.account.id)
            DashboardAction.OpenAccountList -> openAccountList()
            is DashboardAction.OpenBudgetDetails -> openBudgetDetails(action.budgetUiModel.id)
            DashboardAction.OpenBudgetList -> openBudgetList()
            DashboardAction.OpenSettings -> openSettings()
            is DashboardAction.OpenTransactionEdit -> openTransactionCreate(action.transaction?.id)
            DashboardAction.OpenTransactionList -> openTransactionList()
        }
    }

    companion object {
        private const val MAX_TRANSACTIONS_IN_LIST = 10
    }
}
