package com.naveenapps.expensemanager.feature.transaction.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.common.utils.UiState
import com.naveenapps.expensemanager.core.common.utils.getAmountTextColor
import com.naveenapps.expensemanager.core.common.utils.toCompleteDateWithDate
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetTransactionWithFilterUseCase
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.TransactionGroup
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.model.toTransactionUIModel
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
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
    getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val _transactions = MutableStateFlow<UiState<List<TransactionGroup>>>(
        UiState.Loading,
    )
    val transactions = _transactions.asStateFlow()

    init {

        combine(
            getCurrencyUseCase.invoke(),
            getTransactionWithFilterUseCase.invoke(),
        ) { currency, transactions ->
            _transactions.value = if (transactions.isNullOrEmpty()) {
                UiState.Empty
            } else {
                UiState.Success(
                    transactions.groupBy {
                        it.createdOn.toCompleteDateWithDate()
                    }.map {
                        val totalAmount = it.value.toTransactionSum()
                        TransactionGroup(
                            date = it.key,
                            amountTextColor = totalAmount.getAmountTextColor(),
                            totalAmount = getFormattedAmountUseCase.invoke(totalAmount, currency),
                            transactions = it.value.map { transaction ->
                                transaction.toTransactionUIModel(
                                    getFormattedAmountUseCase.invoke(
                                        transaction.amount.amount,
                                        currency,
                                    ),
                                )
                            },
                        )
                    },
                )
            }
        }.launchIn(viewModelScope)
    }

    fun openCreateScreen(transactionId: String? = null) {
        appComposeNavigator.navigate(
            ExpenseManagerScreens.TransactionCreate.createRoute(transactionId ?: ""),
        )
    }

    fun closePage() {
        appComposeNavigator.popBackStack()
    }
}

fun List<Transaction>.toTransactionSum() =
    this.sumOf {
        when (it.type) {
            TransactionType.INCOME -> {
                it.amount.amount
            }

            TransactionType.EXPENSE -> {
                it.amount.amount * -1
            }

            TransactionType.TRANSFER -> {
                0.0
            }
        }
    }
