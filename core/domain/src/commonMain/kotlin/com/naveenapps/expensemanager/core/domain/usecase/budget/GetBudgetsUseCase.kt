package com.naveenapps.expensemanager.core.domain.usecase.budget

import androidx.compose.runtime.Stable
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetTransactionWithFilterUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.toTransactionUIModel
import com.naveenapps.expensemanager.core.foundation.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Budget
import com.naveenapps.expensemanager.core.model.BudgetPeriod
import com.naveenapps.expensemanager.core.model.BudgetStatus
import com.naveenapps.expensemanager.core.model.BudgetTitle
import com.naveenapps.expensemanager.core.model.CategoryTransactionState
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.TransactionUiItem
import com.naveenapps.expensemanager.core.model.isExpense
import com.naveenapps.expensemanager.core.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn

class GetBudgetsUseCase(
    private val budgetRepository: BudgetRepository,
    private val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val getBudgetTransactionsUseCase: GetBudgetTransactionsUseCase,
    private val appCoroutineDispatchers: AppCoroutineDispatchers,
) {
    operator fun invoke(): Flow<List<BudgetUiModel>> = combine(
        getCurrencyUseCase.invoke(),
        getTransactionWithFilterUseCase.invoke(),
        budgetRepository.getBudgets(),
    ) { currency, _, budgets ->
        budgets.map { budget ->
            val transactions = when (val response = getBudgetTransactionsUseCase.invoke(budget)) {
                is Resource.Error -> null
                is Resource.Success -> response.data.filter { it.type.isExpense() }
            }
            val transactionAmount = transactions?.sumOf { it.amount.amount } ?: 0.0
            val percent = budgetSpentPercent(transactionAmount, budget.amount)
            budget.toBudgetUiModel(
                title = budget.title(),
                budgetAmount = getFormattedAmountUseCase(budget.amount, currency),
                transactionAmount = getFormattedAmountUseCase(transactionAmount, currency),
                percent,
                transactions?.map {
                    it.toTransactionUIModel(getFormattedAmountUseCase(it.amount.amount, currency))
                },
            )
        }
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
    status = budgetStatus(percent),
    amount = budgetAmount,
    transactionAmount = transactionAmount,
    percent = percent,
    transactions = transactions,
)

/**
 * Share of the budget spent, in percent. A budget of 0 can't be divided by: anything spent
 * against it counts as 100% (over), nothing spent as 0%.
 */
fun budgetSpentPercent(spent: Double, budgetAmount: Double): Float = when {
    budgetAmount > 0.0 -> (spent / budgetAmount).toFloat() * 100
    spent > 0.0 -> 100f
    else -> 0f
}

/**
 * Status by share spent: safe up to 35%, watch up to 60%, risk up to 85%, over above. The UI shows
 * them green, light green, orange and red. (The old integer ranges left gaps, so e.g. 35.5% or
 * 60.4% showed red.)
 */
fun budgetStatus(percent: Float): BudgetStatus = when {
    percent <= 35f -> BudgetStatus.SAFE
    percent <= 60f -> BudgetStatus.WATCH
    percent <= 85f -> BudgetStatus.RISK
    else -> BudgetStatus.OVER
}

@Stable
data class BudgetUiModel(
    val id: String,
    val title: BudgetTitle,
    val selectedMonth: String,
    val periodType: BudgetPeriod = BudgetPeriod.MONTHLY,
    val status: BudgetStatus,
    val amount: Amount,
    val transactionAmount: Amount,
    val percent: Float,
    val transactions: List<TransactionUiItem>? = null,
    /** Spending per category (pie chart and list); only filled for a budget's details. */
    val categorySpending: CategoryTransactionState? = null,
)
