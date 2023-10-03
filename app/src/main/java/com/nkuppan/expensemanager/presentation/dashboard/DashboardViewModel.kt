package com.nkuppan.expensemanager.presentation.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.core.ui.utils.UiText
import com.nkuppan.expensemanager.core.ui.utils.getBalanceCurrency
import com.nkuppan.expensemanager.core.ui.utils.getCurrency
import com.nkuppan.expensemanager.domain.usecase.account.GetAccountsUseCase
import com.nkuppan.expensemanager.domain.usecase.settings.account.GetSelectedAccountUseCase
import com.nkuppan.expensemanager.domain.usecase.settings.currency.GetCurrencyUseCase
import com.nkuppan.expensemanager.domain.usecase.settings.filter.GetFilterTypeTextUseCase
import com.nkuppan.expensemanager.domain.usecase.transaction.GetExpenseAmountUseCase
import com.nkuppan.expensemanager.domain.usecase.transaction.GetIncomeAmountUseCase
import com.nkuppan.expensemanager.domain.usecase.transaction.GetTransactionWithFilterUseCase
import com.nkuppan.expensemanager.domain.usecase.transaction.GetTransactionsForCurrentMonthUseCase
import com.nkuppan.expensemanager.presentation.account.list.AccountUiModel
import com.nkuppan.expensemanager.presentation.account.list.toAccountUiModel
import com.nkuppan.expensemanager.presentation.analysis.AnalysisChartData
import com.nkuppan.expensemanager.presentation.analysis.constructGraphItems
import com.nkuppan.expensemanager.presentation.home.HomeScreenBottomBarItems
import com.nkuppan.expensemanager.presentation.transaction.history.TransactionUIModel
import com.nkuppan.expensemanager.presentation.transaction.history.toTransactionUIModel
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject


@HiltViewModel
class DashboardViewModel @Inject constructor(
    getTransactionsForCurrentMonthUseCase: GetTransactionsForCurrentMonthUseCase,
    getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
    getSelectedAccountUseCase: GetSelectedAccountUseCase,
    getFilterTypeTextUseCase: GetFilterTypeTextUseCase,
    getCurrencyUseCase: GetCurrencyUseCase,
    getIncomeAmountUseCase: GetIncomeAmountUseCase,
    getExpenseAmountUseCase: GetExpenseAmountUseCase,
    getAccountsUseCase: GetAccountsUseCase,
) : ViewModel() {

    var homeScreenBottomBarItems by mutableStateOf(HomeScreenBottomBarItems.Home)
        private set

    private val _errorMessage = Channel<UiText>()
    val errorMessage = _errorMessage.receiveAsFlow()

    private val _incomeAmount = MutableStateFlow(0.0)
    private val _expenseAmount = MutableStateFlow(0.0)
    private val _totalIncome = MutableStateFlow(0.0)

    private val _dateValue = MutableStateFlow("")
    val dateValue = _dateValue.asStateFlow()

    private val _accountValue = MutableStateFlow("")
    val accountValue = _accountValue.asStateFlow()

    private val _expenseAmountValue = MutableStateFlow<UiText>(UiText.DynamicString(""))
    val expenseAmountValue = _expenseAmountValue.asStateFlow()

    private val _incomeAmountValue = MutableStateFlow<UiText>(UiText.DynamicString(""))
    val incomeAmountValue = _incomeAmountValue.asStateFlow()

    private val _totalIncomeValue = MutableStateFlow<UiText>(UiText.DynamicString(""))
    val totalIncomeValue = _totalIncomeValue.asStateFlow()

    private val _transactions = MutableStateFlow<List<TransactionUIModel>?>(null)
    val transactions = _transactions.asStateFlow()

    private val _chartData = MutableStateFlow<AnalysisChartData?>(null)
    val chartData = _chartData.asStateFlow()

    private val _accounts = MutableStateFlow<List<AccountUiModel>>(emptyList())
    val accounts = _accounts.asStateFlow()

    init {
        getSelectedAccountUseCase.invoke().onEach {
            _accountValue.value = it?.name ?: "All"
        }.launchIn(viewModelScope)

        getFilterTypeTextUseCase.invoke().onEach {
            _dateValue.value = it
        }.launchIn(viewModelScope)

        getCurrencyUseCase.invoke()
            .combine(getTransactionWithFilterUseCase.invoke()) { currency, response ->

                _transactions.value = ((response?.map {
                    it.toTransactionUIModel(currency)
                } ?: emptyList()).take(MAX_TRANSACTIONS_IN_LIST))

            }.launchIn(viewModelScope)

        combine(
            getCurrencyUseCase.invoke(),
            getIncomeAmountUseCase.invoke(),
            getExpenseAmountUseCase.invoke()
        ) { currency, income, expense ->

            val incomeAmount = income ?: 0.0
            _incomeAmount.value = income ?: 0.0
            _incomeAmountValue.value = getCurrency(currency, incomeAmount)

            val expenseAmount = expense ?: 0.0
            _expenseAmount.value = expense ?: 0.0
            _expenseAmountValue.value = getCurrency(currency, expenseAmount)

            val total = _incomeAmount.value - _expenseAmount.value
            _totalIncome.value = total
            _totalIncomeValue.value = getBalanceCurrency(currency, total)
        }.launchIn(viewModelScope)


        getCurrencyUseCase.invoke().combine(
            getTransactionsForCurrentMonthUseCase.invoke()
        ) { currency, response ->
            val data = constructGraphItems(
                response,
                currency
            )
            _chartData.value = data?.chartData ?: AnalysisChartData(
                chartData = entryModelOf(
                    listOf(entryOf(0, 0)),
                    listOf(entryOf(0, 0)),
                ),
                dates = emptyList()
            )
        }.launchIn(viewModelScope)



        getCurrencyUseCase.invoke().combine(getAccountsUseCase.invoke()) { currency, accounts ->
            currency to accounts
        }.map { currencyAndAccountPair ->

            val (currency, accounts) = currencyAndAccountPair

            _accounts.value = accounts.map {
                it.toAccountUiModel(currency)
            }
        }.launchIn(viewModelScope)
    }

    fun setUISystem(homeScreenBottomBarItems: HomeScreenBottomBarItems) {
        this.homeScreenBottomBarItems = homeScreenBottomBarItems
    }

    companion object {
        private const val MAX_TRANSACTIONS_IN_LIST = 5
    }
}