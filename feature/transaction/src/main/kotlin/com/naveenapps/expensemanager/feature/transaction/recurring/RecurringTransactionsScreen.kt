package com.naveenapps.expensemanager.feature.transaction.recurring

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.common.utils.toCompleteDateWithDate
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardViewDefaults
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.core.designsystem.ui.components.IconAndBackgroundView
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.feature.transaction.R
import com.naveenapps.expensemanager.feature.transaction.list.TransactionSearchBar
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RecurringTransactionsScreen(
    viewModel: RecurringTransactionsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    RecurringTransactionsContent(
        state = state,
        onAction = viewModel::processAction,
    )
}

@Composable
private fun RecurringTransactionsContent(
    state: RecurringTransactionsState,
    onAction: (RecurringTransactionsAction) -> Unit,
) {
    state.pendingStop?.let {
        AlertDialog(
            onDismissRequest = { onAction(RecurringTransactionsAction.DismissStop) },
            icon = { Icon(Icons.Outlined.EventRepeat, contentDescription = null) },
            title = { Text(text = stringResource(R.string.recurring_stop_title)) },
            text = { Text(text = stringResource(R.string.recurring_stop_message)) },
            confirmButton = {
                TextButton(onClick = { onAction(RecurringTransactionsAction.ConfirmStop) }) {
                    Text(text = stringResource(R.string.recurring_stop))
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(RecurringTransactionsAction.DismissStop) }) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            },
        )
    }

    // Same top-bar search pattern as the transaction list.
    var isSearchActive by rememberSaveable { mutableStateOf(state.searchQuery.isNotEmpty()) }
    val closeSearch = {
        isSearchActive = false
        onAction(RecurringTransactionsAction.Search(""))
    }
    BackHandler(enabled = isSearchActive, onBack = closeSearch)

    Scaffold(
        floatingActionButton = {
            // The empty state has its own add button; avoid showing two.
            if (state.hasAnyRules && !isSearchActive) {
                FloatingActionButton(
                    onClick = { onAction(RecurringTransactionsAction.OpenCreate) },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_recurring_transaction),
                    )
                }
            }
        },
        topBar = {
            if (isSearchActive) {
                TransactionSearchBar(
                    query = state.searchQuery,
                    onQueryChange = { onAction(RecurringTransactionsAction.Search(it)) },
                    onClose = closeSearch,
                    placeholder = stringResource(R.string.search_recurring_hint),
                )
            } else {
                ExpenseManagerTopAppBar(
                    navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                    navigationBackClick = { onAction(RecurringTransactionsAction.ClosePage) },
                    title = stringResource(R.string.recurring_transactions),
                    actions = {
                        if (state.hasAnyRules) {
                            IconButton(onClick = { isSearchActive = true }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = stringResource(R.string.search_recurring),
                                )
                            }
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        val items = state.items ?: return@Scaffold
        when {
            !state.hasAnyRules -> RecurringEmptyState(
                modifier = Modifier.padding(innerPadding),
                onAdd = { onAction(RecurringTransactionsAction.OpenCreate) },
            )

            items.isEmpty() -> RecurringNoMatches(modifier = Modifier.padding(innerPadding))

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                // Bottom padding keeps the last row clear of the add button.
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
            ) {
                itemsIndexed(items, key = { _, item -> item.rule.id }) { index, item ->
                    AppCardView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 2.dp),
                        shape = AppCardViewDefaults.cardShape(index, items),
                    ) {
                        RecurringRow(
                            item = item,
                            onClick = { onAction(RecurringTransactionsAction.OpenEdit(item)) },
                            onStop = { onAction(RecurringTransactionsAction.AskToStop(item)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecurringRow(item: RecurringItemUi, onClick: () -> Unit, onStop: () -> Unit) {
    val rule = item.rule
    val categoryName = rule.category?.let { category -> category.titleResId?.let { stringResource(it) } ?: category.name }.orEmpty()
    val title = rule.notes.ifBlank { categoryName }
    val account = listOfNotNull(rule.fromAccount?.name, rule.toAccount?.name).joinToString(" → ")
    val amountColor = when (rule.type) {
        TransactionType.EXPENSE -> colorResource(com.naveenapps.expensemanager.core.common.R.color.red_500)
        TransactionType.INCOME -> colorResource(com.naveenapps.expensemanager.core.common.R.color.green_500)
        else -> MaterialTheme.colorScheme.onSurface
    }

    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        rule.category?.let {
            IconAndBackgroundView(
                icon = it.storedIcon.name,
                iconBackgroundColor = it.storedIcon.backgroundColor,
                name = categoryName,
                customImagePath = it.storedIcon.customImagePath,
            )
            Spacer(modifier = Modifier.width(14.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(
                    R.string.recurring_schedule,
                    stringResource(rule.frequency.labelResId),
                    rule.nextDueDate.toCompleteDateWithDate(),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (account.isNotBlank()) {
                Text(
                    text = account,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = item.amount,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = amountColor,
        )
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.recurring_more_options),
                )
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.recurring_edit)) },
                    onClick = {
                        showMenu = false
                        onClick()
                    },
                )
                DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.recurring_stop)) },
                    onClick = {
                        showMenu = false
                        onStop()
                    },
                )
            }
        }
    }
}

@Composable
private fun RecurringNoMatches(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.recurring_no_matches),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun RecurringEmptyState(onAdd: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.EventRepeat,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        Text(
            text = stringResource(R.string.recurring_empty_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.recurring_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onAdd,
            modifier = Modifier.padding(top = 24.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
            Text(text = stringResource(R.string.add_recurring_transaction))
        }
    }
}
