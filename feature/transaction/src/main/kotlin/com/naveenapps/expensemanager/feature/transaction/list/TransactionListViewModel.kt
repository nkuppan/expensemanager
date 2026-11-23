package com.naveenapps.expensemanager.feature.transaction.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.common.utils.toCompleteDateWithDate
import com.naveenapps.expensemanager.core.designsystem.R as DesignSystemR
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.debt.DebtActivity
import com.naveenapps.expensemanager.core.domain.usecase.debt.GetDebtActivityUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.AddTransactionUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.DeleteTransactionUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetTransactionWithFilterUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.toTransactionUIModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.AmountTone
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.TransactionGroup
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.model.TransactionUiItem
import com.naveenapps.expensemanager.core.model.isIncome
import com.naveenapps.expensemanager.core.model.milliToDouble
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.core.repository.AnalyticsEvents
import com.naveenapps.expensemanager.core.repository.AnalyticsParams
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.core.repository.ImageStorageRepository
import com.naveenapps.expensemanager.feature.transaction.search.CategoryDisplayNames
import com.naveenapps.expensemanager.feature.transaction.search.matchesSearch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class TransactionListViewModel(
    getCurrencyUseCase: GetCurrencyUseCase,
    getFormattedAmountUseCase: GetFormattedAmountUseCase,
    getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
    appCoroutineDispatchers: AppCoroutineDispatchers,
    private val appComposeNavigator: AppComposeNavigator,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val imageStorageRepository: ImageStorageRepository,
    private val analyticsRepository: AnalyticsRepository,
    getDebtActivityUseCase: GetDebtActivityUseCase,
    categoryDisplayNames: CategoryDisplayNames = CategoryDisplayNames.Stored,
    getAllAccountsUseCase: GetAllAccountsUseCase? = null,
) : ViewModel() {

    private val _transactions = MutableStateFlow(TransactionListState(emptyList(), isLoading = true))
    val state = _transactions.asStateFlow()

    private val _event = Channel<TransactionListEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    private val searchQuery = MutableStateFlow("")
    private val searchScope = MutableStateFlow(SearchScope.ALL_TIME)

    // The transactions currently listed, by id, so a swipe-delete can act on the full model.
    private var visibleTransactions: Map<String, Transaction> = emptyMap()

    // Debt entries listed among the transactions; tapping one opens the debt entry instead.
    private var visibleDebtIds: Set<String> = emptySet()

    // Deleted but still undoable. Its attachment files are only removed once the undo window
    // has passed; otherwise "Undo" would bring back a transaction pointing at missing photos.
    private var pendingUndo: Transaction? = null

    init {
        // While searching all time, read every date; otherwise the selected period. The other
        // filters (accounts, categories, types) apply either way.
        val allTime = combine(searchQuery, searchScope) { query, scope ->
            query.isNotBlank() && scope == SearchScope.ALL_TIME
        }.distinctUntilChanged()
        val source = allTime.flatMapLatest { all ->
            if (all) getTransactionWithFilterUseCase.allTime() else getTransactionWithFilterUseCase.invoke()
        }
        // Lent / borrowed / repaid entries that moved money in an account, so a balance change
        // always has a row explaining it. Never counted in the day totals or search totals.
        val debts = allTime.flatMapLatest { all -> getDebtActivityUseCase(allTime = all) }

        val accountsFlow = getAllAccountsUseCase?.invoke() ?: flowOf(emptyList())

        combine(
            combine(getCurrencyUseCase.invoke(), accountsFlow) { currency, accounts -> currency to accounts },
            source,
            searchQuery,
            categoryDisplayNames.lookup(),
            debts,
        ) { (currency, accounts), transactions, query, displayName, debtActivity ->

            val matching = transactions.orEmpty().filter { it.matches(query, displayName) }
            visibleTransactions = matching.associateBy { it.id }
            val matchingDebts = debtActivity.filter { activity ->
                matchesSearch(query, listOf(activity.personName, activity.entry.note, activity.account.name))
            }
            visibleDebtIds = matchingDebts.map { it.entry.id }.toSet()

            val summary = if (query.isBlank()) {
                null
            } else {
                // A total that's zero isn't shown (e.g. no income among the results).
                fun totalOf(type: TransactionType): String? = matching
                    .filter { it.type == type }
                    .sumOf { it.amount.amount }
                    .takeIf { it > 0.0 }
                    ?.let { getFormattedAmountUseCase.invoke(it, currency).amountString }
                SearchSummary(
                    count = matching.size,
                    spent = totalOf(TransactionType.EXPENSE),
                    earned = totalOf(TransactionType.INCOME),
                )
            }

            val allRawTransactions = transactions.orEmpty()
            val closingBalances = mutableMapOf<String, Double>()

            // Compute running balance per account backwards from current account balance
            val accountsMap = accounts.associateBy { it.id }
            for ((accountId, account) in accountsMap) {
                data class AccountEvent(val id: String, val date: Date, val delta: Double)

                val events = mutableListOf<AccountEvent>()
                for (t in allRawTransactions) {
                    if (t.fromAccountId == accountId) {
                        val delta = if (t.type.isIncome()) t.amount.amount else -t.amount.amount
                        events.add(AccountEvent(t.id, t.createdOn, delta))
                    } else if (t.toAccountId == accountId) {
                        events.add(AccountEvent(t.id, t.createdOn, t.amount.amount))
                    }
                }
                for (d in debtActivity) {
                    if (d.account.id == accountId) {
                        val delta = d.entry.amountMilli.milliToDouble() * d.entry.kind.accountSign
                        events.add(AccountEvent(d.entry.id, d.entry.date, delta))
                    }
                }

                events.sortByDescending { it.date }
                var balance = account.amount
                for (event in events) {
                    closingBalances[event.id] = balance
                    balance -= event.delta
                }
            }

            // Only what has actually happened: a recurring rule logs each occurrence on its day.
            val rows = matching.map { transaction ->
                val closingBal = closingBalances[transaction.id]?.let {
                    getFormattedAmountUseCase.invoke(it, currency)
                }
                ListRow(
                    date = transaction.createdOn,
                    transaction = transaction,
                    item = transaction.toTransactionUIModel(
                        getFormattedAmountUseCase.invoke(transaction.amount.amount, currency),
                    ).copy(closingBalance = closingBal),
                )
            } + matchingDebts.map { activity ->
                val closingBal = closingBalances[activity.entry.id]?.let {
                    getFormattedAmountUseCase.invoke(it, currency)
                }
                ListRow(
                    date = activity.entry.date,
                    transaction = null,
                    item = activity.toTransactionUiItem(
                        getFormattedAmountUseCase.invoke(activity.entry.amountMilli.milliToDouble(), currency),
                    ).copy(closingBalance = closingBal),
                )
            }
            val groupedItem = rows.sortedByDescending { it.date }.groupBy {
                it.date.toCompleteDateWithDate()
            }.map {
                // The day's total counts real transactions only, not debt entries.
                val totalAmount = it.value.mapNotNull { row -> row.transaction }.toTransactionSum()
                TransactionGroup(
                    date = it.key,
                    amountTone = AmountTone.of(totalAmount),
                    totalAmount = getFormattedAmountUseCase.invoke(totalAmount, currency),
                    transactions = it.value.map { row -> row.item },
                )
            }

            _transactions.update {
                it.copy(
                    transactionListItem = groupedItem.convertGroupToTransactionListItems(),
                    isLoading = false,
                    searchSummary = summary,
                )
            }
        }.flowOn(appCoroutineDispatchers.computation).launchIn(viewModelScope)

        // One search_used per search the user settled on (not per keystroke): how many results
        // and whether it covered all time or the selected period. The text itself isn't sent.
        searchQuery
            .debounce(SEARCH_SETTLE_MS.milliseconds)
            .map { it.trim() }
            .distinctUntilChanged()
            .filter { it.isNotEmpty() }
            .onEach {
                analyticsRepository.logEvent(
                    AnalyticsEvents.SEARCH_USED,
                    mapOf(
                        AnalyticsParams.COUNT to resultsBucket(_transactions.value.searchSummary?.count ?: 0),
                        AnalyticsParams.TYPE to searchScope.value.name.lowercase(),
                    ),
                )
            }
            .launchIn(viewModelScope)
    }

    private fun openCreateScreen(transactionId: String? = null) {
        appComposeNavigator.navigate(
            ExpenseManagerScreens.TransactionCreate(transactionId),
        )
    }

    private fun openRecurringTransactions() {
        appComposeNavigator.navigate(ExpenseManagerScreens.RecurringTransactions)
    }

    private fun closePage() {
        appComposeNavigator.popBackStack()
    }

    private fun search(query: String) {
        searchQuery.value = query
        _transactions.update { it.copy(searchQuery = query) }
    }

    private fun setSearchScope(scope: SearchScope) {
        searchScope.value = scope
        _transactions.update { it.copy(searchScope = scope) }
    }

    private fun deleteTransaction(transactionId: String) {
        val transaction = visibleTransactions[transactionId] ?: return
        viewModelScope.launch {
            // A second delete inside the undo window makes the first one final.
            finalizePendingDelete()
            if (deleteTransactionUseCase(transaction) is Resource.Success) {
                pendingUndo = transaction
                _event.send(TransactionListEvent.ShowUndoDelete)
            } else {
                _event.send(TransactionListEvent.ShowMessage(DesignSystemR.string.error_delete_failed))
            }
        }
    }

    private fun undoDelete() {
        val transaction = pendingUndo ?: return
        pendingUndo = null
        viewModelScope.launch {
            // Re-adding goes through the normal path, so account balances are re-applied.
            if (addTransactionUseCase(transaction) !is Resource.Success) {
                transaction.attachments.forEach { imageStorageRepository.deleteTransactionAttachment(it) }
                _event.send(TransactionListEvent.ShowMessage(DesignSystemR.string.error_save_failed))
            }
        }
    }

    private fun finalizePendingDelete() {
        val transaction = pendingUndo ?: return
        pendingUndo = null
        transaction.attachments.forEach { imageStorageRepository.deleteTransactionAttachment(it) }
    }

    override fun onCleared() {
        finalizePendingDelete()
        super.onCleared()
    }

    fun processAction(action: TransactionListAction) {
        when (action) {
            TransactionListAction.ClosePage -> closePage()

            TransactionListAction.OpenCreateTransaction -> openCreateScreen()

            is TransactionListAction.OpenEdiTransaction -> if (action.transactionId in visibleDebtIds) {
                appComposeNavigator.navigate(ExpenseManagerScreens.DebtEntry(id = action.transactionId))
            } else {
                openCreateScreen(action.transactionId)
            }

            is TransactionListAction.Search -> search(action.query)

            is TransactionListAction.SetSearchScope -> setSearchScope(action.scope)

            TransactionListAction.OpenRecurringTransactions -> openRecurringTransactions()

            is TransactionListAction.DeleteTransaction -> deleteTransaction(action.transactionId)

            TransactionListAction.UndoDelete -> undoDelete()

            TransactionListAction.UndoExpired -> finalizePendingDelete()
        }
    }
}

