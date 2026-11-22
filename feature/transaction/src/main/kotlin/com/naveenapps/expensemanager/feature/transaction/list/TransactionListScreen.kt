package com.naveenapps.expensemanager.feature.transaction.list

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.common.utils.fromCompleteDate
import com.naveenapps.expensemanager.core.common.utils.toCompleteDateWithDate
import com.naveenapps.expensemanager.core.common.utils.toDate
import com.naveenapps.expensemanager.core.common.utils.toDay
import com.naveenapps.expensemanager.core.common.utils.toMonthYear
import com.naveenapps.expensemanager.core.designsystem.R as DesignSystemR
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmChip
import com.naveenapps.expensemanager.core.designsystem.em.EmDivider
import com.naveenapps.expensemanager.core.designsystem.em.EmEmptyState
import com.naveenapps.expensemanager.core.designsystem.em.EmFab
import com.naveenapps.expensemanager.core.designsystem.em.EmIconButton
import com.naveenapps.expensemanager.core.designsystem.em.EmLoading
import com.naveenapps.expensemanager.core.designsystem.em.EmPreviews
import com.naveenapps.expensemanager.core.designsystem.em.EmScaffold
import com.naveenapps.expensemanager.core.designsystem.em.EmSearchField
import com.naveenapps.expensemanager.core.designsystem.em.EmSnackbarDuration
import com.naveenapps.expensemanager.core.designsystem.em.EmSnackbarHostState
import com.naveenapps.expensemanager.core.designsystem.em.EmSnackbarResult
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.em.emColorOf
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.TransactionItem
import com.naveenapps.expensemanager.core.designsystem.ui.utils.categoryDisplayName
import com.naveenapps.expensemanager.core.designsystem.utils.ObserveAsEvents
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.AmountTone
import com.naveenapps.expensemanager.core.model.DebtEntryKind
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.model.TransactionGroup
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.model.TransactionUiItem
import com.naveenapps.expensemanager.feature.filter.FilterView
import com.naveenapps.expensemanager.feature.transaction.R
import java.util.Date
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TransactionListScreen(
    showBackNavigationIcon: Boolean = false,
    // False inside Home's tabs, where the bottom bar's centre "+" adds a transaction instead.
    showAddButton: Boolean = true,
    viewModel: TransactionListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { EmSnackbarHostState() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            TransactionListEvent.ShowUndoDelete -> coroutineScope.launch {
                // Replace any undo snackbar still showing; its delete becomes final.
                snackbarHostState.currentSnackbarData?.dismiss()
                val result = snackbarHostState.showSnackbar(
                    message = context.getString(R.string.transaction_deleted),
                    actionLabel = context.getString(R.string.undo),
                    duration = EmSnackbarDuration.Long,
                )
                viewModel.processAction(
                    if (result == EmSnackbarResult.ActionPerformed) {
                        TransactionListAction.UndoDelete
                    } else {
                        TransactionListAction.UndoExpired
                    },
                )
            }

            is TransactionListEvent.ShowMessage -> coroutineScope.launch {
                snackbarHostState.showSnackbar(context.getString(event.messageRes))
            }
        }
    }

    TransactionListScreenContent(
        showBackNavigationIcon,
        showAddButton,
        state,
        viewModel::processAction,
        snackbarHostState,
    )
}

