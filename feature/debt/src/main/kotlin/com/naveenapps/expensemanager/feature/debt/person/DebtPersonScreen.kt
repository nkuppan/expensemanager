package com.naveenapps.expensemanager.feature.debt.person

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.common.utils.toCompleteDateWithDate
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountSize
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountText
import com.naveenapps.expensemanager.core.designsystem.em.EmBottomSheet
import com.naveenapps.expensemanager.core.designsystem.em.EmButton
import com.naveenapps.expensemanager.core.designsystem.em.EmButtonVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmCardPosition
import com.naveenapps.expensemanager.core.designsystem.em.EmConfirm
import com.naveenapps.expensemanager.core.designsystem.em.EmIconButton
import com.naveenapps.expensemanager.core.designsystem.em.EmMenuItem
import com.naveenapps.expensemanager.core.designsystem.em.EmOverflowMenu
import com.naveenapps.expensemanager.core.designsystem.em.EmScaffold
import com.naveenapps.expensemanager.core.designsystem.em.EmSectionHeader
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextField
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBar
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBarNavigation
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
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

    PersonDialogs(state = state, onAction = onAction)

    EmScaffold(
        topBar = {
            EmTopBar(
                navigation = EmTopBarNavigation.Back,
                onNavigationClick = { onAction(DebtPersonAction.ClosePage) },
                title = state.name,
                actions = {
                    if (state.status == DebtStatus.OWES_YOU) {
                        EmIconButton(
                            icon = Icons.Outlined.Share,
                            contentDescription = stringResource(R.string.debt_share_reminder),
                            onClick = {
                            context.shareReminder(state)
                            onAction(DebtPersonAction.ReminderShared)
                        },
                        )
                    }
                    EmOverflowMenu(
                        contentDescription = stringResource(R.string.debt_more_options),
                        items = listOf(
                            EmMenuItem(
                                label = stringResource(R.string.debt_rename),
                                onClick = { onAction(DebtPersonAction.ShowRename) },
                            ),
                            EmMenuItem(
                                label = stringResource(R.string.debt_delete),
                                onClick = { onAction(DebtPersonAction.ShowDelete) },
                                destructive = true,
                            ),
                        ),
                    )
                },
            )
        },
    ) { innerPadding ->
        if (!state.isLoaded) return@EmScaffold
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = EmTheme.spacing.lg,
                end = EmTheme.spacing.lg,
                top = EmTheme.spacing.sm,
                bottom = EmTheme.spacing.xl,
            ),
        ) {
            item { BalanceHeader(state = state, onAction = onAction) }
            item {
                EmSectionHeader(
                    title = stringResource(R.string.debt_history),
                    modifier = Modifier.padding(top = EmTheme.spacing.xl, bottom = EmTheme.spacing.xs),
                )
            }
            itemsIndexed(state.entries, key = { _, row -> row.id }) { index, row ->
                EmCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = EmTheme.spacing.xxs),
                    position = EmCardPosition.of(index, state.entries),
                ) {
                    EntryRow(row = row, onClick = { onAction(DebtPersonAction.OpenEntry(row.id)) })
                }
            }
        }
    }
}

@Composable
private fun BalanceHeader(state: DebtPersonState, onAction: (DebtPersonAction) -> Unit) {
    EmCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(EmTheme.spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PersonAvatar(name = state.name, size = 48.dp)
                Spacer(modifier = Modifier.width(EmTheme.spacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    EmText(
                        text = when (state.status) {
                            DebtStatus.OWES_YOU -> stringResource(R.string.debt_owes_you_title, state.name)
                            DebtStatus.YOU_OWE -> stringResource(R.string.debt_you_owe_title, state.name)
                            DebtStatus.SETTLED -> stringResource(R.string.debt_all_settled)
                        },
                        color = EmTextColor.Supporting,
                    )
                    if (state.status != DebtStatus.SETTLED) {
                        EmAmountText(
                            amount = state.balance,
                            color = state.status.color(),
                            size = EmAmountSize.Large,
                        )
                    }
                    state.dueDate?.let { due ->
                        EmText(
                            text = if (state.isOverdue) {
                                stringResource(R.string.debt_overdue_since, due.toCompleteDateWithDate())
                            } else {
                                stringResource(R.string.debt_due_on, due.toCompleteDateWithDate())
                            },
                            role = EmTextRole.Supporting,
                            color = if (state.isOverdue) {
                                EmTheme.colors.scheme.error
                            } else {
                                EmTheme.colors.scheme.onSurfaceVariant
                            },
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(EmTheme.spacing.lg))
            if (state.status != DebtStatus.SETTLED) {
                EmButton(
                    text = stringResource(R.string.debt_settle_up),
                    onClick = { onAction(DebtPersonAction.SettleUp) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(EmTheme.spacing.sm))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm)) {
                EmButton(
                    text = stringResource(R.string.debt_add_lent),
                    onClick = { onAction(DebtPersonAction.AddEntry(DebtEntryKind.LENT)) },
                    modifier = Modifier.weight(1f),
                    variant = EmButtonVariant.Secondary,
                )
                EmButton(
                    text = stringResource(R.string.debt_add_borrowed),
                    onClick = { onAction(DebtPersonAction.AddEntry(DebtEntryKind.BORROWED)) },
                    modifier = Modifier.weight(1f),
                    variant = EmButtonVariant.Secondary,
                )
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
            .padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            EmText(
                text = stringResource(row.kind.labelRes()),
                role = EmTextRole.ItemTitle,
            )
            EmText(
                text = details,
                role = EmTextRole.Supporting,
                color = EmTextColor.Supporting,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (row.note.isNotBlank()) {
                EmText(
                    text = row.note,
                    role = EmTextRole.Supporting,
                    color = EmTextColor.Supporting,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(modifier = Modifier.width(EmTheme.spacing.sm))
        // Lent / paid back add to what they owe; borrowed / got back reduce it.
        val status = if (row.kind.balanceSign > 0) DebtStatus.OWES_YOU else DebtStatus.YOU_OWE
        EmAmountText(
            amount = row.amount,
            color = status.color(),
        )
    }
}

@Composable
private fun PersonDialogs(state: DebtPersonState, onAction: (DebtPersonAction) -> Unit) {
    if (state.showRenameDialog) {
        var name by rememberSaveable { mutableStateOf(state.name) }
        EmBottomSheet(
            onDismissRequest = { onAction(DebtPersonAction.DismissRename) },
            title = stringResource(R.string.debt_rename_title),
            actions = {
                EmButton(
                    text = stringResource(R.string.debt_cancel),
                    onClick = { onAction(DebtPersonAction.DismissRename) },
                    variant = EmButtonVariant.Secondary,
                    modifier = Modifier.weight(1f),
                )
                EmButton(
                    text = stringResource(R.string.debt_save),
                    onClick = { onAction(DebtPersonAction.Rename(name)) },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.weight(1f),
                )
            },
        ) {
            EmTextField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(R.string.debt_person_hint),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
    if (state.showDeleteDialog) {
        EmConfirm(
            title = stringResource(R.string.debt_delete_person_title, state.name),
            message = stringResource(R.string.debt_delete_person_message),
            confirmText = stringResource(R.string.debt_delete),
            onConfirm = { onAction(DebtPersonAction.ConfirmDelete) },
            onDismiss = { onAction(DebtPersonAction.DismissDelete) },
            destructive = true,
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