/**
 * Case-insensitive match on the free-text parts of a transaction: notes, category name and
 * account names. A built-in category matches both by the name shown (displayName, e.g. its
 * Hindi label) and by its stored English name.
 * One row of the list: a transaction, or a debt entry shown beside them (transaction = null). */
private data class ListRow(val date: Instant, val transaction: Transaction?, val item: TransactionUiItem)

/**
 * A debt entry as a list row. Money leaving the account (lent, paid back) shows like an expense,
 * money coming in (borrowed, got back) like income; the screen titles it "Lent to Ravi" etc.
 */
private fun DebtActivity.toTransactionUiItem(amount: Amount): TransactionUiItem = TransactionUiItem(
    id = entry.id,
    amount = amount,
    notes = entry.note.ifBlank { null },
    categoryName = personName,
    transactionType = if (entry.kind.accountSign < 0) TransactionType.EXPENSE else TransactionType.INCOME,
    categoryIcon = StoredIcon(name = DEBT_ROW_ICON, backgroundColor = DEBT_ROW_COLOR),
    date = entry.date.toCompleteDateWithDate(),
    fromAccountName = account.name,
    fromAccountIcon = account.storedIcon,
    debtKind = entry.kind,
)

private const val DEBT_ROW_ICON = "diversity"
private const val DEBT_ROW_COLOR = "#FF7E57C2"

