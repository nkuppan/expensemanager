package com.nkuppan.expensemanager.presentation.budget.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.core.ui.utils.UiText
import com.nkuppan.expensemanager.core.ui.utils.getCurrency
import com.nkuppan.expensemanager.domain.model.Budget
import com.nkuppan.expensemanager.domain.model.Currency
import com.nkuppan.expensemanager.domain.model.UiState
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
) : ViewModel() {

    private val _errorMessage = MutableSharedFlow<UiText>()
    val errorMessage = _errorMessage.asSharedFlow()

    private val _budgets = MutableStateFlow<UiState<List<BudgetUiModel>>>(UiState.Loading)
    val budgets = _budgets.asStateFlow()

    init {

        getCurrencyUseCase.invoke().combine(getBudgetsUseCase.invoke()) { currency, budget ->
            currency to budget
        }.map { currencyAndBudgetPair ->

            val (currency, budget) = currencyAndBudgetPair

            _budgets.value = if (budget.isEmpty()) {
                UiState.Empty
            } else {
                UiState.Success(
                    budget.map {
                        it.toBudgetUiModel(currency)
                    }
                )
            }
        }.launchIn(viewModelScope)
    }
}

fun Budget.toBudgetUiModel(currency: Currency) = BudgetUiModel(
    id = this.id,
    name = this.name,
    icon = this.iconName,
    iconBackgroundColor = this.iconBackgroundColor,
    amount = getCurrency(
        currency,
        this.amount
    ),
)


data class BudgetUiModel(
    val id: String,
    val name: String,
    val icon: String,
    val iconBackgroundColor: String,
    val amount: UiText,
)