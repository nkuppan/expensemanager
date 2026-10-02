package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.naveenapps.expensemanager.core.model.MonthlyRecap
import com.naveenapps.expensemanager.core.model.RecapCategory
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.repository.CategoryRepository
import com.naveenapps.expensemanager.core.repository.TransactionRepository
import kotlinx.coroutines.flow.firstOrNull
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

/**
 * Builds the recap for a calendar month (the previous month by default): total spent, how many
 * days were logged, and the top expense categories by share. Returns null when the month has
 * no expenses — there's nothing to recap or share.
 */
class GetMonthlyRecapUseCase(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
) {
    suspend operator fun invoke(
        month: YearMonth = YearMonth.now().minusMonths(1),
        maxCategories: Int = DEFAULT_TOP_CATEGORIES,
    ): MonthlyRecap? {
        val zone = ZoneId.systemDefault()
        val start = month.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()

        val inMonth = transactionRepository.getAllTransaction().firstOrNull().orEmpty()
            .filter { it.createdOn.time in start until end }
        val expenses = inMonth.filter { it.type == TransactionType.EXPENSE }
        val total = expenses.sumOf { it.amount.amount }
        if (expenses.isEmpty() || total <= 0.0) return null

        val categoriesById = categoryRepository.getCategories().firstOrNull().orEmpty()
            .associateBy { it.id }

        val top = expenses
            .groupBy { it.categoryId }
            .mapNotNull { (categoryId, items) ->
                val category = categoriesById[categoryId] ?: return@mapNotNull null
                RecapCategory(category, (items.sumOf { it.amount.amount } / total).toFloat())
            }
            .sortedByDescending { it.share }
            .take(maxCategories)

        val daysLogged = inMonth
            .map { it.createdOn.toInstant().atZone(zone).toLocalDate() }
            .distinct()
            .size

        return MonthlyRecap(
            month = Date.from(month.atDay(1).atStartOfDay(zone).toInstant()),
            monthKey = month.format(DateTimeFormatter.ofPattern("yyyyMM")),
            totalExpense = total,
            transactionCount = expenses.size,
            daysLogged = daysLogged,
            topCategories = top,
        )
    }

    private companion object {
        const val DEFAULT_TOP_CATEGORIES = 4
    }
}
