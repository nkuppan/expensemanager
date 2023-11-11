package com.naveenapps.expensemanager.presentation.transaction.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.common.utils.UiState
import com.naveenapps.expensemanager.core.common.utils.getAmountTextColor
import com.naveenapps.expensemanager.core.common.utils.toCompleteDate
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetTransactionWithFilterUseCase
import com.naveenapps.expensemanager.core.model.TransactionUiState
import com.naveenapps.expensemanager.core.model.toTransactionUIModel
import com.naveenapps.expensemanager.presentation.budget.list.toTransactionSum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import javax.inject.Inject

@HiltViewModel
class TransactionListViewModel @Inject constructor(
    getCurrencyUseCase: GetCurrencyUseCase,
    getFormattedAmountUseCase: GetFormattedAmountUseCase,
    getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase
) : ViewModel() {

    private val _transactions = MutableStateFlow<UiState<List<TransactionUiState>>>(
        UiState.Loading
    )
    val transactions = _transactions.asStateFlow()

    init {

        combine(
            getCurrencyUseCase.invoke(),
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
                            totalAmount = getFormattedAmountUseCase.invoke(totalAmount, currency),
                            transactions = it.value.map { transaction ->
                                transaction.toTransactionUIModel(
                                    getFormattedAmountUseCase.invoke(
                                        transaction.amount.amount,
                                        currency
                                    )
                                )
                            },
                        )
                    }
                )
            }
        }.launchIn(viewModelScope)
    }
}