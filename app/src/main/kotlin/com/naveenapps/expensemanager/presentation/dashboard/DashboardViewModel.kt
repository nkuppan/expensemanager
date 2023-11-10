package com.naveenapps.expensemanager.presentation.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.domain.model.TransactionUiItem
import com.naveenapps.expensemanager.domain.model.toTransactionUIModel
import com.naveenapps.expensemanager.domain.usecase.account.GetAccountsUseCase
import com.naveenapps.expensemanager.domain.usecase.budget.BudgetUiModel
import com.naveenapps.expensemanager.domain.usecase.budget.GetBudgetsUseCase
import com.naveenapps.expensemanager.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.domain.usecase.transaction.GetAmountStateUseCase
import com.naveenapps.expensemanager.domain.usecase.transaction.GetTransactionGroupByCategoryUseCase
import com.naveenapps.expensemanager.domain.usecase.transaction.GetTransactionWithFilterUseCase
import com.naveenapps.expensemanager.presentation.account.list.AccountUiModel
import com.naveenapps.expensemanager.presentation.account.list.toAccountUiModel
import com.naveenapps.expensemanager.presentation.category.transaction.CategoryTransactionUiModel
import com.naveenapps.expensemanager.presentation.home.HomeScreenBottomBarItems
import com.naveenapps.expensemanager.ui.utils.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
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
) : ViewModel() {

    var homeScreenBottomBarItems by mutableStateOf(HomeScreenBottomBarItems.Home)
        private set

    private val _errorMessage = Channel<UiText>()
    val errorMessage = _errorMessage.receiveAsFlow()

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
    }

    fun setUISystem(homeScreenBottomBarItems: HomeScreenBottomBarItems) {
        this.homeScreenBottomBarItems = homeScreenBottomBarItems
    }

    companion object {
        private const val MAX_TRANSACTIONS_IN_LIST = 10
    }
}

data class AmountUiState(
    val income: String = "",
    val expense: String = "",
    val balance: String = "",
)