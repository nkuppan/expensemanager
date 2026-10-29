package com.naveenapps.expensemanager.feature.budget.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.budget.BudgetUiModel
import com.naveenapps.expensemanager.core.domain.usecase.budget.GetBudgetDetailUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerArgsNames
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.feature.budget.components.BudgetProgress
import com.naveenapps.expensemanager.feature.budget.components.toBudgetProgress
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update

data class BudgetDetailState(
    val budget: BudgetUiModel? = null,
    /** The gauge: what's left, the daily allowance, ... */
    val progress: BudgetProgress? = null,
    /** Set when a category is tapped: only its transactions are listed. */
    val selectedCategoryId: String? = null,
)

class BudgetDetailViewModel(
    savedStateHandle: SavedStateHandle,
    getBudgetDetailUseCase: GetBudgetDetailUseCase,
    getCurrencyUseCase: GetCurrencyUseCase,
    getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val _state = MutableStateFlow(BudgetDetailState())
    val state = _state.asStateFlow()

    init {
        savedStateHandle.get<String>(ExpenseManagerArgsNames.ID)?.let { id ->
            combine(getBudgetDetailUseCase.invoke(id), getCurrencyUseCase.invoke()) { budget, currency ->
                val progress = budget?.toBudgetProgress(
                    today = LocalDate.now(),
                    format = { value -> getFormattedAmountUseCase(value, currency).amountString.orEmpty() },
                )
                _state.update { state ->
                    // Drop a filter whose category no longer has spending in this budget.
                    val categories = budget?.categorySpending?.categoryTransactions.orEmpty()
                    val selected = state.selectedCategoryId?.takeIf { selectedId ->
                        categories.any { it.category.id == selectedId }
                    }
                    state.copy(budget = budget, progress = progress, selectedCategoryId = selected)
                }
            }.launchIn(viewModelScope)
        }
    }

    /** Tapping a category lists only its transactions; tapping it again lists all of them. */
    fun toggleCategory(categoryId: String) {
        _state.update {
            it.copy(selectedCategoryId = if (it.selectedCategoryId == categoryId) null else categoryId)
        }
    }

    fun clearCategoryFilter() {
        _state.update { it.copy(selectedCategoryId = null) }
    }

    fun closePage() {
        appComposeNavigator.popBackStack()
    }

    fun openTransactionCreateScreen(transactionId: String? = null) {
        appComposeNavigator.navigate(
            ExpenseManagerScreens.TransactionCreate(transactionId),
        )
    }

    fun openBudgetCreateScreen() {
        _state.value.budget?.id?.let {
            appComposeNavigator.navigate(ExpenseManagerScreens.BudgetCreate(it))
        }
    }
}
