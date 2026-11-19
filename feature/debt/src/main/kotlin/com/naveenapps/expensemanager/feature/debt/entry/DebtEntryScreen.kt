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
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.common.utils.toCompleteDateWithDate
import com.naveenapps.expensemanager.core.designsystem.R as DesignSystemR
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountField
import com.naveenapps.expensemanager.core.designsystem.em.EmBottomActionBar
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmChip
import com.naveenapps.expensemanager.core.designsystem.em.EmChipVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmConfirm
import com.naveenapps.expensemanager.core.designsystem.em.EmIconButton
import com.naveenapps.expensemanager.core.designsystem.em.EmModalSheet
import com.naveenapps.expensemanager.core.designsystem.em.EmPickerField
import com.naveenapps.expensemanager.core.designsystem.em.EmScaffold
import com.naveenapps.expensemanager.core.designsystem.em.EmSection
import com.naveenapps.expensemanager.core.designsystem.em.EmSnackbarHostState
import com.naveenapps.expensemanager.core.designsystem.em.EmSwitch
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextField
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBar
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBarNavigation
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppDatePickerDialog
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
    val snackbarHostState = remember { EmSnackbarHostState() }
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
    snackbarHostState: EmSnackbarHostState,
) {
    EntryDialogs(state = state, onAction = onAction)

    EmScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            EmTopBar(
                navigation = EmTopBarNavigation.Back,
                onNavigationClick = { onAction(DebtEntryAction.ClosePage) },
                title = stringResource(if (state.isEditing) R.string.debt_edit_entry else R.string.debt_new_entry),
                actions = {
                    if (state.isEditing) {
                        EmIconButton(
                            icon = Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.debt_delete),
                            onClick = { onAction(DebtEntryAction.ShowDelete) },
                            tint = EmTheme.colors.scheme.error,
                        )
                    }
                },
            )
        },
        bottomBar = {
            if (state.isLoaded) {
                EmBottomActionBar(
                    primaryText = stringResource(R.string.debt_save),
                    onPrimaryClick = { onAction(DebtEntryAction.Save) },
                    primaryIcon = Icons.Default.Done,
                )
            }
        },
    ) { innerPadding ->
        if (!state.isLoaded) return@EmScaffold
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
        modifier = modifier.padding(horizontal = EmTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.lg),
    ) {
        FlowRow(
            modifier = Modifier.padding(top = EmTheme.spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
        ) {
            DebtEntryKind.entries.forEach { kind ->
                EmChip(
                    label = stringResource(kind.labelRes()),
                    selected = state.kind == kind,
                    onClick = { onAction(DebtEntryAction.SelectKind(kind)) },
                )
            }
        }

        PersonField(state = state, onAction = onAction)

        EmAmountField(
            value = state.amount.value,
            onValueChange = { state.amount.onValueChange?.invoke(it) },
            label = stringResource(id = R.string.debt_amount),
            modifier = Modifier.fillMaxWidth(),
            isError = state.amount.valueError,
            errorMessage = stringResource(R.string.debt_amount_error),
            trailing = {
                EmIconButton(
                    icon = Icons.Outlined.Calculate,
                    contentDescription = stringResource(DesignSystemR.string.a11y_open_calculator),
                    onClick = {
                        focusManager.clearFocus(force = true)
                        onAction(DebtEntryAction.ShowNumberPad)
                    },
                )
            },
        )

        AccountSection(state = state, onAction = onAction)

        EmPickerField(
            label = stringResource(id = R.string.debt_date),
            value = state.date.toCompleteDateWithDate(),
            onClick = {
                focusManager.clearFocus(force = true)
                onAction(DebtEntryAction.ShowDateSelection)
            },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = Icons.Outlined.EditCalendar,
        )

        if (state.kind.isNewDebt) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmPickerField(
                    label = stringResource(id = R.string.debt_due_date),
                    value = state.dueDate?.toCompleteDateWithDate().orEmpty(),
                    onClick = {
                        focusManager.clearFocus(force = true)
                        onAction(DebtEntryAction.ShowDueDateSelection)
                    },
                    modifier = Modifier.weight(1f),
                    leadingIcon = Icons.Outlined.Event,
                )
                if (state.dueDate != null) {
                    EmIconButton(
                        icon = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.debt_clear_due),
                        onClick = { onAction(DebtEntryAction.ClearDueDate) },
                    )
                }
            }
        }

        EmTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.note.value,
            onValueChange = { state.note.onValueChange?.invoke(it) },
            maxLines = 3,
            leadingIcon = Icons.AutoMirrored.Filled.Notes,
            label = stringResource(R.string.debt_note),
            placeholder = stringResource(R.string.debt_note_hint),
        )

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PersonField(state: DebtEntryState, onAction: (DebtEntryAction) -> Unit) {
    val fixed = state.fixedPersonName
    if (fixed != null) {
        EmTextField(
            modifier = Modifier.fillMaxWidth(),
            value = fixed,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            leadingIcon = Icons.Outlined.Person,
            label = stringResource(R.string.debt_person),
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm)) {
        EmTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.personName.value,
            onValueChange = { state.personName.onValueChange?.invoke(it) },
            singleLine = true,
            isError = state.personName.valueError,
            leadingIcon = Icons.Outlined.Person,
            label = stringResource(R.string.debt_person),
            placeholder = stringResource(R.string.debt_person_hint),
            supportingText = if (state.personName.valueError) stringResource(R.string.debt_person_error) else null,
        )
        // Someone already in the ledger: tap instead of typing (and avoid duplicates).
        val typed = state.personName.value.trim()
        val suggestions = state.people
            .filter { typed.isEmpty() || it.name.contains(typed, ignoreCase = true) }
            .filterNot { it.name.equals(typed, ignoreCase = true) }
            .take(MAX_SUGGESTIONS)
        if (suggestions.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm)) {
                suggestions.forEach { person ->
                    EmChip(
                        label = person.name,
                        onClick = { onAction(DebtEntryAction.PickPerson(person)) },
                        variant = EmChipVariant.Suggestion,
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountSection(state: DebtEntryState, onAction: (DebtEntryAction) -> Unit) {
    EmSection(title = stringResource(state.kind.accountLabelRes())) {
        EmCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(
                    start = EmTheme.spacing.lg,
                    end = EmTheme.spacing.sm,
                    top = EmTheme.spacing.md,
                    bottom = EmTheme.spacing.md,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    EmText(
                        text = stringResource(R.string.debt_move_money),
                    )
                    EmText(
                        text = stringResource(R.string.debt_move_money_message),
                        role = EmTextRole.Supporting,
                        color = EmTextColor.Supporting,
                    )
                }
                EmSwitch(
                    checked = state.linkAccount,
                    onCheckedChange = { onAction(DebtEntryAction.SetLinkAccount(it)) },
                )
            }
        }
        val account = state.selectedAccount
        if (state.linkAccount && account != null) {
            Spacer(modifier = Modifier.height(EmTheme.spacing.sm))
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

        state.showAccountSelection -> EmModalSheet(
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

        state.showDeleteDialog -> EmConfirm(
            title = stringResource(R.string.debt_delete_entry_title),
            message = stringResource(R.string.debt_delete_entry_message),
            confirmText = stringResource(R.string.debt_delete),
            onConfirm = { onAction(DebtEntryAction.ConfirmDelete) },
            onDismiss = { onAction(DebtEntryAction.DismissDelete) },
            destructive = true,
        )
    }
}

private const val MAX_SUGGESTIONS = 6
