package com.nkuppan.expensemanager.feature.transaction.list

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.core.model.PaymentMode
import com.nkuppan.expensemanager.core.model.Resource
import com.nkuppan.expensemanager.core.model.Transaction
import com.nkuppan.expensemanager.core.ui.utils.UiText
import com.nkuppan.expensemanager.core.ui.utils.getCurrency
import com.nkuppan.expensemanager.data.usecase.settings.currency.GetCurrencyUseCase
import com.nkuppan.expensemanager.data.usecase.transaction.GetTransactionByIdUseCase
import com.nkuppan.expensemanager.data.usecase.transaction.GetTransactionByNameUseCase
import com.nkuppan.expensemanager.data.utils.toTransactionDate
import com.nkuppan.expensemanager.feature.transaction.R
import com.nkuppan.expensemanager.feature.transaction.history.TransactionUIModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase,
    private val getTransactionByNameUseCase: GetTransactionByNameUseCase
) : ViewModel() {

    var categoryId: Int = -1

    private val _transactionList = Channel<List<TransactionUIModel>>()
    val transactionList = _transactionList.receiveAsFlow()

    private val _openTransaction = Channel<Transaction>()
    val openTransaction = _openTransaction.receiveAsFlow()

    private val _errorMessage = Channel<UiText>()
    val errorMessage = _errorMessage.receiveAsFlow()

    private var currencySymbol: Int = R.string.default_currency_type

    private val searchText = MutableStateFlow<String?>("")

    init {
        viewModelScope.launch {
            getCurrencyUseCase.invoke().collectLatest {
                currencySymbol = it.type
            }
        }

        searchText.flatMapLatest {
            getTransactionByNameUseCase.invoke(it)
        }.onEach { transactions ->
            _transactionList.send(
                transactions.map {
                    TransactionUIModel(
                        it.id,
                        getCurrency(currencySymbol, it.amount),
                        if (it.notes.isBlank()) {
                            UiText.StringResource(R.string.not_assigned)
                        } else {
                            UiText.DynamicString(it.notes)
                        },
                        it.category.name,
                        it.category.backgroundColor,
                        it.account.type.getPaymentModeIcon(),
                        it.updatedOn.toTransactionDate(),
                    )
                }
            )
        }.launchIn(viewModelScope)
    }

    fun loadTransactions(searchText: String = "") {
        this@TransactionListViewModel.searchText.value = searchText
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

@DrawableRes
fun PaymentMode.getPaymentModeIcon(): Int {
    return when (this) {
        PaymentMode.CARD -> R.drawable.ic_card
        PaymentMode.WALLET -> R.drawable.ic_wallet
        PaymentMode.UPI -> R.drawable.ic_upi
        PaymentMode.CHEQUE -> R.drawable.ic_cheque
        PaymentMode.INTERNET_BANKING -> R.drawable.ic_netbanking
        PaymentMode.BANK_ACCOUNT -> R.drawable.ic_bank
        PaymentMode.NONE -> R.drawable.ic_wallet
    }
}