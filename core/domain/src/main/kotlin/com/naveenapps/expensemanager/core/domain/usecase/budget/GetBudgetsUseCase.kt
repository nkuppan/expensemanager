package com.naveenapps.expensemanager.core.domain.usecase.budget

import androidx.compose.runtime.Stable
import com.naveenapps.expensemanager.core.common.R
import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetTransactionWithFilterUseCase
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Budget
import com.naveenapps.expensemanager.core.model.BudgetPeriod
import com.naveenapps.expensemanager.core.model.BudgetTitle
import com.naveenapps.expensemanager.core.model.CategoryTransactionState
import com.naveenapps.expensemanager.core.model.TransactionUiItem
import com.naveenapps.expensemanager.core.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn

class GetBudgetsUseCase(
    private val budgetRepository: BudgetRepository,
    private val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
    private val calculateBudgetUseCase: CalculateBudgetUseCase,
    private val appCoroutineDispatchers: AppCoroutineDispatchers,
) {
    operator fun invoke(): Flow<List<BudgetUiModel>> = combine(
        getTransactionWithFilterUseCase.invoke(),
        budgetRepository.getBudgets(),
    ) { _, budgets ->
        budgets.map { budget -> calculateBudgetUseCase(budget) }
    }.flowOn(appCoroutineDispatchers.computation)
}

fun Budget.toBudgetUiModel(
    title: BudgetTitle,
    budgetAmount: Amount,
    transactionAmount: Amount,
    percent: Float,
    transactions: List<TransactionUiItem>? = null,
) = BudgetUiModel(
    id = this.id,
    title = title,
    selectedMonth = this.selectedMonth,
    periodType = this.periodType,
    progressBarColor = when {
        percent < 0f -> R.color.green_500
        percent in 0f..35f -> R.color.green_500
        percent in 36f..60f -> R.color.light_green_500
        percent in 61f..85f -> R.color.orange_500
        else -> R.color.red_500
    },
    amount = budgetAmount,
    transactionAmount = transactionAmount,
    percent = percent,
    transactions = transactions,
)

@Stable
data class BudgetUiModel(
    val id: String,
    val title: BudgetTitle,
    val selectedMonth: String,
    val periodType: BudgetPeriod = BudgetPeriod.MONTHLY,
    val progressBarColor: Int,
    val amount: Amount,
    val transactionAmount: Amount,
    val percent: Float,
    val transactions: List<TransactionUiItem>? = null,
    /** Spending includes other currencies, converted at today's rate. */
    val convertedAtCurrentRates: Boolean = false,
    /** Conversion is disabled and expenses in other currencies were left out of the spending. */
    val otherCurrenciesExcluded: Boolean = false,
    /** Spending per category (pie chart and list); only filled for a budget's details. */
    val categorySpending: CategoryTransactionState? = null,
)
