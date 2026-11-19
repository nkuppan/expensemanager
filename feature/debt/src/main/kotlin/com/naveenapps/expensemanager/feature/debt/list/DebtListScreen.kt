package com.naveenapps.expensemanager.feature.debt.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.CallMade
import androidx.compose.material.icons.automirrored.outlined.CallReceived
import androidx.compose.material.icons.outlined.People
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.common.utils.toCompleteDateWithDate
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountSize
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountText
import com.naveenapps.expensemanager.core.designsystem.em.EmButton
import com.naveenapps.expensemanager.core.designsystem.em.EmButtonVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmCardPosition
import com.naveenapps.expensemanager.core.designsystem.em.EmEmptyAction
import com.naveenapps.expensemanager.core.designsystem.em.EmEmptyState
import com.naveenapps.expensemanager.core.designsystem.em.EmScaffold
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBar
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBarNavigation
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.model.DebtEntryKind
import com.naveenapps.expensemanager.feature.debt.DebtStatus
import com.naveenapps.expensemanager.feature.debt.PersonAvatar
import com.naveenapps.expensemanager.feature.debt.R
import com.naveenapps.expensemanager.feature.debt.color
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DebtListScreen(viewModel: DebtListViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    DebtListContent(state = state, onAction = viewModel::processAction)
}

@Composable
private fun DebtListContent(state: DebtListState, onAction: (DebtListAction) -> Unit) {
    EmScaffold(
        topBar = {
            EmTopBar(
                navigation = EmTopBarNavigation.Back,
                onNavigationClick = { onAction(DebtListAction.ClosePage) },
                title = stringResource(R.string.debt_title),
            )
        },
        bottomBar = {
            if (state.isLoaded && state.people.isNotEmpty()) {
                AddEntryBar(onAction = onAction)
            }
        },
    ) { innerPadding ->
        if (!state.isLoaded) return@EmScaffold
        if (state.people.isEmpty()) {
            DebtEmptyState(modifier = Modifier.padding(innerPadding), onAction = onAction)
            return@EmScaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = EmTheme.spacing.lg,
                end = EmTheme.spacing.lg,
                top = EmTheme.spacing.sm,
                bottom = EmTheme.spacing.lg,
            ),
        ) {
            item {
                DebtTotalsCard(
                    toGet = state.toGet,
                    toGive = state.toGive,
                    modifier = Modifier.padding(bottom = EmTheme.spacing.lg),
                )
            }
            itemsIndexed(state.people, key = { _, row -> row.id }) { index, row ->
                EmCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = EmTheme.spacing.xxs),
                    position = EmCardPosition.of(index, state.people),
                ) {
                    PersonRow(row = row, onClick = { onAction(DebtListAction.OpenPerson(row.id)) })
                }
            }
        }
    }
}

/** "You'll get ₹3,200 · You'll give ₹800". Also used on Home. */
@Composable
fun DebtTotalsCard(
    toGet: String,
    toGive: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    EmCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(modifier = Modifier.padding(EmTheme.spacing.lg)) {
            TotalColumn(
                label = stringResource(R.string.debt_you_get),
                amount = toGet,
                color = EmTheme.colors.income,
                modifier = Modifier.weight(1f),
            )
            TotalColumn(
                label = stringResource(R.string.debt_you_give),
                amount = toGive,
                color = EmTheme.colors.expense,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun TotalColumn(
    label: String,
    amount: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        EmText(
            text = label,
            role = EmTextRole.Supporting,
            color = EmTextColor.Supporting,
        )
        EmAmountText(
            amount = amount,
            color = color,
            size = EmAmountSize.Large,
        )
    }
}

@Composable
private fun PersonRow(row: DebtPersonRow, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PersonAvatar(name = row.name)
        Spacer(modifier = Modifier.width(EmTheme.spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            EmText(
                text = row.name,
                role = EmTextRole.ItemTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val subtitle = when {
                row.status == DebtStatus.SETTLED -> stringResource(R.string.debt_settled)

                row.dueDate != null && row.isOverdue ->
                    stringResource(R.string.debt_overdue_since, row.dueDate.toCompleteDateWithDate())

                row.dueDate != null -> stringResource(R.string.debt_due_on, row.dueDate.toCompleteDateWithDate())

                row.status == DebtStatus.OWES_YOU -> stringResource(R.string.debt_owes_you)

                else -> stringResource(R.string.debt_you_owe)
            }
            EmText(
                text = subtitle,
                role = EmTextRole.Supporting,
                color = if (row.isOverdue) {
                    EmTheme.colors.scheme.error
                } else {
                    EmTheme.colors.scheme.onSurfaceVariant
                },
            )
        }
        if (row.status != DebtStatus.SETTLED) {
            Column(horizontalAlignment = Alignment.End) {
                EmAmountText(
                    amount = row.amount,
                    color = row.status.color(),
                )
                EmText(
                    text = stringResource(
                        if (row.status == DebtStatus.OWES_YOU) R.string.debt_owes_you else R.string.debt_you_owe,
                    ),
                    role = EmTextRole.Supporting,
                    color = EmTextColor.Supporting,
                )
            }
        }
    }
}

@Composable
private fun AddEntryBar(onAction: (DebtListAction) -> Unit) {
    Box(modifier = Modifier.background(EmTheme.colors.scheme.surfaceContainer)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.md),
            horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.md),
        ) {
            EmButton(
                text = stringResource(R.string.debt_add_lent),
                onClick = { onAction(DebtListAction.AddEntry(DebtEntryKind.LENT)) },
                modifier = Modifier.weight(1f),
                leadingIcon = Icons.AutoMirrored.Outlined.CallMade,
            )
            EmButton(
                text = stringResource(R.string.debt_add_borrowed),
                onClick = { onAction(DebtListAction.AddEntry(DebtEntryKind.BORROWED)) },
                modifier = Modifier.weight(1f),
                variant = EmButtonVariant.Secondary,
                leadingIcon = Icons.AutoMirrored.Outlined.CallReceived,
            )
        }
    }
}

@Composable
private fun DebtEmptyState(onAction: (DebtListAction) -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        EmEmptyState(
            icon = Icons.Outlined.People,
            title = stringResource(R.string.debt_empty_title),
            body = stringResource(R.string.debt_empty_message),
            action = EmEmptyAction(stringResource(R.string.debt_add_lent)) {
                onAction(DebtListAction.AddEntry(DebtEntryKind.LENT))
            },
            secondaryAction = EmEmptyAction(stringResource(R.string.debt_add_borrowed)) {
                onAction(DebtListAction.AddEntry(DebtEntryKind.BORROWED))
            },
        )
    }
}
