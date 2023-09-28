package com.nkuppan.expensemanager.presentation.transaction.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.core.ui.utils.UiText
import com.nkuppan.expensemanager.core.ui.utils.getCurrency
import com.nkuppan.expensemanager.data.utils.toTransactionDate
import com.nkuppan.expensemanager.domain.model.CategoryType
import com.nkuppan.expensemanager.domain.model.Currency
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.model.Transaction
import com.nkuppan.expensemanager.domain.usecase.settings.currency.GetCurrencyUseCase
import com.nkuppan.expensemanager.domain.usecase.transaction.FindTransactionByIdUseCase
import com.nkuppan.expensemanager.domain.usecase.transaction.GetTransactionGroupByMonthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryListViewModel @Inject constructor(
    getCurrencyUseCase: GetCurrencyUseCase,
    getTransactionGroupByMonthUseCase: GetTransactionGroupByMonthUseCase,
    private val findTransactionByIdUseCase: FindTransactionByIdUseCase
) : ViewModel() {

    private val _errorMessage = Channel<UiText>()
    val errorMessage = _errorMessage.receiveAsFlow()

    private val _openTransaction = Channel<Transaction>()
    val openTransaction = _openTransaction.receiveAsFlow()

    private val _transactionHistory = Channel<List<HistoryListItem>>()
    val transactionHistory = _transactionHistory.receiveAsFlow()

    init {
        getCurrencyUseCase.invoke().combine(
            getTransactionGroupByMonthUseCase.invoke()
        ) { currency, result ->
            val values: MutableList<HistoryListItem> = arrayListOf()
            result.forEach { value ->

                val titleValue: UiText = if (value.monthText == SimpleDateFormat(
                        "MMM yyyy",
                        Locale.getDefault()
                    ).format(Date())
                ) {
                    UiText.StringResource(R.string.this_month)
                } else {
                    UiText.DynamicString(value.monthText)
                }

                val totalAmount = value.transaction.sumOf {
                    if (it.category.type == CategoryType.INCOME) {
                        it.amount
                    } else {
                        -it.amount
                    }
                }

                val history = HistoryListItem(
                    titleValue,
                    getCurrency(currency, totalAmount),
                    value.transaction.map {
                        it.toTransactionUIModel(currency)
                    },
                    expanded = false
                )
                values.add(history)
            }
            _transactionHistory.send(values)
        }.launchIn(viewModelScope)
    }

    fun loadTransactions() {

    }

    fun openTransactionEdit(transactionId: String) {
        viewModelScope.launch {
            when (val response = findTransactionByIdUseCase.invoke(transactionId)) {
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


fun Transaction.toTransactionUIModel(currency: Currency): TransactionUIModel {
    return TransactionUIModel(
        this.id,
        getCurrency(currency, this.amount),
        if (this.notes.isBlank()) {
            null
        } else {
            UiText.DynamicString(this.notes)
        },
        this.category.name,
        this.category.type,
        this.category.iconBackgroundColor,
        this.category.iconName,
        accountName = this.fromAccount.name,
        this.fromAccount.iconName,
        accountColor = this.fromAccount.iconBackgroundColor,
        this.createdOn.toTransactionDate(),
    )
}