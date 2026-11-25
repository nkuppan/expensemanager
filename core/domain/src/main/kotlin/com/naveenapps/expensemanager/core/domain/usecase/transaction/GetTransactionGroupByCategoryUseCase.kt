package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.domain.usecase.category.GetAllCategoryUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.CategoryTransaction
import com.naveenapps.expensemanager.core.model.CategoryTransactionState
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.PieChartData
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.getDummyPieChartData
import com.naveenapps.expensemanager.core.model.isTransfer
import com.naveenapps.expensemanager.core.model.netExpense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn

class GetTransactionGroupByCategoryUseCase(
    private val getAllCategoryUseCase: GetAllCategoryUseCase,
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
    private val appCoroutineDispatchers: AppCoroutineDispatchers,
) {
    fun invoke(categoryType: CategoryType): Flow<CategoryTransactionState> = combine(
        getCurrencyUseCase.invoke(),
        getTransactionWithFilterUseCase.invoke(),
    ) { currency, transactions ->

        val (categoryTransactions, totalAmount) = transactions.orEmpty().groupByCategory(categoryType) {
            getFormattedAmountUseCase.invoke(amount = it, currency = currency)
        }

        val pieChartData: List<PieChartData>
        val newCategoryTransaction: List<CategoryTransaction>

        if (categoryTransactions.isEmpty()) {
            val categories = getAllCategoryUseCase.invoke().firstOrNull()
                ?.filter { it.type == categoryType }
                .orEmpty()
            val equalShare = if (categories.isNotEmpty()) 100f / categories.size else 0f
            newCategoryTransaction = categories.map { category ->
                CategoryTransaction(
                    category = category,
                    percent = 0f,
                    amount = getFormattedAmountUseCase.invoke(amount = 0.0, currency = currency),
                    transaction = emptyList(),
                )
            }
            pieChartData = categories.map { getDummyPieChartData(it.name, equalShare, it.defaultKey) }
        } else {
            newCategoryTransaction = categoryTransactions
            pieChartData = categoryTransactions.map { it.toChartModel() }
        }

        CategoryTransactionState(
            pieChartData = pieChartData,
            totalAmount = getFormattedAmountUseCase.invoke(amount = totalAmount, currency = currency),
            categoryTransactions = newCategoryTransaction,
            hideValues = categoryTransactions.isEmpty(),
            categoryType = categoryType,
        )
    }.flowOn(appCoroutineDispatchers.computation)
}

fun CategoryTransaction.toChartModel(): PieChartData = PieChartData(
    name = this.category.name,
    value = this.percent,
    color = this.category.storedIcon.backgroundColor,
    categoryDefaultKey = this.category.defaultKey,
)

/**
 * Spending (or income) of [categoryType] per category, largest share first, with the total.
 * Transfers are left out. For expenses, refunds subtract from their category so the share is
 * the net amount; a category whose spending is fully refunded disappears. Shared by the
 * Categories tab and a budget's details.
 */
fun List<Transaction>.groupByCategory(
    categoryType: CategoryType,
    formatAmount: (Double) -> Amount,
): Pair<List<CategoryTransaction>, Double> {
    // Single pass: group by category, keep the requested category type.
    val byCategory = filterNot { it.type.isTransfer() }
        .groupBy { it.category }
        .filterKeys { it.type == categoryType }

    // Expenses are net of refunds (money that came back); income is counted as received.
    val categoryAmounts = byCategory.mapValues { (category, txns) ->
        if (category.type == CategoryType.EXPENSE) txns.netExpense() else txns.sumOf { it.amount.amount }
    }
    // A category whose spending was fully (or more than fully) refunded has nothing to show.
    val visibleAmounts = categoryAmounts.filterValues { it > 0.0 }
    val totalAmount = visibleAmounts.values.sum()

    val categoryTransactions = visibleAmounts.entries
        .map { (category, spent) ->
            CategoryTransaction(
                category = category,
                percent = if (totalAmount > 0.0) (spent / totalAmount).toFloat() * 100 else 0f,
                amount = formatAmount(spent),
                transaction = byCategory.getValue(category),
            )
        }
        .sortedByDescending { it.percent }
    return categoryTransactions to totalAmount
}
