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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.common.utils.toCompleteDateWithDate
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountText
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmCardPosition
import com.naveenapps.expensemanager.core.designsystem.em.EmConfirm
import com.naveenapps.expensemanager.core.designsystem.em.EmEmptyAction
import com.naveenapps.expensemanager.core.designsystem.em.EmEmptyState
import com.naveenapps.expensemanager.core.designsystem.em.EmFab
import com.naveenapps.expensemanager.core.designsystem.em.EmMenuItem
import com.naveenapps.expensemanager.core.designsystem.em.EmOverflowMenu
import com.naveenapps.expensemanager.core.designsystem.em.EmScaffold
import com.naveenapps.expensemanager.core.designsystem.em.EmSearchField
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBar
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBarNavigation
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.IconAndBackgroundView
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.feature.transaction.R
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
        EmConfirm(
            title = stringResource(R.string.recurring_stop_title),
            message = stringResource(R.string.recurring_stop_message),
            confirmText = stringResource(R.string.recurring_stop),
            onConfirm = { onAction(RecurringTransactionsAction.ConfirmStop) },
            onDismiss = { onAction(RecurringTransactionsAction.DismissStop) },
        )
    }

    // Same always-visible search box as the transaction list. Back clears an active search first.
    BackHandler(enabled = state.searchQuery.isNotEmpty()) {
        onAction(RecurringTransactionsAction.Search(""))
    }

    EmScaffold(
        floatingActionButton = {
            // The empty state has its own add button; avoid showing two.
            if (state.hasAnyRules) {
                EmFab(
                    icon = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_recurring_transaction),
                    onClick = { onAction(RecurringTransactionsAction.OpenCreate) },
                )
            }
        },
        topBar = {
            Column {
                EmTopBar(
                    navigation = EmTopBarNavigation.Back,
                    onNavigationClick = { onAction(RecurringTransactionsAction.ClosePage) },
                    title = stringResource(R.string.recurring_transactions),
                )
                // Nothing to search until there's at least one rule.
                if (state.hasAnyRules) {
                    EmSearchField(
                        query = state.searchQuery,
                        onQueryChange = { onAction(RecurringTransactionsAction.Search(it)) },
                        placeholder = stringResource(R.string.search_recurring_hint),
                        modifier = Modifier.padding(
                            start = EmTheme.spacing.lg,
                            end = EmTheme.spacing.lg,
                            bottom = EmTheme.spacing.sm,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        val items = state.items ?: return@EmScaffold
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
                contentPadding = PaddingValues(
                    start = EmTheme.spacing.lg,
                    end = EmTheme.spacing.lg,
                    top = EmTheme.spacing.sm,
                    bottom = 88.dp,
                ),
            ) {
                itemsIndexed(items, key = { _, item -> item.rule.id }) { index, item ->
                    EmCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = EmTheme.spacing.xxs),
                        position = EmCardPosition.of(index, items),
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
        TransactionType.EXPENSE -> EmTheme.colors.expense
        TransactionType.INCOME -> EmTheme.colors.income
        else -> EmTheme.colors.scheme.onSurface
    }

    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(
                start = EmTheme.spacing.lg,
                top = EmTheme.spacing.md,
                bottom = EmTheme.spacing.md,
                end = EmTheme.spacing.xs,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        rule.category?.let {
            IconAndBackgroundView(
                icon = it.storedIcon.name,
                iconBackgroundColor = it.storedIcon.backgroundColor,
                name = categoryName,
                customImagePath = it.storedIcon.customImagePath,
            )
            Spacer(modifier = Modifier.width(EmTheme.spacing.md))
        }
        Column(modifier = Modifier.weight(1f)) {
            EmText(
                text = title,
                role = EmTextRole.ItemTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            EmText(
                text = stringResource(
                    R.string.recurring_schedule,
                    stringResource(rule.frequency.labelResId),
                    rule.nextDueDate.toCompleteDateWithDate(),
                ),
                role = EmTextRole.Supporting,
                color = EmTextColor.Supporting,
            )
            if (account.isNotBlank()) {
                EmText(
                    text = account,
                    role = EmTextRole.Supporting,
                    color = EmTextColor.Supporting,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(modifier = Modifier.width(EmTheme.spacing.sm))
        EmAmountText(
            amount = item.amount,
            color = amountColor,
        )
        EmOverflowMenu(
            contentDescription = stringResource(R.string.recurring_more_options),
            items = listOf(
                EmMenuItem(label = stringResource(R.string.recurring_edit), onClick = onClick),
                EmMenuItem(label = stringResource(R.string.recurring_stop), onClick = onStop, destructive = true),
            ),
        )
    }
}

@Composable
private fun RecurringNoMatches(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(EmTheme.spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        EmText(
            text = stringResource(R.string.recurring_no_matches),
            color = EmTextColor.Supporting,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun RecurringEmptyState(onAdd: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        EmEmptyState(
            icon = Icons.Outlined.EventRepeat,
            title = stringResource(R.string.recurring_empty_title),
            body = stringResource(R.string.recurring_empty_message),
            action = EmEmptyAction(stringResource(R.string.add_recurring_transaction), onAdd),
        )
    }
}
