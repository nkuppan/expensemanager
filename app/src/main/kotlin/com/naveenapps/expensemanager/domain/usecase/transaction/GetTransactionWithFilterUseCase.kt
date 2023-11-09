package com.naveenapps.expensemanager.domain.usecase.transaction

import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.domain.repository.SettingsRepository
import com.naveenapps.expensemanager.domain.repository.TransactionRepository
import com.naveenapps.expensemanager.domain.usecase.settings.daterange.GetDateRangeUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class GetTransactionWithFilterUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val getDateRangeUseCase: GetDateRangeUseCase,
    private val transactionRepository: TransactionRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun invoke(): Flow<List<Transaction>?> {
        return combine(
            settingsRepository.isFilterEnabled(),
            settingsRepository.getCategoryTypes(),
            settingsRepository.getCategories(),
            settingsRepository.getAccounts(),
            getDateRangeUseCase.invoke()
        ) { isFilterEnabled, categoryTypes, categories, accounts, dateRangeModel ->
            FilterValue(
                isFilterEnabled,
                dateRangeModel.type,
                dateRangeModel.dateRanges,
                accounts,
                categories,
                categoryTypes
            )
        }.flatMapLatest {
            val accounts = it.accounts
            return@flatMapLatest if (it.dateRangeType == DateRangeType.ALL) {
                if (it.isFilterEnabled && accounts != null) {
                    transactionRepository.getTransactionsByAccountId(accounts)
                } else {
                    transactionRepository.getAllTransaction()
                }
            } else {
                if (it.isFilterEnabled && accounts != null) {
                    transactionRepository.getTransactionByAccountIdAndDateFilter(
                        accounts,
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
    val isFilterEnabled: Boolean,
    val dateRangeType: DateRangeType,
    val filterRange: List<Long>,
    val accounts: List<String>? = null,
    val categories: List<String>? = null,
    val categoryTypes: List<CategoryType>? = null,
)