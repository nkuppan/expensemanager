package com.naveenapps.expensemanager.feature.transaction.list

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.common.utils.fromCompleteDate
import com.naveenapps.expensemanager.core.common.utils.toCompleteDateWithDate
import com.naveenapps.expensemanager.core.common.utils.toDate
import com.naveenapps.expensemanager.core.common.utils.toDay
import com.naveenapps.expensemanager.core.common.utils.toMonthYear
import com.naveenapps.expensemanager.core.designsystem.R as DesignSystemR
import com.naveenapps.expensemanager.core.designsystem.components.EmptyItem
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.SheetSearchField
import com.naveenapps.expensemanager.core.designsystem.ui.components.TransactionItem
import com.naveenapps.expensemanager.core.designsystem.utils.ObserveAsEvents
import com.naveenapps.expensemanager.core.model.Amount
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
    val snackbarHostState = remember { SnackbarHostState() }
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
                    duration = SnackbarDuration.Long,
                )
                viewModel.processAction(
                    if (result == SnackbarResult.ActionPerformed) {
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
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    // Search is always visible under the title (no tap to open it, and it doesn't grab focus, so
    // the keyboard only appears when the user taps it). Back clears an active search first.
    BackHandler(enabled = state.searchQuery.isNotEmpty()) {
        onAction(TransactionListAction.Search(""))
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            // The app draws edge to edge and there's no app bar to take the status-bar inset, so this
            // row keeps clear of the status bar itself.
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(
                        start = if (showBackNavigationIcon) 4.dp else 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 8.dp,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Opened as its own screen (not the Home tab): a way back, at the start of the row.
                if (showBackNavigationIcon) {
                    IconButton(onClick = { onAction(TransactionListAction.ClosePage) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(DesignSystemR.string.a11y_back),
                        )
                    }
                }
                SheetSearchField(
                    query = state.searchQuery,
                    onQueryChange = { onAction(TransactionListAction.Search(it)) },
                    placeholder = stringResource(R.string.search_transactions_hint),
                    modifier = Modifier.weight(1f),
                    // Recurring transactions sit at the end of the search box.
                    trailingAction = {
                        IconButton(onClick = { onAction(TransactionListAction.OpenRecurringTransactions) }) {
                            Icon(
                                imageVector = Icons.Outlined.EventRepeat,
                                contentDescription = stringResource(R.string.recurring_transactions),
                            )
                        }
                    },
                )
            }
        },
        floatingActionButton = {
            if (showAddButton) {
                FloatingActionButton(
                    onClick = { onAction(TransactionListAction.OpenCreateTransaction) },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 10.dp,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_transaction),
                    )
                }
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
                    .padding(end = 6.dp),
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
                    CircularProgressIndicator()
                }
            }
        } else if (state.transactionListItem.isEmpty()) {
            item {
                EmptyItem(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 80.dp),
                    emptyItemText = if (state.searchQuery.isNotBlank()) {
                        stringResource(id = R.string.no_matching_transactions)
                    } else {
                        stringResource(id = R.string.no_transactions_available)
                    },
                    icon = com.naveenapps.expensemanager.core.designsystem.R.drawable.ic_no_transaction,
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
                                textColor = section.item.amountTextColor,
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
            .padding(horizontal = 16.dp, vertical = 2.dp),
    ) {
        AppCardView {
            transactions.forEachIndexed { index, item ->
                TransactionItem(
                    modifier = Modifier
                        .fillMaxWidth(),
                    categoryName = item.categoryTitleResId?.let { stringResource(it) }
                        ?: item.categoryName,
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
                    // Swipe to reveal edit/delete (also offered to TalkBack as actions).
                    onEdit = onItemClick?.let { click -> { click(item) } },
                    onDelete = onDelete?.let { delete -> { delete(item) } },
                )

                // Thin inset divider between rows — not after the last one
                if (index < transactions.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    )
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
            .padding(start = 20.dp, end = 16.dp, top = 4.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = pluralStringResource(R.plurals.search_result_count, summary.count, summary.count),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            val totals = listOfNotNull(
                summary.spent?.let { stringResource(R.string.search_spent, it) },
                summary.earned?.let { stringResource(R.string.search_earned, it) },
            )
            if (totals.isNotEmpty()) {
                Text(
                    text = totals.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        FilterChip(
            selected = scope == SearchScope.ALL_TIME,
            onClick = {
                onScopeChange(
                    if (scope == SearchScope.ALL_TIME) SearchScope.SELECTED_PERIOD else SearchScope.ALL_TIME,
                )
            },
            label = { Text(text = stringResource(R.string.search_all_time)) },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────
//  Improved date header
// ─────────────────────────────────────────────────────────────────────

@Composable
fun TransactionHeaderItem(
    date: String,
    textColor: Int,
    totalAmount: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = date.fromCompleteDate().toDate(),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Text(
                    text = date.fromCompleteDate().toMonthYear(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
                Text(
                    text = date.fromCompleteDate().toDay(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    ),
                )
            }

            Text(
                text = totalAmount,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.3).sp,
                ),
                color = colorResource(id = textColor),
            )
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
    amountTextColor = com.naveenapps.expensemanager.core.common.R.color.red_500,
    totalAmount = Amount(amount = 300.0, amountString = "300.00 ₹"),
    transactions = buildList {
        repeat(3) {
            add(getTransactionItem(it.toString()))
        }
    },
)

@AppPreviewsLightAndDarkMode
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
