package com.nkuppan.expensemanager.domain.usecase.transaction

import com.nkuppan.expensemanager.domain.model.CategoryType
import com.nkuppan.expensemanager.domain.model.FilterType
import com.nkuppan.expensemanager.domain.model.Transaction
import com.nkuppan.expensemanager.domain.repository.SettingsRepository
import com.nkuppan.expensemanager.domain.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class GetTransactionWithFilterUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val transactionRepository: TransactionRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun invoke(): Flow<List<Transaction>?> {
        return combine(
            settingsRepository.isFilterEnabled(),
            settingsRepository.getCategoryTypes(),
            settingsRepository.getCategories(),
            settingsRepository.getAccounts(),
            settingsRepository.getFilterType()
        ) { isFilterEnabled, categoryTypes, categories, accounts, filterType ->
            val filterTypeRanges = settingsRepository.getFilterRange(filterType)
            val isValidAccount = accounts?.isNotEmpty() == true && accounts.firstOrNull() != "-1"
            FilterValue(
                filterType,
                filterTypeRanges,
                accounts,
                categories,
                categoryTypes,
                isValidAccount
            )
        }.flatMapLatest {
            return@flatMapLatest if (it.filterType == FilterType.ALL) {
                if (it.isValidAccountId) {
                    transactionRepository.getTransactionsByAccountId(it.accounts?.first()!!)
                } else {
                    transactionRepository.getAllTransaction()
                }
            } else {
                if (it.isValidAccountId) {
                    transactionRepository.getTransactionByAccountIdAndDateFilter(
                        it.accounts?.first()!!,
                        it.filterRange[0],
                        it.filterRange[1]
                    )
                } else {
                    transactionRepository.getTransactionByDateFilter(
                        it.filterRange[0],
                        it.filterRange[1]
                    )
                }
            }
        }
    }
}

data class FilterValue(
    val filterType: FilterType,
    val filterRange: List<Long>,
    val accounts: List<String>? = null,
    val categories: List<String>? = null,
    val categoryTypes: List<CategoryType>? = null,
    val isValidAccountId: Boolean
)