package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.naveenapps.expensemanager.core.domain.usecase.settings.daterange.GetDateRangeByTypeUseCase
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.Transaction
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class GetExportTransactionsUseCase @Inject constructor(
    private val accountRepository: com.naveenapps.expensemanager.core.repository.AccountRepository,
    private val categoryRepository: com.naveenapps.expensemanager.core.repository.CategoryRepository,
    private val transactionRepository: com.naveenapps.expensemanager.core.repository.TransactionRepository,
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