package com.nkuppan.expensemanager.presentation.transaction.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.domain.model.UiState
import com.nkuppan.expensemanager.domain.usecase.settings.currency.GetCurrencyUseCase
import com.nkuppan.expensemanager.domain.usecase.transaction.GetTransactionByNameUseCase
import com.nkuppan.expensemanager.presentation.transaction.history.TransactionUIModel
import com.nkuppan.expensemanager.presentation.transaction.history.toTransactionUIModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionListViewModel @Inject constructor(
    getCurrencyUseCase: GetCurrencyUseCase,
    getTransactionByNameUseCase: GetTransactionByNameUseCase
) : ViewModel() {

    private val _transactions = MutableStateFlow<UiState<List<TransactionUIModel>>>(
        UiState.Loading
    )
    val transactions = _transactions.asStateFlow()

    init {

        getCurrencyUseCase.invoke().combine(
            getTransactionByNameUseCase.invoke(null)
        ) { currency, transactions ->
            _transactions.value = if (transactions.isEmpty()) {
                UiState.Empty
            } else {
                UiState.Success(
                    transactions.map {
                        it.toTransactionUIModel(currency)
                    }
                )
            }
        }.launchIn(viewModelScope)
    }
}