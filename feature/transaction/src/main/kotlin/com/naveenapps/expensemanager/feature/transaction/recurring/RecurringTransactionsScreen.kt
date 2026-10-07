package com.naveenapps.expensemanager.feature.transaction.recurring

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.common.utils.toCompleteDateWithDate
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardViewDefaults
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.core.designsystem.ui.components.IconAndBackgroundView
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.feature.transaction.R
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RecurringTransactionsScreen(
    viewModel: RecurringTransactionsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    state.pendingStop?.let { item ->
        AlertDialog(
            onDismissRequest = viewModel::dismissStop,
            icon = { Icon(Icons.Outlined.EventRepeat, contentDescription = null) },
            title = { Text(text = stringResource(R.string.recurring_stop_title)) },
            text = { Text(text = stringResource(R.string.recurring_stop_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::confirmStop) {
                    Text(text = stringResource(R.string.recurring_stop))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissStop) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            },
        )
    }

    Scaffold(
        topBar = {
            ExpenseManagerTopAppBar(
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationBackClick = viewModel::closePage,
                title = stringResource(R.string.recurring_transactions),
            )
        },
    ) { innerPadding ->
        val items = state.items ?: return@Scaffold
        if (items.isEmpty()) {
            RecurringEmptyState(modifier = Modifier.padding(innerPadding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            ) {
                itemsIndexed(items, key = { _, item -> item.rule.id }) { index, item ->
                    AppCardView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 2.dp),
                        shape = AppCardViewDefaults.cardShape(index, items),
                    ) {
                        RecurringRow(item = item, onStop = { viewModel.askToStop(item) })
                    }
                }
            }
        }
    }
}

@Composable
private fun RecurringRow(item: RecurringItemUi, onStop: () -> Unit) {
    val rule = item.rule
    val categoryName = rule.category?.let { category -> category.titleResId?.let { stringResource(it) } ?: category.name }.orEmpty()
    val title = rule.notes.ifBlank { categoryName }
    val account = listOfNotNull(rule.fromAccount?.name, rule.toAccount?.name).joinToString(" → ")
    val amountColor = when (rule.type) {
        TransactionType.EXPENSE -> colorResource(com.naveenapps.expensemanager.core.common.R.color.red_500)
        TransactionType.INCOME -> colorResource(com.naveenapps.expensemanager.core.common.R.color.green_500)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
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
        TextButton(onClick = onStop) {
            Text(text = stringResource(R.string.recurring_stop))
        }
    }
}

@Composable
private fun RecurringEmptyState(modifier: Modifier = Modifier) {
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
    }
}
