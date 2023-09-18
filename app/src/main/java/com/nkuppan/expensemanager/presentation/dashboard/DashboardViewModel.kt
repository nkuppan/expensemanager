package com.nkuppan.expensemanager.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.core.ui.utils.UiText
import com.nkuppan.expensemanager.core.ui.utils.getCurrency
import com.nkuppan.expensemanager.domain.usecase.settings.account.GetSelectedAccountUseCase
import com.nkuppan.expensemanager.domain.usecase.settings.currency.GetCurrencyUseCase
import com.nkuppan.expensemanager.domain.usecase.settings.filter.GetFilterTypeTextUseCase
import com.nkuppan.expensemanager.domain.usecase.transaction.GetExpenseAmountUseCase
import com.nkuppan.expensemanager.domain.usecase.transaction.GetIncomeAmountUseCase
import com.nkuppan.expensemanager.domain.usecase.transaction.GetPreviousDaysTransactionWithFilterUseCase
import com.nkuppan.expensemanager.domain.usecase.transaction.GetTransactionWithFilterUseCase
import com.nkuppan.expensemanager.presentation.transaction.history.TransactionUIModel
import com.nkuppan.expensemanager.presentation.transaction.history.toTransactionUIModel
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
    private val getPreviousDaysTransactionWithFilterUseCase: GetPreviousDaysTransactionWithFilterUseCase,
    getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
    getSelectedAccountUseCase: GetSelectedAccountUseCase,
    getFilterTypeTextUseCase: GetFilterTypeTextUseCase,
    getCurrencyUseCase: GetCurrencyUseCase,
    getIncomeAmountUseCase: GetIncomeAmountUseCase,
    getExpenseAmountUseCase: GetExpenseAmountUseCase
) : ViewModel() {

    private val _errorMessage = Channel<UiText>()
    val errorMessage = _errorMessage.receiveAsFlow()

    private var currencySymbol: Int = R.string.default_currency_type

    private val _incomeAmount = MutableStateFlow(0.0)
    private val _expenseAmount = MutableStateFlow(0.0)
    private val _totalIncome = MutableStateFlow(0.0)

    private val _dateValue = MutableStateFlow("")
    val dateValue = _dateValue.asStateFlow()

    private val _accountValue = MutableStateFlow("")
    val accountValue = _accountValue.asStateFlow()

    private val _expenseAmountValue = MutableStateFlow(getCurrency(currencySymbol, 0.0))
    val expenseAmountValue = _expenseAmountValue.asStateFlow()

    private val _incomeAmountValue = MutableStateFlow(getCurrency(currencySymbol, 0.0))
    val incomeAmountValue = _incomeAmountValue.asStateFlow()

    private val _totalIncomeValue = MutableStateFlow(getCurrency(currencySymbol, 0.0))
    val totalIncomeValue = _totalIncomeValue.asStateFlow()

    private val _transactions = MutableStateFlow<List<TransactionUIModel>?>(null)
    val transactions = _transactions.asStateFlow()

    init {
        getSelectedAccountUseCase.invoke().onEach {
            _accountValue.value = it?.name ?: "All"
        }.launchIn(viewModelScope)

        getFilterTypeTextUseCase.invoke().onEach {
            _dateValue.value = it
        }.launchIn(viewModelScope)

        getCurrencyUseCase.invoke().combine(getIncomeAmountUseCase.invoke()) { currency, income ->
            currencySymbol = currency.type
            val amount = income ?: 0.0
            _incomeAmount.value = income ?: 0.0
            _incomeAmountValue.value = getCurrency(currency.type, amount)
        }.launchIn(viewModelScope)

        getCurrencyUseCase.invoke().combine(getExpenseAmountUseCase.invoke()) { currency, expense ->
            currencySymbol = currency.type
            val amount = expense ?: 0.0
            _expenseAmount.value = expense ?: 0.0
            _expenseAmountValue.value = getCurrency(currency.type, amount)
        }.launchIn(viewModelScope)

        getTransactionWithFilterUseCase.invoke().onEach { response ->

            _transactions.value = ((response?.map {
                it.toTransactionUIModel(currencySymbol)
            } ?: emptyList()).take(MAX_TRANSACTIONS_IN_LIST))

        }.launchIn(viewModelScope)

        _incomeAmountValue.combine(_expenseAmountValue) { _, _ ->
            val total = _incomeAmount.value - _expenseAmount.value
            _totalIncome.value = total
            _totalIncomeValue.value = getCurrency(currencySymbol, total)
        }.launchIn(viewModelScope)
    }

    companion object {
        private const val NUMBER_OF_DAYS = 7
        private const val MAX_TRANSACTIONS_IN_LIST = 5
    }
}