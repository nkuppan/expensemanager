package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.GetDateRangeUseCase
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.repository.AccountRepository
import com.naveenapps.expensemanager.core.repository.CategoryRepository
import com.naveenapps.expensemanager.core.repository.SettingsRepository
import com.naveenapps.expensemanager.core.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.shareIn

class GetTransactionWithFilterUseCase(
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository,
    private val getDateRangeUseCase: GetDateRangeUseCase,
    private val transactionRepository: TransactionRepository,
    sharingScope: CoroutineScope,
) {
    // One query for the selected period, shared by everything that shows it: the dashboard
    // summary, both category donuts, budgets, the transaction list and the widget. Each
    // collector used to run its own query (with four joined lookups per row), and every save
    // re-ran all of them. It keeps running 5 s after the last collector leaves, so rotation and
    // quick screen switches reuse it; the cached list is dropped when it stops, so a later
    // collector never sees a stale period or filter first.
    @OptIn(ExperimentalCoroutinesApi::class)
    private val selectedPeriod: SharedFlow<List<Transaction>?> = filterValues()
        .flatMapLatest { filter ->
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
        .shareIn(
            scope = sharingScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000, replayExpirationMillis = 0),
            replay = 1,
        )

    /** Transactions in the selected period that match the current filters. Shared; see above. */
    fun invoke(): Flow<List<Transaction>?> = selectedPeriod

    /**
     * Same account / category / type filters as [invoke], but over all dates. Used by search, so
     * looking for "rent" finds every month's rent, not only the selected period's.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun allTime(): Flow<List<Transaction>?> = filterValues().flatMapLatest { filter ->
        transactionRepository.getAllFilteredTransaction(
            filter.accounts,
            filter.categories,
            filter.transactionTypes,
        )
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

    private fun query(filter: FilterValue, start: Long, end: Long) = transactionRepository.getFilteredTransaction(
        filter.accounts,
        filter.categories,
        filter.transactionTypes,
        start,
        end,
    )

    /** The current filter selection (date range, accounts, categories, types). */
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
