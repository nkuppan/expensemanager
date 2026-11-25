package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategorySpend
import com.naveenapps.expensemanager.core.model.SpendingInsights
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.WeekdaySpend
import com.naveenapps.expensemanager.core.model.isExpense
import com.naveenapps.expensemanager.core.model.isRefund
import com.naveenapps.expensemanager.core.model.netExpense
import com.naveenapps.expensemanager.core.model.netIncome
import com.naveenapps.expensemanager.core.repository.CategoryRepository
import java.time.DayOfWeek
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn

/**
 * Spending insights for the Analysis screen, over the same filtered transactions as its chart,
 * so every insight always agrees with what's plotted above it.
 */
class GetSpendingInsightsUseCase(
    private val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
    private val categoryRepository: CategoryRepository,
    private val dispatchers: AppCoroutineDispatchers,
) {
    operator fun invoke(): Flow<SpendingInsights> = combine(
        getTransactionWithFilterUseCase.invoke(),
        categoryRepository.getCategories(),
    ) { transactions, categories ->
        calculateSpendingInsights(
            transactions = transactions.orEmpty(),
            categoriesById = categories.associateBy { it.id },
            zone = ZoneId.systemDefault(),
        )
    }.flowOn(dispatchers.computation)
}

/** Pure calculation behind [GetSpendingInsightsUseCase]; unit-tested. */
fun calculateSpendingInsights(
    transactions: List<Transaction>,
    categoriesById: Map<String, Category>,
    zone: ZoneId,
    maxBiggest: Int = 5,
): SpendingInsights {
    // Expenses and refunds both move the spending figure: a refund subtracts what came back.
    val expenseItems = transactions.filter { it.type.isExpense() || it.type.isRefund() }
    val totalExpense = expenseItems.netExpense()
    val totalIncome = transactions.netIncome()

    val categories = expenseItems
        .groupBy { it.categoryId }
        .mapNotNull { (categoryId, items) ->
            val category = categoriesById[categoryId] ?: return@mapNotNull null
            val total = items.netExpense()
            CategorySpend(
                category = category,
                total = total,
                share = if (totalExpense > 0) (total / totalExpense).toFloat() else 0f,
                count = items.count { it.type.isExpense() },
            )
        }
        .filter { it.total > 0.0 }
        .sortedByDescending { it.total }

    val byWeekday = expenseItems.groupBy { it.createdOn.toInstant().atZone(zone).dayOfWeek }
    val weekdays = DayOfWeek.entries.map { day ->
        WeekdaySpend(day, byWeekday[day].orEmpty().netExpense())
    }

    return SpendingInsights(
        totalExpense = totalExpense,
        totalIncome = totalIncome,
        savingsRate = if (totalIncome > 0) ((totalIncome - totalExpense) / totalIncome).toFloat() else null,
        categories = categories,
        weekdays = weekdays,
        busiestWeekday = weekdays.filter { it.total > 0 }.maxByOrNull { it.total }?.dayOfWeek,
        biggestExpenses = transactions
            .filter { it.type.isExpense() }
            .sortedByDescending { it.amount.amount }
            .take(maxBiggest),
    )
}