private const val SEARCH_SETTLE_MS = 1_500L

/** Search results for analytics: 0, 1-5, 6-20, 21+. */
internal fun resultsBucket(count: Int): String = when {
    count <= 0 -> "0"
    count <= 5 -> "1-5"
    count <= 20 -> "6-20"
    else -> "21+"
}

internal fun Transaction.matches(query: String, displayName: (Category) -> String = { it.name }): Boolean = matchesSearch(query, listOf(notes, category.name, displayName(category), fromAccount.name, toAccount?.name))

fun List<Transaction>.toTransactionSum() = this.sumOf {
    when (it.type) {
        TransactionType.INCOME -> {
            it.amount.amount
        }

        TransactionType.EXPENSE -> {
            it.amount.amount * -1
        }

        TransactionType.TRANSFER -> {
            0.0
        }
    }
}

fun List<TransactionGroup>.convertGroupToTransactionListItems(): List<TransactionListItem> = buildList {
    this@convertGroupToTransactionListItems.forEach {
        add(
            TransactionListItem.HeaderItem(
                date = it.date,
                amountTone = it.amountTone,
                totalAmount = it.totalAmount.amountString ?: "",
            ),
        )

        it.transactions.forEach {
            add(TransactionListItem.TransactionItem(date = it))
        }

        add(TransactionListItem.Divider)
    }
}

sealed class TransactionListItem {

    data class HeaderItem(
        val date: String,
        val amountTone: AmountTone,
        val totalAmount: String,
    ) : TransactionListItem()

    data class TransactionItem(
        val date: TransactionUiItem,
    ) : TransactionListItem()

    data object Divider : TransactionListItem()
}
