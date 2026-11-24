package com.naveenapps.expensemanager.core.domain.usecase.budget

import com.naveenapps.expensemanager.core.foundation.endOfMonthMillis
import com.naveenapps.expensemanager.core.foundation.endOfYearMillis
import com.naveenapps.expensemanager.core.foundation.monthAndYearKeyToLocalDate
import com.naveenapps.expensemanager.core.foundation.startOfMonthMillis
import com.naveenapps.expensemanager.core.foundation.startOfYearMillis
import com.naveenapps.expensemanager.core.foundation.toLocalDateIn
import com.naveenapps.expensemanager.core.foundation.toMonthAndYearKey
import com.naveenapps.expensemanager.core.foundation.toYearKey
import com.naveenapps.expensemanager.core.foundation.yearKeyToLocalDate
import com.naveenapps.expensemanager.core.model.Budget
import com.naveenapps.expensemanager.core.model.BudgetPeriod
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.repository.AccountRepository
import com.naveenapps.expensemanager.core.repository.CategoryRepository
import com.naveenapps.expensemanager.core.repository.TransactionRepository
import kotlinx.coroutines.flow.firstOrNull

class GetBudgetTransactionsUseCase(
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(budget: Budget): Resource<List<Transaction>> {
        val accounts: List<String> = if (budget.isAllAccountsSelected) {
            accountRepository.getAccounts().firstOrNull()?.map { it.id } ?: emptyList()
        } else {
            budget.accounts
        }

        val categories: List<String> = if (budget.isAllCategoriesSelected) {
            categoryRepository.getCategories().firstOrNull()?.map { it.id } ?: emptyList()
        } else {
            budget.categories
        }

        val isYearly = budget.periodType == BudgetPeriod.YEARLY
        val date = if (isYearly) {
            budget.selectedMonth.yearKeyToLocalDate()
        } else {
            budget.selectedMonth.monthAndYearKeyToLocalDate()
        }

        date ?: return Resource.Error(IllegalArgumentException("Unknown month value"))

        val startDate = if (isYearly) date.startOfYearMillis() else date.startOfMonthMillis()
        val endDate = if (isYearly) date.endOfYearMillis() else date.endOfMonthMillis()

        val transaction = transactionRepository.getFilteredTransaction(
            accounts = accounts,
            categories = categories,
            transactionType = CategoryType.entries.map { it.ordinal }.toList(),
            startDate = startDate,
            endDate = endDate,
        ).firstOrNull()?.filter {
            if (isYearly) {
                it.createdOn.toLocalDateIn().toYearKey() == budget.selectedMonth
            } else {
                it.createdOn.toLocalDateIn().toMonthAndYearKey() == budget.selectedMonth
            }
        }

        return Resource.Success(transaction ?: emptyList())
    }
}
