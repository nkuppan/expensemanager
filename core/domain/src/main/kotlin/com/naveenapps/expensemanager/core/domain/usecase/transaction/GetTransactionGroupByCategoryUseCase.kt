package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.domain.usecase.category.GetAllCategoryUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.model.CategoryTransaction
import com.naveenapps.expensemanager.core.model.CategoryTransactionState
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.PieChartData
import com.naveenapps.expensemanager.core.model.getDummyPieChartData
import com.naveenapps.expensemanager.core.model.isTransfer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn

class GetTransactionGroupByCategoryUseCase(
    private val getAllCategoryUseCase: GetAllCategoryUseCase,
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
    private val appCoroutineDispatchers: AppCoroutineDispatchers
) {
    fun invoke(categoryType: CategoryType): Flow<CategoryTransactionState> {
        return combine(
            getCurrencyUseCase.invoke(),
            getTransactionWithFilterUseCase.invoke(),
        ) { currency, transactions ->

            // Single pass: filter transfers, group by category, filter by type
            val byCategory = transactions
                ?.filterNot { it.type.isTransfer() }
                ?.groupBy { it.category }
                ?.filterKeys { it.type == categoryType }
                ?: emptyMap()

            // Compute per-category sums once; derive total from those — no double iteration
            val categoryAmounts = byCategory.mapValues { (_, txns) ->
                txns.sumOf { it.amount.amount }
            }
            val totalAmount = categoryAmounts.values.sum()

            val categoryTransactions = byCategory.entries
                .map { (category, txns) ->
                    val spent = categoryAmounts.getValue(category)
                    CategoryTransaction(
                        category = category,
                        percent = if (totalAmount > 0.0) (spent / totalAmount).toFloat() * 100 else 0f,
                        amount = getFormattedAmountUseCase.invoke(amount = spent, currency = currency),
                        transaction = txns,
                    )
                }
                .sortedByDescending { it.percent }

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
                pieChartData = categories.map { getDummyPieChartData(it.name, equalShare, it.titleResId) }
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
}

fun CategoryTransaction.toChartModel(): PieChartData {
    return PieChartData(
        name = this.category.name,
        value = this.percent,
        color = this.category.storedIcon.backgroundColor,
        titleResId = this.category.titleResId,
    )
}
