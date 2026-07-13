package com.naveenapps.expensemanager.core.domain.usecase.budget

import androidx.compose.runtime.Stable
import com.naveenapps.expensemanager.core.common.R
import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.common.utils.fromMonthAndYearKey
import com.naveenapps.expensemanager.core.common.utils.toMonthAndYearKey
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetTransactionWithFilterUseCase
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Budget
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.TransactionUiItem
import com.naveenapps.expensemanager.core.model.isExpense
import com.naveenapps.expensemanager.core.model.toTransactionUIModel
import com.naveenapps.expensemanager.core.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GetBudgetsUseCase(
    private val budgetRepository: BudgetRepository,
    private val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val getBudgetTransactionsUseCase: GetBudgetTransactionsUseCase,
    private val appCoroutineDispatchers: AppCoroutineDispatchers
) {
    operator fun invoke(): Flow<List<BudgetUiModel>> {
        return combine(
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
                val percent = (transactionAmount / budget.amount).toFloat() * 100
                budget.toBudgetUiModel(
                    name = budgetName(budget.selectedMonth),
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
}

private val shortMonthFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())

fun budgetName(selectedMonth: String): String {
    val currentMonth = Date().toMonthAndYearKey()
    return if (selectedMonth == currentMonth) {
        "This Month Budget"
    } else {
        val short = selectedMonth.fromMonthAndYearKey()
            ?.let { shortMonthFormat.format(it) }
            ?: selectedMonth
        "$short Budget"
    }
}

fun Budget.toBudgetUiModel(
    name: String,
    budgetAmount: Amount,
    transactionAmount: Amount,
    percent: Float,
    transactions: List<TransactionUiItem>? = null,
) = BudgetUiModel(
    id = this.id,
    name = name,
    selectedMonth = this.selectedMonth,
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
    val name: String,
    val selectedMonth: String,
    val progressBarColor: Int,
    val amount: Amount,
    val transactionAmount: Amount,
    val percent: Float,
    val transactions: List<TransactionUiItem>? = null,
)
