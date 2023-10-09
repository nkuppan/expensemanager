package com.nkuppan.expensemanager.domain.usecase.budget

import com.nkuppan.expensemanager.data.utils.fromTransactionMonthToDate
import com.nkuppan.expensemanager.data.utils.toTransactionMonth
import com.nkuppan.expensemanager.domain.model.Budget
import com.nkuppan.expensemanager.domain.model.CategoryType
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.model.Transaction
import com.nkuppan.expensemanager.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.firstOrNull
import org.joda.time.DateTime
import javax.inject.Inject

class GetBudgetTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository,
) {
    suspend operator fun invoke(budget: Budget): Resource<List<Transaction>> {

        val transaction = if (budget.isAllAccountsSelected && budget.isAllCategoriesSelected) {
            repository.getAllTransaction().firstOrNull()?.filter {
                it.createdOn.toTransactionMonth() == budget.selectedMonth
            }
        } else {
            val date = budget.selectedMonth.fromTransactionMonthToDate()
            val startDayOfMonth = DateTime(date).withDayOfMonth(1).withTimeAtStartOfDay().millis
            val endDayOfMonth = DateTime()
                .run {
                    return@run dayOfMonth().withMaximumValue().plus(1).withTimeAtStartOfDay().millis
                }

            repository.getFilteredTransaction(
                accounts = budget.accounts,
                categories = budget.categories,
                categoryType = CategoryType.values().map { it.ordinal }.toList(),
                startDate = startDayOfMonth,
                endDate = endDayOfMonth,
            ).firstOrNull()?.filter {
                it.createdOn.toTransactionMonth() == budget.selectedMonth
            }
        }

        return Resource.Success(transaction ?: emptyList())
    }
}