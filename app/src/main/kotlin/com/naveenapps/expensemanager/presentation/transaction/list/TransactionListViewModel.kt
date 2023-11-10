package com.naveenapps.expensemanager.presentation.transaction.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.R
import com.naveenapps.expensemanager.core.common.utils.toCompleteDate
import com.naveenapps.expensemanager.domain.model.TransactionUiState
import com.naveenapps.expensemanager.domain.model.UiState
import com.naveenapps.expensemanager.domain.model.toTransactionUIModel
import com.naveenapps.expensemanager.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.domain.usecase.transaction.GetTransactionWithFilterUseCase
import com.naveenapps.expensemanager.presentation.budget.list.toTransactionSum
import com.naveenapps.expensemanager.ui.utils.getCurrency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import javax.inject.Inject

@HiltViewModel
class TransactionListViewModel @Inject constructor(
    getCurrencyUseCase: GetCurrencyUseCase,
    getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase
) : ViewModel() {

    private val _transactions = MutableStateFlow<UiState<List<TransactionUiState>>>(
        UiState.Loading
    )
    val transactions = _transactions.asStateFlow()

    init {

        getCurrencyUseCase.invoke().combine(
            getTransactionWithFilterUseCase.invoke()
        ) { currency, transactions ->
            _transactions.value = if (transactions.isNullOrEmpty()) {
                UiState.Empty
            } else {
                UiState.Success(
                    transactions.groupBy {
                        it.createdOn.toCompleteDate()
                    }.map {
                        val totalAmount = it.value.toTransactionSum()
                        TransactionUiState(
                            date = it.key,
                            amountTextColor = totalAmount.getAmountTextColor(),
                            totalAmount = getCurrency(currency, totalAmount),
                            transactions = it.value.map { it.toTransactionUIModel(currency) },
                        )
                    }
                )
            }
        }.launchIn(viewModelScope)
    }
}

fun Double.getAmountTextColor() = if (this < 0) {
    R.color.red_500
} else {
    R.color.green_500
}