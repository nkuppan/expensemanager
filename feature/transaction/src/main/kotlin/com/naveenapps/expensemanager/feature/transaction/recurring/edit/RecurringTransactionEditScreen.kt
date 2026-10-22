package com.naveenapps.expensemanager.feature.transaction.recurring.edit

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.common.utils.toCompleteDateWithDate
import com.naveenapps.expensemanager.core.common.utils.toTimeAndMinutes
import com.naveenapps.expensemanager.core.designsystem.R as DesignSystemR
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppDatePickerDialog
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppDialog
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppTimePickerDialog
import com.naveenapps.expensemanager.core.designsystem.ui.components.ClickableTextField
import com.naveenapps.expensemanager.core.designsystem.ui.components.DecimalTextField
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.core.designsystem.ui.components.SafeModalBottomSheet
import com.naveenapps.expensemanager.core.designsystem.ui.components.SettingsSection
import com.naveenapps.expensemanager.core.designsystem.utils.ObserveAsEvents
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.RecurringFrequency
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.feature.account.selection.AccountItem
import com.naveenapps.expensemanager.feature.account.selection.AccountItemDefaults
import com.naveenapps.expensemanager.feature.account.selection.AccountSelectionScreen
import com.naveenapps.expensemanager.feature.category.selection.CategoryItem
import com.naveenapps.expensemanager.feature.category.selection.CategoryItemDefaults
import com.naveenapps.expensemanager.feature.category.selection.CategorySelectionScreen
import com.naveenapps.expensemanager.feature.transaction.R
import com.naveenapps.expensemanager.feature.transaction.create.AccountSelection
import com.naveenapps.expensemanager.feature.transaction.create.toTime
import com.naveenapps.expensemanager.feature.transaction.numberpad.NumberPadDialogView
import com.naveenapps.expensemanager.feature.transaction.recurring.labelResId
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RecurringTransactionEditScreen(
    viewModel: RecurringTransactionEditViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val resources = LocalContext.current.resources

    ObserveAsEvents(viewModel.event) {
        when (it) {
            is RecurringTransactionEditEvent.ShowMessage -> coroutineScope.launch {
                snackbarHostState.showSnackbar(resources.getString(it.messageRes))
            }
        }
    }

    RecurringTransactionEditContent(
        state = state,
        onAction = viewModel::processAction,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
private fun RecurringTransactionEditContent(
    state: RecurringTransactionEditState,
    onAction: (RecurringTransactionEditAction) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    BackHandler { onAction(RecurringTransactionEditAction.ClosePage) }

    RecurringEditDialogs(state = state, onAction = onAction)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            ExpenseManagerTopAppBar(
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationBackClick = { onAction(RecurringTransactionEditAction.ClosePage) },
                title = stringResource(R.string.edit_recurring_transaction),
                actions = {
                    IconButton(onClick = { onAction(RecurringTransactionEditAction.ShowStopDialog) }) {
                        Icon(
                            imageVector = Icons.Outlined.EventBusy,
                            contentDescription = stringResource(R.string.recurring_stop),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            if (state.isLoaded) {
                ExtendedFloatingActionButton(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    onClick = { onAction(RecurringTransactionEditAction.Save) },
                    icon = { Icon(imageVector = Icons.Default.Done, contentDescription = null) },
                    text = { Text(text = stringResource(R.string.save)) },
                )
            }
        },
    ) { innerPadding ->
        if (!state.isLoaded) return@Scaffold
        RecurringEditForm(
            state = state,
            onAction = onAction,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun RecurringEditDialogs(
    state: RecurringTransactionEditState,
    onAction: (RecurringTransactionEditAction) -> Unit,
) {
    when {
        state.showDiscardDialog -> AppDialog(
            onDismissRequest = { onAction(RecurringTransactionEditAction.DismissDiscardDialog) },
            onConfirmation = { onAction(RecurringTransactionEditAction.ConfirmDiscard) },
            dialogTitle = stringResource(R.string.discard_changes_title),
            dialogText = stringResource(R.string.discard_changes_message),
            positiveButtonText = stringResource(R.string.discard),
            negativeButtonText = stringResource(R.string.keep_editing),
        )

        state.showStopDialog -> AlertDialog(
            onDismissRequest = { onAction(RecurringTransactionEditAction.DismissStopDialog) },
            icon = { Icon(Icons.Outlined.EventRepeat, contentDescription = null) },
            title = { Text(text = stringResource(R.string.recurring_stop_title)) },
            text = { Text(text = stringResource(R.string.recurring_stop_message)) },
            confirmButton = {
                TextButton(onClick = { onAction(RecurringTransactionEditAction.ConfirmStop) }) {
                    Text(text = stringResource(R.string.recurring_stop))
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(RecurringTransactionEditAction.DismissStopDialog) }) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            },
        )

        state.showNumberPad -> NumberPadDialogView(
            initialAmount = state.numberPadInitialAmount,
            onConfirm = { onAction(RecurringTransactionEditAction.SetNumberPadValue(it)) },
        )

        state.showCategorySelection -> SafeModalBottomSheet(
            onDismissRequest = { onAction(RecurringTransactionEditAction.DismissCategorySelection) },
        ) {
            CategorySelectionScreen(
                categories = state.categories,
                selectedCategory = state.selectedCategory,
                // Creating a category from here would leave the screen mid-edit; keep it to picking.
                createNewCallback = null,
                onItemSelection = { onAction(RecurringTransactionEditAction.SelectCategory(it)) },
            )
        }

        state.showAccountSelection -> SafeModalBottomSheet(
            onDismissRequest = { onAction(RecurringTransactionEditAction.DismissAccountSelection) },
        ) {
            AccountSelectionScreen(
                accounts = state.accounts,
                selectedAccount = when (state.accountSelection) {
                    AccountSelection.FROM_ACCOUNT -> state.selectedFromAccount
                    AccountSelection.TO_ACCOUNT -> state.selectedToAccount
                },
                createNewCallback = null,
                onItemSelection = { onAction(RecurringTransactionEditAction.SelectAccount(it)) },
            )
        }

        state.showDateSelection -> AppDatePickerDialog(
            selectedDate = state.nextDueDate,
            onDateSelected = { onAction(RecurringTransactionEditAction.SelectDate(it)) },
            onDismiss = { onAction(RecurringTransactionEditAction.DismissDateSelection) },
        )

        state.showTimeSelection -> {
            val time = state.nextDueDate.toTime()
            AppTimePickerDialog(
                reminderTimeState = Triple(time.hour, time.minute, time.is24Hour),
                onTimeSelected = { onAction(RecurringTransactionEditAction.SelectTime(it.first, it.second)) },
                onDismiss = { onAction(RecurringTransactionEditAction.DismissDateSelection) },
            )
        }
    }
}

@Composable
private fun RecurringEditForm(
    state: RecurringTransactionEditState,
    onAction: (RecurringTransactionEditAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        FutureOnlyNotice(modifier = Modifier.padding(top = 8.dp))

        SettingsSection(title = stringResource(R.string.details)) {
            AppCardView {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(state.transactionType.labelRes()),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    DecimalTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.amount.value,
                        isError = state.amount.valueError,
                        onValueChange = state.amount.onValueChange,
                        label = R.string.amount,
                        errorMessage = stringResource(id = R.string.amount_error_message),
                        trailingIcon = {
                            IconButton(onClick = { onAction(RecurringTransactionEditAction.ShowNumberPad) }) {
                                Icon(
                                    imageVector = Icons.Outlined.Calculate,
                                    contentDescription = stringResource(DesignSystemR.string.a11y_open_calculator),
                                )
                            }
                        },
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.notes.value,
                        singleLine = false,
                        maxLines = 3,
                        leadingIcon = { Icon(imageVector = Icons.AutoMirrored.Filled.Notes, contentDescription = null) },
                        placeholder = { Text(text = stringResource(id = R.string.optional_details)) },
                        label = { Text(text = stringResource(id = R.string.notes)) },
                        onValueChange = { state.notes.onValueChange?.invoke(it) },
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(force = true) }),
                    )
                }
            }
        }

        SettingsSection(title = stringResource(R.string.repeat)) {
            AppCardView {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    FrequencySelector(
                        selected = state.frequency,
                        onSelect = { onAction(RecurringTransactionEditAction.SelectFrequency(it)) },
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        ClickableTextField(
                            modifier = Modifier.weight(1f),
                            value = state.nextDueDate.toCompleteDateWithDate(),
                            label = R.string.recurring_next_date,
                            leadingIcon = Icons.Outlined.EditCalendar,
                            onClick = {
                                focusManager.clearFocus(force = true)
                                onAction(RecurringTransactionEditAction.ShowDateSelection)
                            },
                        )
                        ClickableTextField(
                            modifier = Modifier.weight(1f),
                            value = state.nextDueDate.toTimeAndMinutes(),
                            label = R.string.select_time,
                            leadingIcon = Icons.Outlined.AccessTime,
                            onClick = {
                                focusManager.clearFocus(force = true)
                                onAction(RecurringTransactionEditAction.ShowTimeSelection)
                            },
                        )
                    }
                    if (state.isScheduleChanged) {
                        Text(
                            text = stringResource(
                                R.string.recurring_schedule_change_hint,
                                state.nextDueDate.toCompleteDateWithDate(),
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }

        if (state.transactionType != TransactionType.TRANSFER) {
            state.selectedCategory?.let { category ->
                SettingsSection(title = stringResource(R.string.select_category)) {
                    CategoryItem(
                        name = category.titleResId?.let { stringResource(it) } ?: category.name,
                        icon = category.storedIcon.name,
                        iconBackgroundColor = category.storedIcon.backgroundColor,
                        customImagePath = category.storedIcon.customImagePath,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            focusManager.clearFocus(force = true)
                            onAction(RecurringTransactionEditAction.ShowCategorySelection)
                        },
                        trailingContent = { CategoryItemDefaults.ChevronTrailing() },
                    )
                }
            }
        }

        AccountSection(
            title = if (state.transactionType == TransactionType.TRANSFER) R.string.from_account else R.string.select_account,
            account = state.selectedFromAccount,
            onClick = {
                focusManager.clearFocus(force = true)
                onAction(RecurringTransactionEditAction.ShowAccountSelection(AccountSelection.FROM_ACCOUNT))
            },
        )

        if (state.transactionType == TransactionType.TRANSFER) {
            AccountSection(
                title = R.string.to_account,
                account = state.selectedToAccount,
                onClick = {
                    focusManager.clearFocus(force = true)
                    onAction(RecurringTransactionEditAction.ShowAccountSelection(AccountSelection.TO_ACCOUNT))
                },
            )
        }

        Spacer(modifier = Modifier.height(72.dp))
    }
}

@Composable
private fun AccountSection(title: Int, account: AccountUiModel?, onClick: () -> Unit) {
    account ?: return
    SettingsSection(title = stringResource(title)) {
        AccountItem(
            name = account.name,
            icon = account.storedIcon.name,
            iconBackgroundColor = account.storedIcon.backgroundColor,
            amount = account.amount.amountString,
            amountTextColor = account.amountTextColor,
            customImagePath = account.storedIcon.customImagePath,
            modifier = Modifier.fillMaxWidth(),
            onClick = onClick,
            trailingContent = { AccountItemDefaults.ChevronTrailing() },
        )
    }
}

/** States the parent/occurrence rule up front so nobody expects past entries to change. */
@Composable
private fun FutureOnlyNotice(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = stringResource(R.string.recurring_edit_future_only),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FrequencySelector(
    selected: RecurringFrequency,
    onSelect: (RecurringFrequency) -> Unit,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        RecurringFrequency.entries.forEach { frequency ->
            FilterChip(
                selected = selected == frequency,
                onClick = { onSelect(frequency) },
                label = { Text(text = stringResource(id = frequency.labelResId)) },
                leadingIcon = if (selected == frequency) {
                    {
                        Icon(
                            imageVector = Icons.Outlined.Repeat,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                        )
                    }
                } else {
                    null
                },
            )
        }
    }
}

private fun TransactionType.labelRes(): Int = when (this) {
    TransactionType.INCOME -> R.string.income
    TransactionType.EXPENSE -> R.string.expense
    TransactionType.TRANSFER -> R.string.transfer
}