@Composable
private fun TransactionListScreenContent(
    showBackNavigationIcon: Boolean,
    showAddButton: Boolean = true,
    state: TransactionListState,
    onAction: (TransactionListAction) -> Unit,
    snackbarHostState: EmSnackbarHostState = remember { EmSnackbarHostState() },
) {
    // Search is always visible under the title (no tap to open it, and it doesn't grab focus, so
    // the keyboard only appears when the user taps it). Back clears an active search first.
    BackHandler(enabled = state.searchQuery.isNotEmpty()) {
        onAction(TransactionListAction.Search(""))
    }

    EmScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            // The app draws edge to edge and there's no app bar to take the status-bar inset, so this
            // row keeps clear of the status bar itself.
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(
                        start = if (showBackNavigationIcon) EmTheme.spacing.xs else EmTheme.spacing.lg,
                        end = EmTheme.spacing.lg,
                        top = EmTheme.spacing.lg,
                        bottom = EmTheme.spacing.sm,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Opened as its own screen (not the Home tab): a way back, at the start of the row.
                if (showBackNavigationIcon) {
                    EmIconButton(
                        icon = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = stringResource(DesignSystemR.string.a11y_back),
                        onClick = { onAction(TransactionListAction.ClosePage) },
                    )
                }
                EmSearchField(
                    query = state.searchQuery,
                    onQueryChange = { onAction(TransactionListAction.Search(it)) },
                    placeholder = stringResource(R.string.search_transactions_hint),
                    modifier = Modifier.weight(1f),
                    // Recurring transactions sit at the end of the search box.
                    trailingAction = {
                        EmIconButton(
                            icon = Icons.Outlined.EventRepeat,
                            contentDescription = stringResource(R.string.recurring_transactions),
                            onClick = { onAction(TransactionListAction.OpenRecurringTransactions) },
                        )
                    },
                )
            }
        },
        floatingActionButton = {
            if (showAddButton) {
                EmFab(
                    icon = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_transaction),
                    onClick = { onAction(TransactionListAction.OpenCreateTransaction) },
                )
            }
        },
    ) { innerPadding ->
        TransactionListScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
            state = state,
            onDelete = { onAction(TransactionListAction.DeleteTransaction(it.id)) },
            onSearchScopeChange = { onAction(TransactionListAction.SetSearchScope(it)) },
        ) { transaction ->
            onAction(TransactionListAction.OpenEdiTransaction(transaction.id))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────
//  Main list
// ─────────────────────────────────────────────────────────────────────

@Composable
private fun TransactionListScreen(
    state: TransactionListState,
    modifier: Modifier = Modifier,
    onDelete: ((TransactionUiItem) -> Unit)? = null,
    onSearchScopeChange: (SearchScope) -> Unit = {},
    onItemClick: ((TransactionUiItem) -> Unit)? = null,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 88.dp), // clear the FAB
    ) {
        item {
            FilterView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = EmTheme.spacing.sm),
            )
        }

        state.searchSummary?.let { summary ->
            item(key = "search_summary") {
                SearchSummaryRow(
                    summary = summary,
                    scope = state.searchScope,
                    onScopeChange = onSearchScopeChange,
                )
            }
        }

        if (state.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 80.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    EmLoading()
                }
            }
        } else if (state.transactionListItem.isEmpty()) {
            item {
                EmEmptyState(
                    illustration = painterResource(id = com.naveenapps.expensemanager.core.designsystem.R.drawable.ic_no_transaction),
                    title = if (state.searchQuery.isNotBlank()) {
                        stringResource(id = R.string.no_matching_transactions)
                    } else {
                        stringResource(id = R.string.no_transactions_available)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 80.dp),
                )
            }
        } else {
            val sections = buildSections(state.transactionListItem)

            sections.forEachIndexed { sectionIndex, section ->
                when (section) {
                    is Section.Header -> {
                        item(key = "header_${section.item.date}") {
                            TransactionHeaderItem(
                                date = section.item.date,
                                tone = section.item.amountTone,
                                totalAmount = section.item.totalAmount,
                            )
                        }
                    }

                    is Section.TransactionGroup -> {
                        item(key = "group_$sectionIndex") {
                            TransactionGroupCard(
                                transactions = section.items,
                                onItemClick = onItemClick,
                                onDelete = onDelete,
                            )
                        }
                    }
                }
            }
        }
    }
}

private sealed interface Section {
    data class Header(val item: TransactionListItem.HeaderItem) : Section
    data class TransactionGroup(val items: List<TransactionUiItem>) : Section
}

private fun buildSections(items: List<TransactionListItem>): List<Section> {
    val sections = mutableListOf<Section>()
    val pending = mutableListOf<TransactionUiItem>()

    fun flush() {
        if (pending.isNotEmpty()) {
            sections.add(Section.TransactionGroup(pending.toList()))
            pending.clear()
        }
    }

    for (item in items) {
        when (item) {
            is TransactionListItem.HeaderItem -> {
                flush()
                sections.add(Section.Header(item))
            }

            is TransactionListItem.TransactionItem -> {
                pending.add(item.date)
            }

            TransactionListItem.Divider -> {
                flush()
            }
        }
    }
    flush()
    return sections
}

@Composable
private fun TransactionGroupCard(
    transactions: List<TransactionUiItem>,
    onItemClick: ((TransactionUiItem) -> Unit)?,
    onDelete: ((TransactionUiItem) -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.xxs),
    ) {
        EmCard {
            transactions.forEachIndexed { index, item ->
                val debtKind = item.debtKind
                TransactionItem(
                    modifier = Modifier
                        .fillMaxWidth(),
                    // A debt entry reads "Lent to Ravi"; categoryName holds the person's name.
                    categoryName = if (debtKind != null) {
                        stringResource(debtKind.rowTitleRes(), item.categoryName)
                    } else {
                        item.categoryDisplayName()
                    },
                    categoryColor = item.categoryIcon.backgroundColor,
                    categoryIcon = item.categoryIcon.name,
                    amount = item.amount,
                    date = item.date,
                    notes = item.notes,
                    transactionType = item.transactionType,
                    isRecurring = item.isRecurring,
                    fromAccountName = item.fromAccountName,
                    fromAccountIcon = item.fromAccountIcon.name,
                    fromAccountColor = item.fromAccountIcon.backgroundColor,
                    toAccountName = item.toAccountName,
                    toAccountIcon = item.toAccountIcon?.name,
                    toAccountColor = item.toAccountIcon?.backgroundColor,
                    onClick = {
                        onItemClick?.invoke(item)
                    },
                    // Swipe to reveal edit/delete (also offered to TalkBack as actions). Debt entries
                    // are deleted from their own screen, where the account effect is explained.
                    onEdit = onItemClick?.let { click -> { click(item) } },
                    onDelete = if (debtKind != null) null else onDelete?.let { delete -> { delete(item) } },
                )

                // Thin inset divider between rows — not after the last one
                if (index < transactions.lastIndex) {
                    EmDivider()
                }
            }
        }
    }
}

