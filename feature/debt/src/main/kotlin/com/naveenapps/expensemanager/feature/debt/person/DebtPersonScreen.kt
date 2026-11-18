package com.naveenapps.expensemanager.feature.debt.person

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.common.utils.toCompleteDateWithDate
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardViewDefaults
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.core.model.DebtEntryKind
import com.naveenapps.expensemanager.feature.debt.DebtStatus
import com.naveenapps.expensemanager.feature.debt.PersonAvatar
import com.naveenapps.expensemanager.feature.debt.R
import com.naveenapps.expensemanager.feature.debt.color
import com.naveenapps.expensemanager.feature.debt.labelRes
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DebtPersonScreen(viewModel: DebtPersonViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    DebtPersonContent(state = state, onAction = viewModel::processAction)
}

@Composable
private fun DebtPersonContent(state: DebtPersonState, onAction: (DebtPersonAction) -> Unit) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    PersonDialogs(state = state, onAction = onAction)

    Scaffold(
        topBar = {
            ExpenseManagerTopAppBar(
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationBackClick = { onAction(DebtPersonAction.ClosePage) },
                title = state.name,
                actions = {
                    if (state.status == DebtStatus.OWES_YOU) {
                        IconButton(onClick = {
                            context.shareReminder(state)
                            onAction(DebtPersonAction.ReminderShared)
                        }) {
                            Icon(
                                Icons.Outlined.Share,
                                contentDescription = stringResource(R.string.debt_share_reminder),
                            )
                        }
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.debt_more_options),
                            )
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.debt_rename)) },
                                onClick = {
                                    showMenu = false
                                    onAction(DebtPersonAction.ShowRename)
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.debt_delete)) },
                                onClick = {
                                    showMenu = false
                                    onAction(DebtPersonAction.ShowDelete)
                                },
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        if (!state.isLoaded) return@Scaffold
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        ) {
            item { BalanceHeader(state = state, onAction = onAction) }
            item {
                Text(
                    text = stringResource(R.string.debt_history),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
                )
            }
            itemsIndexed(state.entries, key = { _, row -> row.id }) { index, row ->
                AppCardView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 2.dp),
                    shape = AppCardViewDefaults.cardShape(index, state.entries),
                ) {
                    EntryRow(row = row, onClick = { onAction(DebtPersonAction.OpenEntry(row.id)) })
                }
            }
        }
    }
}

@Composable
private fun BalanceHeader(state: DebtPersonState, onAction: (DebtPersonAction) -> Unit) {
    AppCardView(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PersonAvatar(name = state.name, size = 48.dp)
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (state.status) {
                            DebtStatus.OWES_YOU -> stringResource(R.string.debt_owes_you_title, state.name)
                            DebtStatus.YOU_OWE -> stringResource(R.string.debt_you_owe_title, state.name)
                            DebtStatus.SETTLED -> stringResource(R.string.debt_all_settled)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (state.status != DebtStatus.SETTLED) {
                        Text(
                            text = state.balance,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = state.status.color(),
                        )
                    }
                    state.dueDate?.let { due ->
                        Text(
                            text = if (state.isOverdue) {
                                stringResource(R.string.debt_overdue_since, due.toCompleteDateWithDate())
                            } else {
                                stringResource(R.string.debt_due_on, due.toCompleteDateWithDate())
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (state.isOverdue) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (state.status != DebtStatus.SETTLED) {
                Button(onClick = { onAction(DebtPersonAction.SettleUp) }, modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(R.string.debt_settle_up))
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { onAction(DebtPersonAction.AddEntry(DebtEntryKind.LENT)) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = stringResource(R.string.debt_add_lent), maxLines = 1)
                }
                OutlinedButton(
                    onClick = { onAction(DebtPersonAction.AddEntry(DebtEntryKind.BORROWED)) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = stringResource(R.string.debt_add_borrowed), maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun EntryRow(row: DebtEntryRow, onClick: () -> Unit) {
    val details = listOfNotNull(
        row.date.toCompleteDateWithDate(),
        row.accountName,
        row.dueDate?.let { stringResource(R.string.debt_due_on, it.toCompleteDateWithDate()) },
    ).joinToString(" · ")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(row.kind.labelRes()),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = details,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (row.note.isNotBlank()) {
                Text(
                    text = row.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        // Lent / paid back add to what they owe; borrowed / got back reduce it.
        val status = if (row.kind.balanceSign > 0) DebtStatus.OWES_YOU else DebtStatus.YOU_OWE
        Text(
            text = row.amount,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = status.color(),
        )
    }
}

@Composable
private fun PersonDialogs(state: DebtPersonState, onAction: (DebtPersonAction) -> Unit) {
    if (state.showRenameDialog) {
        var name by rememberSaveable { mutableStateOf(state.name) }
        AlertDialog(
            onDismissRequest = { onAction(DebtPersonAction.DismissRename) },
            title = { Text(stringResource(R.string.debt_rename_title)) },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.debt_person_hint)) },
                )
            },
            confirmButton = {
                TextButton(onClick = { onAction(DebtPersonAction.Rename(name)) }, enabled = name.isNotBlank()) {
                    Text(stringResource(R.string.debt_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(DebtPersonAction.DismissRename) }) {
                    Text(stringResource(R.string.debt_cancel))
                }
            },
        )
    }
    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { onAction(DebtPersonAction.DismissDelete) },
            title = { Text(stringResource(R.string.debt_delete_person_title, state.name)) },
            text = { Text(stringResource(R.string.debt_delete_person_message)) },
            confirmButton = {
                TextButton(onClick = { onAction(DebtPersonAction.ConfirmDelete) }) {
                    Text(stringResource(R.string.debt_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(DebtPersonAction.DismissDelete) }) {
                    Text(stringResource(R.string.debt_cancel))
                }
            },
        )
    }
}

/** "Hi Ravi, just a gentle reminder about the ₹1,500 from 1 Nov. Thanks!" via the share sheet. */
private fun Context.shareReminder(state: DebtPersonState) {
    val message = state.openSince?.let {
        getString(R.string.debt_reminder_message, state.name, state.balance, it.toCompleteDateWithDate())
    } ?: getString(R.string.debt_reminder_message_no_date, state.name, state.balance)
    val send = Intent(Intent.ACTION_SEND)
        .setType("text/plain")
        .putExtra(Intent.EXTRA_TEXT, message)
    runCatching { startActivity(Intent.createChooser(send, getString(R.string.debt_share_reminder))) }
}
