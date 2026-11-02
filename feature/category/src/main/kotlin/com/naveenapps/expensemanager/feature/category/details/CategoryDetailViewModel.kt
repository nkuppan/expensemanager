package com.naveenapps.expensemanager.feature.category.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.category.FindCategoryByIdFlowUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.FormatAmountInCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currencyapi.ConvertAmountUseCase
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
    private val convertAmountUseCase: ConvertAmountUseCase,
    private val formatAmountInCurrencyUseCase: FormatAmountInCurrencyUseCase,
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
                val inCategory = sameTypeTransactions.filter { it.categoryId == category.id }

                // Totals are converted into the display currency before summing; with a
                // rate missing they're shown as unavailable, never as a mixed-currency sum.
                val total = convertAmountUseCase.total(
                    sameTypeTransactions.map { it.amount.amount to it.fromCurrencyCode },
                    currency.code,
                )
                val categoryTotal = convertAmountUseCase.total(
                    inCategory.map { it.amount.amount to it.fromCurrencyCode },
                    currency.code,
                )

                // Each row shows what was recorded, in its own currency.
                val categoryTransactions = inCategory.map { transaction ->
                    transaction.toTransactionUIModel(
                        formatAmountInCurrencyUseCase(
                            transaction.amount.amount,
                            transaction.fromCurrencyCode,
                        ),
                    )
                }

                val categoryValue = categoryTotal.value
                val totalValue = total.value
                val percent = if (categoryValue != null && totalValue != null && totalValue > 0.0) {
                    (categoryValue / totalValue).toFloat() * 100
                } else {
                    0f
                }

                // The category is shown even with nothing in the period, so the screen keeps its
                // title, summary and edit button (with a zero total) instead of losing them.
                _state.update {
                    it.copy(
                        categoryTransaction = CategoryTransaction(
                            category = category,
                            percent = percent,
                            amount = getFormattedAmountUseCase(categoryTotal, currency),
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
