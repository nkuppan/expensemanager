package com.nkuppan.expensemanager.domain.usecase.transaction

import com.nkuppan.expensemanager.domain.model.CategoryType
import com.nkuppan.expensemanager.domain.model.DateRangeType
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.model.Transaction
import com.nkuppan.expensemanager.domain.repository.AccountRepository
import com.nkuppan.expensemanager.domain.repository.CategoryRepository
import com.nkuppan.expensemanager.domain.repository.TransactionRepository
import com.nkuppan.expensemanager.domain.usecase.settings.daterange.GetDateRangeByTypeUseCase
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class GetExportTransactionsUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val getDateRangeByTypeUseCase: GetDateRangeByTypeUseCase,
) {
    suspend operator fun invoke(
        dateRangeType: DateRangeType,
        selectedAccounts: List<String>,
        isAllAccountsSelected: Boolean
    ): Resource<List<Transaction>> {

        val accounts: List<String> = if (isAllAccountsSelected) {
            accountRepository.getAccounts().firstOrNull()?.map { it.id }
                ?: emptyList()
        } else {
            selectedAccounts
        }

        val categories: List<String> = categoryRepository.getCategories()
            .firstOrNull()?.map { it.id } ?: emptyList()

        val dateRanges = getDateRangeByTypeUseCase.invoke(dateRangeType)

        val transaction = transactionRepository.getFilteredTransaction(
            accounts = accounts,
            categories = categories,
            categoryType = CategoryType.values().map { it.ordinal }.toList(),
            startDate = dateRanges.dateRanges[0],
            endDate = dateRanges.dateRanges[1],
        ).firstOrNull()

        return Resource.Success(transaction ?: emptyList())
    }
}