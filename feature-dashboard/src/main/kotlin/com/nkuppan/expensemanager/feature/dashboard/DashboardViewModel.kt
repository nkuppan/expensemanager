package com.nkuppan.expensemanager.feature.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.nkuppan.expensemanager.core.model.CategoryType
import com.nkuppan.expensemanager.core.model.Resource
import com.nkuppan.expensemanager.core.model.Transaction
import com.nkuppan.expensemanager.core.ui.utils.UiText
import com.nkuppan.expensemanager.core.ui.utils.getCurrency
import com.nkuppan.expensemanager.data.usecase.settings.filter.GetFilterTypeTextUseCase
import com.nkuppan.expensemanager.data.usecase.settings.account.GetSelectedAccountUseCase
import com.nkuppan.expensemanager.data.usecase.settings.currency.GetCurrencyUseCase
import com.nkuppan.expensemanager.data.usecase.transaction.*
import com.nkuppan.expensemanager.data.utils.getPreviousDateTime
import com.nkuppan.expensemanager.data.utils.toTransactionDate
import com.nkuppan.expensemanager.feature.transaction.history.TransactionUIModel
import com.nkuppan.expensemanager.feature.transaction.list.getPaymentModeIcon
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase,
    private val getPreviousDaysTransactionWithFilterUseCase: GetPreviousDaysTransactionWithFilterUseCase,
    getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
    getSelectedAccountUseCase: GetSelectedAccountUseCase,
    getFilterTypeTextUseCase: GetFilterTypeTextUseCase,
    getCurrencyUseCase: GetCurrencyUseCase,
    getIncomeAmountUseCase: GetIncomeAmountUseCase,
    getExpenseAmountUseCase: GetExpenseAmountUseCase
) : ViewModel() {

    private val _openTransaction = Channel<Transaction>()
    val openTransaction = _openTransaction.receiveAsFlow()

    private val _errorMessage = Channel<UiText>()
    val errorMessage = _errorMessage.receiveAsFlow()

    private var currencySymbol: Int = com.nkuppan.expensemanager.data.R.string.default_currency_type

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

    private val _lastSevenDayAnalysisData = MutableStateFlow<BarData?>(null)
    val previousDayAnalysisData = _lastSevenDayAnalysisData.asStateFlow()

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

            constructPreviousDaysAnalysisGraphData()

            _transactions.value = ((response?.map {
                TransactionUIModel(
                    it.id,
                    getCurrency(currencySymbol, it.amount),
                    if (it.notes.isBlank()) {
                        UiText.StringResource(com.nkuppan.expensemanager.feature.transaction.R.string.not_assigned)
                    } else {
                        UiText.DynamicString(it.notes)
                    },
                    it.category.name,
                    it.category.type,
                    it.category.backgroundColor,
                    it.account.type.getPaymentModeIcon(),
                    it.updatedOn.toTransactionDate(),
                )
            } ?: emptyList()).take(MAX_TRANSACTIONS_IN_LIST))

        }.launchIn(viewModelScope)

        _incomeAmountValue.combine(_expenseAmountValue) { _, _ ->
            val total = _incomeAmount.value - _expenseAmount.value
            _totalIncome.value = total
            _totalIncomeValue.value = getCurrency(currencySymbol, total)
        }.launchIn(viewModelScope)
    }

    fun openTransactionEdit(transactionId: String) {
        viewModelScope.launch {
            when (val response = getTransactionByIdUseCase.invoke(transactionId)) {
                is Resource.Error -> {
                    _errorMessage.send(UiText.StringResource(com.nkuppan.expensemanager.feature.transaction.R.string.unable_to_find_transaction))
                }

                is Resource.Success -> {
                    _openTransaction.send(response.data)
                }
            }
        }
    }

    private fun constructPreviousDaysAnalysisGraphData(numberOfDays: Int = NUMBER_OF_DAYS) {

        getPreviousDaysTransactionWithFilterUseCase.invoke(numberOfDays).onEach {
            val entries: ArrayList<BarEntry> = ArrayList()
            _lastSevenDayAnalysisData.value = (constructBarData(numberOfDays, it, entries))
        }.launchIn(viewModelScope)
    }

    private fun constructBarData(
        numberOfDays: Int,
        response: Map<String, List<Transaction>>,
        entries: ArrayList<BarEntry>
    ): BarData {
        repeat(numberOfDays) { index ->
            val previousDay = getPreviousDateTime(index)

            val dateTimeValue =
                SimpleDateFormat("dd MM yyyy", Locale.getDefault()).format(previousDay.time)
            val data = response[dateTimeValue]
            val expenseSum =
                (data?.sumOf { transaction ->
                    if (transaction.category.type == CategoryType.INCOME) {
                        transaction.amount
                    } else {
                        -transaction.amount
                    }
                } ?: 0.0).toFloat()

            Log.i(
                "TAG", "Analysis: $dateTimeValue $expenseSum"
            )

            entries.add(
                BarEntry(index.toFloat(), expenseSum).apply {

                }
            )
        }

        val barDataSet = BarDataSet(entries, "Analysis")
        return BarData(barDataSet)
    }

    companion object {
        private const val NUMBER_OF_DAYS = 7
        private const val MAX_TRANSACTIONS_IN_LIST = 5
    }
}