/**
 * Shown while searching: how many transactions matched and what they add up to, which answers
 * "how much did I spend on X?". The chip switches between all time (the default) and the period
 * selected in the filter above.
 */
@Composable
private fun SearchSummaryRow(
    summary: SearchSummary,
    scope: SearchScope,
    onScopeChange: (SearchScope) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = EmTheme.spacing.xl,
                end = EmTheme.spacing.lg,
                top = EmTheme.spacing.xs,
                bottom = EmTheme.spacing.sm,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            EmText(
                text = pluralStringResource(R.plurals.search_result_count, summary.count, summary.count),
                role = EmTextRole.SectionTitle,
            )
            val totals = listOfNotNull(
                summary.spent?.let { stringResource(R.string.search_spent, it) },
                summary.earned?.let { stringResource(R.string.search_earned, it) },
            )
            if (totals.isNotEmpty()) {
                EmText(
                    text = totals.joinToString(" · "),
                    role = EmTextRole.Supporting,
                    color = EmTextColor.Supporting,
                )
            }
        }
        Spacer(modifier = Modifier.width(EmTheme.spacing.md))
        EmChip(
            label = stringResource(R.string.search_all_time),
            selected = scope == SearchScope.ALL_TIME,
            onClick = {
                onScopeChange(
                    if (scope == SearchScope.ALL_TIME) SearchScope.SELECTED_PERIOD else SearchScope.ALL_TIME,
                )
            },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────
//  Improved date header
// ─────────────────────────────────────────────────────────────────────

@Composable
fun TransactionHeaderItem(
    date: String,
    tone: AmountTone,
    totalAmount: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = EmTheme.spacing.xl, vertical = EmTheme.spacing.lg),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EmText(text = date.fromCompleteDate().toDate(), role = EmTextRole.Display)

            Spacer(Modifier.width(EmTheme.spacing.md))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                EmText(
                    text = date.fromCompleteDate().toMonthYear(),
                    role = EmTextRole.Label,
                    color = EmTextColor.Supporting,
                )
                EmText(
                    text = date.fromCompleteDate().toDay(),
                    role = EmTextRole.Supporting,
                    color = EmTextColor.Supporting,
                )
            }

            EmText(text = totalAmount, role = EmTextRole.SectionTitle, color = emColorOf(tone))
        }
    }
}

@Preview
@Composable
fun TransactionListItemEmptyStatePreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        TransactionListScreen(
            state = TransactionListState(emptyList()),
            modifier = Modifier.fillMaxSize(),
        )
    }
}

val DUMMY_DATA = listOf(
    getTransactionUiState("12/10/2023"),
    getTransactionUiState("13/10/2023"),
    getTransactionUiState("14/10/2023"),
)

fun getTransactionItem(id: String) = TransactionUiItem(
    id = id,
    notes = "Sample Description",
    amount = Amount(amount = 300.0, amountString = "300.00 ₹"),
    categoryName = "Clothing",
    transactionType = TransactionType.EXPENSE,
    categoryIcon = StoredIcon(
        name = "agriculture",
        backgroundColor = "#000000",
    ),
    fromAccountName = "DB Bank xxxx",
    fromAccountIcon = StoredIcon(
        name = "account_balance",
        backgroundColor = "#000000",
    ),
    date = Date().toCompleteDateWithDate(),
)

private fun getTransactionUiState(date: String) = TransactionGroup(
    date = date,
    amountTone = AmountTone.NEGATIVE,
    totalAmount = Amount(amount = 300.0, amountString = "300.00 ₹"),
    transactions = buildList {
        repeat(3) {
            add(getTransactionItem(it.toString()))
        }
    },
)

@EmPreviews
@Composable
fun TransactionListItemSuccessStatePreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        TransactionListScreenContent(
            state = TransactionListState(DUMMY_DATA.convertGroupToTransactionListItems()),
            showBackNavigationIcon = true,
            onAction = {},
        )
    }
}

/** Row title of a debt entry listed among transactions, e.g. "Lent to %1$s". */
private fun DebtEntryKind.rowTitleRes(): Int = when (this) {
    DebtEntryKind.LENT -> R.string.debt_row_lent
    DebtEntryKind.BORROWED -> R.string.debt_row_borrowed
    DebtEntryKind.RECEIVED -> R.string.debt_row_received
    DebtEntryKind.PAID -> R.string.debt_row_paid
}
