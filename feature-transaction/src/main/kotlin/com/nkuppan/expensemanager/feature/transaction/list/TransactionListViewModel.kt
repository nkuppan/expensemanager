package com.nkuppan.expensemanager.feature.transaction.list

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.core.model.PaymentMode
import com.nkuppan.expensemanager.core.model.Resource
import com.nkuppan.expensemanager.core.model.Transaction
import com.nkuppan.expensemanager.core.model.UiState
import com.nkuppan.expensemanager.core.ui.utils.UiText
import com.nkuppan.expensemanager.core.ui.utils.getCurrency
import com.nkuppan.expensemanager.data.usecase.settings.currency.GetCurrencyUseCase
import com.nkuppan.expensemanager.data.usecase.transaction.GetTransactionByIdUseCase
import com.nkuppan.expensemanager.data.usecase.transaction.GetTransactionByNameUseCase
import com.nkuppan.expensemanager.data.utils.toTransactionDate
import com.nkuppan.expensemanager.feature.transaction.R
import com.nkuppan.expensemanager.feature.transaction.history.TransactionUIModel
import com.nkuppan.expensemanager.feature.transaction.history.toTransactionUIModel
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

    private val _transactions = MutableStateFlow<UiState<List<TransactionUIModel>>>(
        UiState.Loading
    )
    val transactions = _transactions.asStateFlow()

    private val _openTransaction = Channel<Transaction>()
    val openTransaction = _openTransaction.receiveAsFlow()

    private val _errorMessage = Channel<UiText>()
    val errorMessage = _errorMessage.receiveAsFlow()

    private var currencySymbol: Int = com.nkuppan.expensemanager.data.R.string.default_currency_type

    private val searchText = MutableStateFlow<String?>("")

    init {
        getCurrencyUseCase.invoke().onEach {
            currencySymbol = it.type
        }.launchIn(viewModelScope)

        searchText.flatMapLatest {
            getTransactionByNameUseCase.invoke(it)
        }.onEach { transactions ->
            _transactions.value =
                UiState.Success(transactions.map {
                    it.toTransactionUIModel(currencySymbol)
                })
        }.launchIn(viewModelScope)
    }

    fun updateSearchText(searchText: String = "") {
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
        PaymentMode.CARD -> com.nkuppan.expensemanager.feature.account.R.drawable.ic_card
        PaymentMode.WALLET -> com.nkuppan.expensemanager.feature.account.R.drawable.ic_wallet
        PaymentMode.UPI -> com.nkuppan.expensemanager.feature.account.R.drawable.ic_upi
        PaymentMode.CHEQUE -> com.nkuppan.expensemanager.feature.account.R.drawable.ic_cheque
        PaymentMode.INTERNET_BANKING -> com.nkuppan.expensemanager.feature.account.R.drawable.ic_netbanking
        PaymentMode.BANK_ACCOUNT -> com.nkuppan.expensemanager.feature.account.R.drawable.ic_bank
        PaymentMode.NONE -> com.nkuppan.expensemanager.feature.account.R.drawable.ic_wallet
    }
}