package com.naveenapps.expensemanager.core.domain.usecase.budget

import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.FormatAmountInCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currencyapi.ConvertAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetTransactionWithFilterUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.groupByCategory
import com.naveenapps.expensemanager.core.domain.usecase.transaction.toChartModel
import com.naveenapps.expensemanager.core.model.CategoryTransactionState
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.isExpense
import com.naveenapps.expensemanager.core.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetBudgetDetailUseCase(
    private val budgetRepository: BudgetRepository,
    private val calculateBudgetUseCase: CalculateBudgetUseCase,
    private val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
    private val getBudgetTransactionsUseCase: GetBudgetTransactionsUseCase,
    private val convertAmountUseCase: ConvertAmountUseCase,
    private val formatAmountInCurrencyUseCase: FormatAmountInCurrencyUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
) {
    operator fun invoke(budgetId: String): Flow<BudgetUiModel?> = combine(
        budgetRepository.findBudgetByIdFlow(budgetId),
        getTransactionWithFilterUseCase.invoke(),
    ) { budget, _ ->
        budget?.let {
            val uiModel = calculateBudgetUseCase(it)
            val expenses = when (val response = getBudgetTransactionsUseCase(it)) {
                is Resource.Error -> emptyList()
                is Resource.Success -> response.data.filter { txn -> txn.type.isExpense() }
            }
            // Grouped in the budget's own currency, like its limit and spending.
            val currency = formatAmountInCurrencyUseCase.currencyFor(it.currencyCode)
            val (byCategory, total) = expenses.groupByCategory(
                categoryType = CategoryType.EXPENSE,
                currencyCode = it.currencyCode,
                convertAmountUseCase = convertAmountUseCase,
            ) { amount -> getFormattedAmountUseCase(amount, currency) }
            uiModel.copy(
                categorySpending = CategoryTransactionState(
                    totalAmount = getFormattedAmountUseCase(total, currency),
                    pieChartData = byCategory.map { item -> item.toChartModel() },
                    categoryTransactions = byCategory,
                    hideValues = total.value == null,
                    categoryType = CategoryType.EXPENSE,
                ),
            )
        }
    }
}
