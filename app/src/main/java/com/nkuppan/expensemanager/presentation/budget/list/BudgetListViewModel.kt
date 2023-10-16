package com.nkuppan.expensemanager.presentation.budget.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.common.ui.utils.UiText
import com.nkuppan.expensemanager.common.ui.utils.getCurrency
import com.nkuppan.expensemanager.domain.model.Budget
import com.nkuppan.expensemanager.domain.model.Currency
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.model.Transaction
import com.nkuppan.expensemanager.domain.model.TransactionType
import com.nkuppan.expensemanager.domain.model.UiState
import com.nkuppan.expensemanager.domain.usecase.budget.GetBudgetTransactionsUseCase
import com.nkuppan.expensemanager.domain.usecase.budget.GetBudgetsUseCase
import com.nkuppan.expensemanager.domain.usecase.settings.currency.GetCurrencyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class BudgetListViewModel @Inject constructor(
    getBudgetsUseCase: GetBudgetsUseCase,
    getCurrencyUseCase: GetCurrencyUseCase,
    getBudgetTransactionsUseCase: GetBudgetTransactionsUseCase,
) : ViewModel() {

    private val _errorMessage = MutableSharedFlow<UiText>()
    val errorMessage = _errorMessage.asSharedFlow()

    private val _budgets = MutableStateFlow<UiState<List<BudgetUiModel>>>(UiState.Loading)
    val budgets = _budgets.asStateFlow()

    init {

        getCurrencyUseCase.invoke().combine(getBudgetsUseCase.invoke()) { currency, budget ->
            currency to budget
        }.map { currencyAndBudgetPair ->

            val (currency, budgets) = currencyAndBudgetPair

            _budgets.value = if (budgets.isEmpty()) {
                UiState.Empty
            } else {
                UiState.Success(
                    budgets.map {
                        val transactionAmount =
                            when (val response = getBudgetTransactionsUseCase(it)) {
                                is Resource.Error -> {
                                    0.0
                                }

                                is Resource.Success -> {
                                    response.data.toTransactionSum()
                                }
                            }
                        it.toBudgetUiModel(currency, transactionAmount)
                    }
                )
            }
        }.launchIn(viewModelScope)
    }
}

fun List<Transaction>.toTransactionSum(): Double {
    val finalAmount = this.sumOf {
        when (it.type) {
            TransactionType.INCOME -> {
                it.amount
            }

            TransactionType.EXPENSE -> {
                it.amount * -1
            }

            TransactionType.TRANSFER -> {
                0.0
            }
        }
    }

    return if (finalAmount < 0) {
        finalAmount * -1
    } else {
        0.0
    }
}

fun Budget.toBudgetUiModel(
    currency: Currency,
    transactionAmount: Double,
) = BudgetUiModel(
    id = this.id,
    name = this.name,
    icon = this.iconName,
    iconBackgroundColor = this.iconBackgroundColor,
    amount = getCurrency(
        currency,
        this.amount
    ),
    transactionAmount = getCurrency(
        currency,
        transactionAmount
    ),
    percent = (transactionAmount / this.amount).toFloat() * 100
)


data class BudgetUiModel(
    val id: String,
    val name: String,
    val icon: String,
    val iconBackgroundColor: String,
    val amount: UiText,
    val transactionAmount: UiText,
    val percent: Float,
)