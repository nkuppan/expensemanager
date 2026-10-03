package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryChange
import com.naveenapps.expensemanager.core.model.PeriodComparison
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlin.math.abs

/**
 * The Analysis period compared with the one before it, using the same filters.
 * Emits null when there's no previous period to compare with (e.g. the "All" range).
 */
class GetPeriodComparisonUseCase(
    private val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
    private val categoryRepository: CategoryRepository,
    private val dispatchers: AppCoroutineDispatchers,
) {
    operator fun invoke(): Flow<PeriodComparison?> = combine(
        getTransactionWithFilterUseCase.withPreviousPeriod(),
        categoryRepository.getCategories(),
    ) { periods, categories ->
        periods ?: return@combine null
        calculatePeriodComparison(
            periods = periods,
            categoriesById = categories.associateBy { it.id },
        )
    }.flowOn(dispatchers.computation)
}

/** Pure calculation behind [GetPeriodComparisonUseCase]; unit-tested. */
fun calculatePeriodComparison(
    periods: PeriodTransactions,
    categoriesById: Map<String, Category>,
    maxCategoryChanges: Int = 3,
): PeriodComparison {
    fun List<Transaction>.total(type: TransactionType) = filter { it.type == type }.sumOf { it.amount.amount }

    fun List<Transaction>.expenseByCategory() = filter { it.type == TransactionType.EXPENSE }
        .groupBy { it.categoryId }
        .mapValues { (_, items) -> items.sumOf { it.amount.amount } }

    val current = periods.current.expenseByCategory()
    val previous = periods.previous.expenseByCategory()
    val categoryChanges = (current.keys + previous.keys)
        .mapNotNull { id ->
            val category = categoriesById[id] ?: return@mapNotNull null
            CategoryChange(category, current[id] ?: 0.0, previous[id] ?: 0.0)
        }
        .filter { abs(it.difference) > 0.005 }
        .sortedByDescending { abs(it.difference) }
        .take(maxCategoryChanges)

    return PeriodComparison(
        type = periods.windows.type,
        isToDate = periods.windows.isToDate,
        currentStart = periods.windows.currentStart,
        currentEnd = periods.windows.currentEnd,
        previousStart = periods.windows.previousStart,
        previousEnd = periods.windows.previousEnd,
        currentExpense = periods.current.total(TransactionType.EXPENSE),
        previousExpense = periods.previous.total(TransactionType.EXPENSE),
        currentIncome = periods.current.total(TransactionType.INCOME),
        previousIncome = periods.previous.total(TransactionType.INCOME),
        categoryChanges = categoryChanges,
    )
}
