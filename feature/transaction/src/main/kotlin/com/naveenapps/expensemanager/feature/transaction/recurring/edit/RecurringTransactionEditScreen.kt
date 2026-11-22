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
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Repeat
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
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountField
import com.naveenapps.expensemanager.core.designsystem.em.EmBottomActionBar
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmChip
import com.naveenapps.expensemanager.core.designsystem.em.EmConfirm
import com.naveenapps.expensemanager.core.designsystem.em.EmIcon
import com.naveenapps.expensemanager.core.designsystem.em.EmIconButton
import com.naveenapps.expensemanager.core.designsystem.em.EmModalSheet
import com.naveenapps.expensemanager.core.designsystem.em.EmPickerField
import com.naveenapps.expensemanager.core.designsystem.em.EmScaffold
import com.naveenapps.expensemanager.core.designsystem.em.EmSection
import com.naveenapps.expensemanager.core.designsystem.em.EmSnackbarHostState
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextField
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBar
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBarNavigation
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppDatePickerDialog
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppTimePickerDialog
import com.naveenapps.expensemanager.core.designsystem.ui.utils.displayName
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
    val snackbarHostState = remember { EmSnackbarHostState() }
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
    snackbarHostState: EmSnackbarHostState = remember { EmSnackbarHostState() },
) {
    BackHandler { onAction(RecurringTransactionEditAction.ClosePage) }

    RecurringEditDialogs(state = state, onAction = onAction)

    EmScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            EmTopBar(
                navigation = EmTopBarNavigation.Back,
                onNavigationClick = { onAction(RecurringTransactionEditAction.ClosePage) },
                title = stringResource(R.string.edit_recurring_transaction),
                actions = {
                    EmIconButton(
                        icon = Icons.Outlined.EventBusy,
                        contentDescription = stringResource(R.string.recurring_stop),
                        onClick = { onAction(RecurringTransactionEditAction.ShowStopDialog) },
                        tint = EmTheme.colors.scheme.error,
                    )
                },
            )
        },
        bottomBar = {
            if (state.isLoaded) {
                EmBottomActionBar(
                    primaryText = stringResource(R.string.save),
                    onPrimaryClick = { onAction(RecurringTransactionEditAction.Save) },
                    primaryIcon = Icons.Default.Done,
                )
            }
        },
    ) { innerPadding ->
        if (!state.isLoaded) return@EmScaffold
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
private fun RecurringEditDialogs(
    state: RecurringTransactionEditState,
    onAction: (RecurringTransactionEditAction) -> Unit,
) {
    when {
        state.showDiscardDialog -> EmConfirm(
            title = stringResource(R.string.discard_changes_title),
            message = stringResource(R.string.discard_changes_message),
            confirmText = stringResource(R.string.discard),
            onConfirm = { onAction(RecurringTransactionEditAction.ConfirmDiscard) },
            onDismiss = { onAction(RecurringTransactionEditAction.DismissDiscardDialog) },
            dismissText = stringResource(R.string.keep_editing),
            destructive = true,
        )

        state.showStopDialog -> EmConfirm(
            title = stringResource(R.string.recurring_stop_title),
            message = stringResource(R.string.recurring_stop_message),
            confirmText = stringResource(R.string.recurring_stop),
            onConfirm = { onAction(RecurringTransactionEditAction.ConfirmStop) },
            onDismiss = { onAction(RecurringTransactionEditAction.DismissStopDialog) },
        )

        state.showNumberPad -> NumberPadDialogView(
            initialAmount = state.numberPadInitialAmount,
            onConfirm = { onAction(RecurringTransactionEditAction.SetNumberPadValue(it)) },
        )

        state.showCategorySelection -> EmModalSheet(
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

        state.showAccountSelection -> EmModalSheet(
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
        modifier = modifier.padding(horizontal = EmTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.lg),
    ) {
        FutureOnlyNotice(modifier = Modifier.padding(top = EmTheme.spacing.sm))

        EmSection(title = stringResource(R.string.details)) {
            EmCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(EmTheme.spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.xs),
                ) {
                    EmText(
                        text = stringResource(state.transactionType.labelRes()),
                        role = EmTextRole.Label,
                        color = EmTextColor.Supporting,
                    )
                    EmAmountField(
                        value = state.amount.value,
                        onValueChange = { state.amount.onValueChange?.invoke(it) },
                        label = stringResource(id = R.string.amount),
                        modifier = Modifier.fillMaxWidth(),
                        isError = state.amount.valueError,
                        errorMessage = stringResource(id = R.string.amount_error_message),
                        trailing = {
                            EmIconButton(
                                icon = Icons.Outlined.Calculate,
                                contentDescription = stringResource(DesignSystemR.string.a11y_open_calculator),
                                onClick = { onAction(RecurringTransactionEditAction.ShowNumberPad) },
                            )
                        },
                    )
                    EmTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.notes.value,
                        singleLine = false,
                        maxLines = 3,
                        leadingIcon = Icons.AutoMirrored.Filled.Notes,
                        placeholder = stringResource(id = R.string.optional_details),
                        label = stringResource(id = R.string.notes),
                        onValueChange = { state.notes.onValueChange?.invoke(it) },
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(force = true) }),
                    )
                }
            }
        }

        EmSection(title = stringResource(R.string.repeat)) {
            EmCard {
                Column(
                    modifier = Modifier.padding(EmTheme.spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.md),
                ) {
                    FrequencySelector(
                        selected = state.frequency,
                        onSelect = { onAction(RecurringTransactionEditAction.SelectFrequency(it)) },
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.md),
                    ) {
                        EmPickerField(
                            label = stringResource(id = R.string.recurring_next_date),
                            value = state.nextDueDate.toCompleteDateWithDate(),
                            onClick = {
                                focusManager.clearFocus(force = true)
                                onAction(RecurringTransactionEditAction.ShowDateSelection)
                            },
                            modifier = Modifier.weight(1f),
                            leadingIcon = Icons.Outlined.EditCalendar,
                        )
                        EmPickerField(
                            label = stringResource(id = R.string.select_time),
                            value = state.nextDueDate.toTimeAndMinutes(),
                            onClick = {
                                focusManager.clearFocus(force = true)
                                onAction(RecurringTransactionEditAction.ShowTimeSelection)
                            },
                            modifier = Modifier.weight(1f),
                            leadingIcon = Icons.Outlined.AccessTime,
                        )
                    }
                    if (state.isScheduleChanged) {
                        EmText(
                            text = stringResource(
                                R.string.recurring_schedule_change_hint,
                                state.nextDueDate.toCompleteDateWithDate(),
                            ),
                            role = EmTextRole.Supporting,
                            color = EmTextColor.Primary,
                        )
                    }
                }
            }
        }

        if (state.transactionType != TransactionType.TRANSFER) {
            state.selectedCategory?.let { category ->
                EmSection(title = stringResource(R.string.select_category)) {
                    CategoryItem(
                        name = category.displayName(),
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
    EmSection(title = stringResource(title)) {
        AccountItem(
            name = account.name,
            icon = account.storedIcon.name,
            iconBackgroundColor = account.storedIcon.backgroundColor,
            amount = account.amount.amountString,
            amountTone = account.amountTone,
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
        horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
    ) {
        EmIcon(
            icon = Icons.Outlined.Info,
            contentDescription = null,
            size = EmTheme.icons.md,
            tint = EmTheme.colors.scheme.onSurfaceVariant,
        )
        EmText(
            text = stringResource(R.string.recurring_edit_future_only),
            role = EmTextRole.Supporting,
            color = EmTextColor.Supporting,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FrequencySelector(
    selected: RecurringFrequency,
    onSelect: (RecurringFrequency) -> Unit,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm)) {
        RecurringFrequency.entries.forEach { frequency ->
            EmChip(
                label = stringResource(id = frequency.labelResId),
                selected = selected == frequency,
                onClick = { onSelect(frequency) },
                leadingIcon = if (selected == frequency) Icons.Outlined.Repeat else null,
            )
        }
    }
}

private fun TransactionType.labelRes(): Int = when (this) {
    TransactionType.INCOME -> R.string.income
    TransactionType.EXPENSE -> R.string.expense
    TransactionType.TRANSFER -> R.string.transfer
}
