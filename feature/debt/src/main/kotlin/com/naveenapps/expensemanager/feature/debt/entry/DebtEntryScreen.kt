package com.naveenapps.expensemanager.feature.debt.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.common.utils.toCompleteDateWithDate
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppDatePickerDialog
import com.naveenapps.expensemanager.core.designsystem.ui.components.ClickableTextField
import com.naveenapps.expensemanager.core.designsystem.ui.components.DecimalTextField
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.core.designsystem.ui.components.SafeModalBottomSheet
import com.naveenapps.expensemanager.core.designsystem.ui.components.SettingsSection
import com.naveenapps.expensemanager.core.designsystem.utils.ObserveAsEvents
import com.naveenapps.expensemanager.core.model.DebtEntryKind
import com.naveenapps.expensemanager.feature.account.selection.AccountItem
import com.naveenapps.expensemanager.feature.account.selection.AccountItemDefaults
import com.naveenapps.expensemanager.feature.account.selection.AccountSelectionScreen
import com.naveenapps.expensemanager.feature.debt.R
import com.naveenapps.expensemanager.feature.debt.accountLabelRes
import com.naveenapps.expensemanager.feature.debt.labelRes
import com.naveenapps.expensemanager.feature.transaction.numberpad.NumberPadDialogView
import java.util.Date
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DebtEntryScreen(viewModel: DebtEntryViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val resources = LocalContext.current.resources

    ObserveAsEvents(viewModel.event) {
        when (it) {
            is DebtEntryEvent.ShowMessage -> coroutineScope.launch {
                snackbarHostState.showSnackbar(resources.getString(it.messageRes))
            }
        }
    }

    DebtEntryContent(state = state, onAction = viewModel::processAction, snackbarHostState = snackbarHostState)
}

