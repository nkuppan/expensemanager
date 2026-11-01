package com.naveenapps.expensemanager.feature.category.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.category.FindCategoryByIdFlowUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetTransactionWithFilterUseCase
import com.naveenapps.expensemanager.core.model.CategoryTransaction
import com.naveenapps.expensemanager.core.model.toTransactionUIModel
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerArgsNames
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update

class CategoryDetailViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val findCategoryByIdFlowUseCase: FindCategoryByIdFlowUseCase,
    private val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val _state = MutableStateFlow(
        CategoryDetailsState(
            categoryTransaction = null,
            transactions = emptyList(),
        ),
    )
    val state = _state.asStateFlow()

    init {
        savedStateHandle.get<String>(ExpenseManagerArgsNames.ID)?.let {
            combine(
                getCurrencyUseCase.invoke(),
                findCategoryByIdFlowUseCase.invoke(it),
                getTransactionWithFilterUseCase.invoke(),
            ) { currency, category, transactions ->
                if (category == null) {
                    _state.update { it.copy(categoryTransaction = null, transactions = emptyList()) }
                    return@combine
                }

                // Only the same type counts towards the share: an expense category's share of all
                // spending, an income category's share of all income, in the selected period.
                val sameTypeTransactions = transactions.orEmpty().filter { transaction ->
                    transaction.type.ordinal == category.type.ordinal
                }
                val total = sameTypeTransactions.sumOf { it.amount.amount }

                val categoryTransactions = sameTypeTransactions
                    .filter { it.categoryId == category.id }
                    .map { transaction ->
                        transaction.toTransactionUIModel(
                            getFormattedAmountUseCase.invoke(
                                transaction.amount.amount,
                                currency,
                            ),
                        )
                    }

                val categoryAmount = categoryTransactions.sumOf { it.amount.amount }

                // The category is shown even with nothing in the period, so the screen keeps its
                // title, summary and edit button (with a zero total) instead of losing them.
                _state.update {
                    it.copy(
                        categoryTransaction = CategoryTransaction(
                            category = category,
                            percent = if (total > 0.0) (categoryAmount / total).toFloat() * 100 else 0f,
                            amount = getFormattedAmountUseCase.invoke(
                                amount = categoryAmount,
                                currency = currency,
                            ),
                            transaction = sameTypeTransactions,
                        ),
                        transactions = categoryTransactions,
                    )
                }
            }.launchIn(viewModelScope)
        }
    }

    fun openCategoryEditScreen() {
        val categoryId = savedStateHandle.get<String>(ExpenseManagerArgsNames.ID)
        categoryId ?: return

        appComposeNavigator.navigate(
            ExpenseManagerScreens.CategoryCreate(categoryId),
        )
    }

    fun openTransactionCreateScreen(transactionId: String?) {
        appComposeNavigator.navigate(
            ExpenseManagerScreens.TransactionCreate(transactionId),
        )
    }

    fun closePage() {
        appComposeNavigator.popBackStack()
    }

    fun processAction(action: CategoryDetailsAction) {
        when (action) {
            CategoryDetailsAction.ClosePage -> closePage()

            CategoryDetailsAction.OpenCategoryEdit -> openCategoryEditScreen()

            CategoryDetailsAction.OpenTransactionCreate -> {
                openTransactionCreateScreen(null)
            }

            is CategoryDetailsAction.OpenTransactionEdit -> {
                openTransactionCreateScreen(action.id)
            }
        }
    }
}
