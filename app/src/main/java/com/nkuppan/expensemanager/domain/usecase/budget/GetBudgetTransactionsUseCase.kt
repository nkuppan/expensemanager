package com.nkuppan.expensemanager.domain.usecase.budget

import com.nkuppan.expensemanager.data.utils.fromMonthAndYear
import com.nkuppan.expensemanager.data.utils.toMonthAndYear
import com.nkuppan.expensemanager.domain.model.Budget
import com.nkuppan.expensemanager.domain.model.CategoryType
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.model.Transaction
import com.nkuppan.expensemanager.domain.repository.AccountRepository
import com.nkuppan.expensemanager.domain.repository.CategoryRepository
import com.nkuppan.expensemanager.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.firstOrNull
import org.joda.time.DateTime
import javax.inject.Inject

class GetBudgetTransactionsUseCase @Inject constructor(
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

        val date = budget.selectedMonth.fromMonthAndYear()
        val startDayOfMonth = DateTime(date).withDayOfMonth(1)
            .withTimeAtStartOfDay().millis
        val endDayOfMonth = DateTime()
            .run {
                return@run dayOfMonth().withMaximumValue().plusDays(1)
                    .withTimeAtStartOfDay().millis
            }

        val transaction = transactionRepository.getFilteredTransaction(
            accounts = accounts,
            categories = categories,
            categoryType = CategoryType.values().map { it.ordinal }.toList(),
            startDate = startDayOfMonth,
            endDate = endDayOfMonth,
        ).firstOrNull()?.filter {
            it.createdOn.toMonthAndYear() == budget.selectedMonth
        }

        return Resource.Success(transaction ?: emptyList())
    }
}