package com.naveenapps.expensemanager.feature.debt.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.CallMade
import androidx.compose.material.icons.automirrored.outlined.CallReceived
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.common.R as CommonR
import com.naveenapps.expensemanager.core.common.utils.toCompleteDateWithDate
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardViewDefaults
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
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
    Scaffold(
        topBar = {
            ExpenseManagerTopAppBar(
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationBackClick = { onAction(DebtListAction.ClosePage) },
                title = stringResource(R.string.debt_title),
            )
        },
        bottomBar = {
            if (state.isLoaded && state.people.isNotEmpty()) {
                AddEntryBar(onAction = onAction)
            }
        },
    ) { innerPadding ->
        if (!state.isLoaded) return@Scaffold
        if (state.people.isEmpty()) {
            DebtEmptyState(modifier = Modifier.padding(innerPadding), onAction = onAction)
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
        ) {
            item {
                DebtTotalsCard(
                    toGet = state.toGet,
                    toGive = state.toGive,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }
            itemsIndexed(state.people, key = { _, row -> row.id }) { index, row ->
                AppCardView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 2.dp),
                    shape = AppCardViewDefaults.cardShape(index, state.people),
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
    AppCardView(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(modifier = Modifier.padding(16.dp)) {
            TotalColumn(
                label = stringResource(R.string.debt_you_get),
                amount = toGet,
                color = colorResource(CommonR.color.green_500),
                modifier = Modifier.weight(1f),
            )
            TotalColumn(
                label = stringResource(R.string.debt_you_give),
                amount = toGive,
                color = colorResource(CommonR.color.red_500),
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
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = amount,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PersonRow(row: DebtPersonRow, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PersonAvatar(name = row.name)
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = row.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
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
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (row.isOverdue) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
        if (row.status != DebtStatus.SETTLED) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = row.amount,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = row.status.color(),
                )
                Text(
                    text = stringResource(
                        if (row.status == DebtStatus.OWES_YOU) R.string.debt_owes_you else R.string.debt_you_owe,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AddEntryBar(onAction: (DebtListAction) -> Unit) {
    Surface(tonalElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = { onAction(DebtListAction.AddEntry(DebtEntryKind.LENT)) },
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.AutoMirrored.Outlined.CallMade, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.debt_add_lent))
            }
            FilledTonalButton(
                onClick = { onAction(DebtListAction.AddEntry(DebtEntryKind.BORROWED)) },
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.AutoMirrored.Outlined.CallReceived, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.debt_add_borrowed))
            }
        }
    }
}

@Composable
private fun DebtEmptyState(onAction: (DebtListAction) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.People,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        Text(
            text = stringResource(R.string.debt_empty_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.debt_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { onAction(DebtListAction.AddEntry(DebtEntryKind.LENT)) }) {
                Text(text = stringResource(R.string.debt_add_lent))
            }
            FilledTonalButton(onClick = { onAction(DebtListAction.AddEntry(DebtEntryKind.BORROWED)) }) {
                Text(text = stringResource(R.string.debt_add_borrowed))
            }
        }
    }
}
