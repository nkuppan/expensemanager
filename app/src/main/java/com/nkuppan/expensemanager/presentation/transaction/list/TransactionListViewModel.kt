package com.nkuppan.expensemanager.presentation.transaction.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.domain.model.TransactionUIModel
import com.nkuppan.expensemanager.domain.model.UiState
import com.nkuppan.expensemanager.domain.model.toTransactionUIModel
import com.nkuppan.expensemanager.domain.usecase.settings.currency.GetCurrencyUseCase
import com.nkuppan.expensemanager.domain.usecase.transaction.GetTransactionByNameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import javax.inject.Inject

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