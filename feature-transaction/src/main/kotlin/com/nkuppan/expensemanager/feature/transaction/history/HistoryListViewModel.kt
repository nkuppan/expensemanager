package com.nkuppan.expensemanager.feature.transaction.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.core.model.Resource
import com.nkuppan.expensemanager.core.model.Transaction
import com.nkuppan.expensemanager.core.ui.utils.UiText
import com.nkuppan.expensemanager.data.usecase.settings.currency.GetCurrencyUseCase
import com.nkuppan.expensemanager.data.usecase.transaction.GetTransactionByIdUseCase
import com.nkuppan.expensemanager.data.usecase.transaction.GetTransactionGroupByMonthUseCase
import com.nkuppan.expensemanager.feature.transaction.R
import com.nkuppan.expensemanager.feature.transaction.list.getPaymentModeIcon
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HistoryListViewModel @Inject constructor(
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val getTransactionGroupByMonthUseCase: GetTransactionGroupByMonthUseCase,
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase
) : ViewModel() {

    private val _errorMessage = Channel<UiText>()
    val errorMessage = _errorMessage.receiveAsFlow()

    private val _openTransaction = Channel<Transaction>()
    val openTransaction = _openTransaction.receiveAsFlow()

    private val _transactionHistory = Channel<List<HistoryListItem>>()
    val transactionHistory = _transactionHistory.receiveAsFlow()

    private var currencySymbol: Int = R.string.default_currency_type

    init {
        viewModelScope.launch {
            getCurrencyUseCase.invoke().collectLatest {
                currencySymbol = it.type
            }
        }
    }

    fun loadTransactions() {

        viewModelScope.launch {

            val transactionHistory =
                when (val result = getTransactionGroupByMonthUseCase.invoke()) {
                    is Resource.Error -> {
                        emptyList()
                    }
                    is Resource.Success -> {

                        val values: MutableList<HistoryListItem> = arrayListOf()

                        result.data.forEach { value ->

                            val titleValue: UiText = if (value.monthText == SimpleDateFormat(
                                    "MMM yyyy",
                                    Locale.getDefault()
                                ).format(Date())
                            ) {
                                UiText.StringResource(R.string.this_month)
                            } else {
                                UiText.DynamicString(value.monthText)
                            }

                            val totalAmount = value.transaction.sumOf { it.amount }

                            val history = HistoryListItem(
                                titleValue,
                                UiText.StringResource(
                                    R.string.amount_string,
                                    totalAmount,
                                    UiText.StringResource(currencySymbol)
                                ),
                                value.transaction.map {
                                    TransactionUIModel(
                                        it.id,
                                        UiText.StringResource(
                                            R.string.amount_string,
                                            it.amount,
                                            UiText.StringResource(currencySymbol)
                                        ),
                                        if (it.notes.isBlank()) {
                                            UiText.StringResource(R.string.not_assigned)
                                        } else {
                                            UiText.DynamicString(it.notes)
                                        },
                                        it.category.name,
                                        it.category.backgroundColor,
                                        it.account.type.getPaymentModeIcon()
                                    )
                                },
                                expanded = false
                            )
                            values.add(history)
                        }

                        values
                    }
                }

            _transactionHistory.send(transactionHistory)
        }
    }

    fun openTransactionEdit(transactionId: String) {
        viewModelScope.launch {
            when (val response = getTransactionByIdUseCase.invoke(transactionId)) {
                is Resource.Error -> {
                    _errorMessage.send(UiText.StringResource(R.string.unable_to_find_transaction))
                }
                is Resource.Success -> {
                    _openTransaction.send(response.data)
                }
            }
        }
    }
}