package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.GetDateRangeUseCase
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.repository.AccountRepository
import com.naveenapps.expensemanager.core.repository.CategoryRepository
import com.naveenapps.expensemanager.core.repository.SettingsRepository
import com.naveenapps.expensemanager.core.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class GetTransactionWithFilterUseCase(
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository,
    private val getDateRangeUseCase: GetDateRangeUseCase,
    private val transactionRepository: TransactionRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun invoke(): Flow<List<Transaction>?> = filterValues().flatMapLatest { filter ->
        if (filter.dateRangeType == DateRangeType.ALL) {
            transactionRepository.getAllFilteredTransaction(
                filter.accounts,
                filter.categories,
                filter.transactionTypes,
            )
        } else {
            query(filter, filter.filterRange[0], filter.filterRange[1])
        }
    }

    /**
     * The selected period next to the one before it, with the same account / category / type
     * filters. Emits null when there's nothing to compare (the "All" range, or a future period).
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun withPreviousPeriod(
        now: () -> Long = { System.currentTimeMillis() },
    ): Flow<PeriodTransactions?> = filterValues().flatMapLatest { filter ->
        val windows = comparisonWindows(filter.dateRangeType, filter.filterRange, now())
            ?: return@flatMapLatest flowOf(null)
        combine(
            query(filter, windows.currentStart, windows.currentEnd),
            query(filter, windows.previousStart, windows.previousEnd),
        ) { current, previous ->
            PeriodTransactions(
                windows = windows,
                current = current.orEmpty(),
                previous = previous.orEmpty(),
            )
        }
    }

    private fun query(filter: FilterValue, start: Long, end: Long) =
        transactionRepository.getFilteredTransaction(
            filter.accounts,
            filter.categories,
            filter.transactionTypes,
            start,
            end,
        )

    private fun filterValues(): Flow<FilterValue> = combine(
        settingsRepository.getTransactionTypes(),
        settingsRepository.getCategories(),
        settingsRepository.getAccounts(),
        getDateRangeUseCase.invoke(),
    ) { selectedTransactionTypes, selectedCategories, selectedAccounts, dateRangeModel ->

        val transactionTypes: List<Int> = if (selectedTransactionTypes.isNullOrEmpty()) {
            TransactionType.entries.map { it.ordinal }
        } else {
            selectedTransactionTypes.map { it.ordinal }
        }

        val accounts: List<String> = if (selectedAccounts.isNullOrEmpty()) {
            accountRepository.getAccounts().firstOrNull()?.map { it.id } ?: emptyList()
        } else {
            selectedAccounts
        }

        val categories: List<String> = if (selectedCategories.isNullOrEmpty()) {
            categoryRepository.getCategories().firstOrNull()?.map { it.id } ?: emptyList()
        } else {
            selectedCategories
        }

        FilterValue(
            dateRangeModel.type,
            dateRangeModel.dateRanges,
            accounts,
            categories,
            transactionTypes,
        )
    }
}

data class PeriodTransactions(
    val windows: ComparisonWindows,
    val current: List<Transaction>,
    val previous: List<Transaction>,
)

data class FilterValue(
    val dateRangeType: DateRangeType,
    val filterRange: List<Long>,
    val accounts: List<String>,
    val categories: List<String>,
    val transactionTypes: List<Int>,
)