@Composable
private fun DebtEntryContent(
    state: DebtEntryState,
    onAction: (DebtEntryAction) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    EntryDialogs(state = state, onAction = onAction)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            ExpenseManagerTopAppBar(
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationBackClick = { onAction(DebtEntryAction.ClosePage) },
                title = stringResource(if (state.isEditing) R.string.debt_edit_entry else R.string.debt_new_entry),
                actions = {
                    if (state.isEditing) {
                        IconButton(onClick = { onAction(DebtEntryAction.ShowDelete) }) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = stringResource(R.string.debt_delete),
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (state.isLoaded) {
                ExtendedFloatingActionButton(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    onClick = { onAction(DebtEntryAction.Save) },
                    icon = { Icon(imageVector = Icons.Default.Done, contentDescription = null) },
                    text = { Text(text = stringResource(R.string.debt_save)) },
                )
            }
        },
    ) { innerPadding ->
        if (!state.isLoaded) return@Scaffold
        EntryForm(
            state = state,
            onAction = onAction,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EntryForm(state: DebtEntryState, onAction: (DebtEntryAction) -> Unit, modifier: Modifier = Modifier) {
    val focusManager = LocalFocusManager.current
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        FlowRow(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DebtEntryKind.entries.forEach { kind ->
                FilterChip(
                    selected = state.kind == kind,
                    onClick = { onAction(DebtEntryAction.SelectKind(kind)) },
                    label = { Text(stringResource(kind.labelRes())) },
                )
            }
        }

        PersonField(state = state, onAction = onAction)

        DecimalTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.amount.value,
            isError = state.amount.valueError,
            onValueChange = state.amount.onValueChange,
            label = R.string.debt_amount,
            errorMessage = stringResource(R.string.debt_amount_error),
            trailingIcon = {
                IconButton(onClick = {
                    focusManager.clearFocus(force = true)
                    onAction(DebtEntryAction.ShowNumberPad)
                }) {
                    Icon(Icons.Outlined.Calculate, contentDescription = null)
                }
            },
        )

        AccountSection(state = state, onAction = onAction)

        ClickableTextField(
            modifier = Modifier.fillMaxWidth(),
            label = R.string.debt_date,
            value = state.date.toCompleteDateWithDate(),
            leadingIcon = Icons.Outlined.EditCalendar,
            onClick = {
                focusManager.clearFocus(force = true)
                onAction(DebtEntryAction.ShowDateSelection)
            },
        )

        if (state.kind.isNewDebt) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ClickableTextField(
                    modifier = Modifier.weight(1f),
                    label = R.string.debt_due_date,
                    value = state.dueDate?.toCompleteDateWithDate().orEmpty(),
                    leadingIcon = Icons.Outlined.Event,
                    onClick = {
                        focusManager.clearFocus(force = true)
                        onAction(DebtEntryAction.ShowDueDateSelection)
                    },
                )
                if (state.dueDate != null) {
                    IconButton(onClick = { onAction(DebtEntryAction.ClearDueDate) }) {
                        Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.debt_clear_due))
                    }
                }
            }
        }

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.note.value,
            onValueChange = { state.note.onValueChange?.invoke(it) },
            maxLines = 3,
            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null) },
            label = { Text(stringResource(R.string.debt_note)) },
            placeholder = { Text(stringResource(R.string.debt_note_hint)) },
        )

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PersonField(state: DebtEntryState, onAction: (DebtEntryAction) -> Unit) {
    val fixed = state.fixedPersonName
    if (fixed != null) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = fixed,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
            label = { Text(stringResource(R.string.debt_person)) },
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.personName.value,
            onValueChange = { state.personName.onValueChange?.invoke(it) },
            singleLine = true,
            isError = state.personName.valueError,
            leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
            label = { Text(stringResource(R.string.debt_person)) },
            placeholder = { Text(stringResource(R.string.debt_person_hint)) },
            supportingText = if (state.personName.valueError) {
                { Text(stringResource(R.string.debt_person_error)) }
            } else {
                null
            },
        )
        // Someone already in the ledger: tap instead of typing (and avoid duplicates).
        val typed = state.personName.value.trim()
        val suggestions = state.people
            .filter { typed.isEmpty() || it.name.contains(typed, ignoreCase = true) }
            .filterNot { it.name.equals(typed, ignoreCase = true) }
            .take(MAX_SUGGESTIONS)
        if (suggestions.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                suggestions.forEach { person ->
                    SuggestionChip(
                        onClick = { onAction(DebtEntryAction.PickPerson(person)) },
                        label = { Text(person.name) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountSection(state: DebtEntryState, onAction: (DebtEntryAction) -> Unit) {
    SettingsSection(title = stringResource(state.kind.accountLabelRes())) {
        AppCardView(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.debt_move_money),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = stringResource(R.string.debt_move_money_message),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = state.linkAccount,
                    onCheckedChange = { onAction(DebtEntryAction.SetLinkAccount(it)) },
                )
            }
        }
        val account = state.selectedAccount
        if (state.linkAccount && account != null) {
            Spacer(modifier = Modifier.height(8.dp))
            AccountItem(
                name = account.name,
                icon = account.storedIcon.name,
                iconBackgroundColor = account.storedIcon.backgroundColor,
                amount = account.amount.amountString,
                amountTextColor = account.amountTextColor,
                customImagePath = account.storedIcon.customImagePath,
                modifier = Modifier.fillMaxWidth(),
                onClick = { onAction(DebtEntryAction.ShowAccountSelection) },
                trailingContent = { AccountItemDefaults.ChevronTrailing() },
            )
        }
    }
}

@Composable
private fun EntryDialogs(state: DebtEntryState, onAction: (DebtEntryAction) -> Unit) {
    when {
        state.showNumberPad -> NumberPadDialogView(
            initialAmount = state.numberPadInitialAmount,
            onConfirm = { onAction(DebtEntryAction.SetNumberPadValue(it)) },
        )

        state.showAccountSelection -> SafeModalBottomSheet(
            onDismissRequest = { onAction(DebtEntryAction.DismissAccountSelection) },
        ) {
            AccountSelectionScreen(
                accounts = state.accounts,
                selectedAccount = state.selectedAccount,
                createNewCallback = null,
                onItemSelection = { onAction(DebtEntryAction.SelectAccount(it)) },
            )
        }

        state.showDateSelection -> AppDatePickerDialog(
            selectedDate = state.date,
            onDateSelected = { onAction(DebtEntryAction.SelectDate(it)) },
            onDismiss = { onAction(DebtEntryAction.DismissDateSelection) },
        )

        state.showDueDateSelection -> AppDatePickerDialog(
            selectedDate = state.dueDate ?: Date(),
            onDateSelected = { onAction(DebtEntryAction.SelectDueDate(it)) },
            onDismiss = { onAction(DebtEntryAction.DismissDateSelection) },
        )

        state.showDeleteDialog -> AlertDialog(
            onDismissRequest = { onAction(DebtEntryAction.DismissDelete) },
            title = { Text(stringResource(R.string.debt_delete_entry_title)) },
            text = { Text(stringResource(R.string.debt_delete_entry_message)) },
            confirmButton = {
                TextButton(onClick = { onAction(DebtEntryAction.ConfirmDelete) }) {
                    Text(stringResource(R.string.debt_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(DebtEntryAction.DismissDelete) }) {
                    Text(stringResource(R.string.debt_cancel))
                }
            },
        )
    }
}

private const val MAX_SUGGESTIONS = 6
