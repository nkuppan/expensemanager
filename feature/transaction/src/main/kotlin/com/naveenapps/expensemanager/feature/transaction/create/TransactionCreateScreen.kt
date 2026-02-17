package com.naveenapps.expensemanager.feature.transaction.create

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.common.utils.toCompleteDateWithDate
import com.naveenapps.expensemanager.core.common.utils.toTimeAndMinutes
import com.naveenapps.expensemanager.core.designsystem.components.DeleteDialogItem
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppDatePickerDialog
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppTimePickerDialog
import com.naveenapps.expensemanager.core.designsystem.ui.components.ClickableTextField
import com.naveenapps.expensemanager.core.designsystem.ui.components.DecimalTextField
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.core.designsystem.ui.components.SettingsSection
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.ReminderTimeState
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.model.TextFieldValue
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.feature.account.selection.AccountItem
import com.naveenapps.expensemanager.feature.account.selection.AccountItemDefaults
import com.naveenapps.expensemanager.feature.account.selection.AccountSelectionScreen
import com.naveenapps.expensemanager.feature.category.selection.CategoryItem
import com.naveenapps.expensemanager.feature.category.selection.CategoryItemDefaults
import com.naveenapps.expensemanager.feature.category.selection.CategorySelectionScreen
import com.naveenapps.expensemanager.feature.transaction.R
import com.naveenapps.expensemanager.feature.transaction.numberpad.NumberPadDialogView
import org.koin.compose.viewmodel.koinViewModel
import java.util.Calendar
import java.util.Date

@Composable
fun TransactionCreateScreen(
    viewModel: TransactionCreateViewModel = koinViewModel()
) {

    val state by viewModel.state.collectAsState()

    TransactionCreateScreenContent(
        state = state,
        onAction = viewModel::processAction
    )
}

@Composable
private fun TransactionCreateScreenContent(
    state: TransactionCreateState,
    onAction: (TransactionCreateAction) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    if (state.showDeleteDialog) {
        DeleteDialogItem(
            confirm = { onAction.invoke(TransactionCreateAction.Delete) },
            dismiss = { onAction.invoke(TransactionCreateAction.DismissDeleteDialog) },
        )
    } else if (state.showNumberPad) {
        NumberPadDialogView(
            onConfirm = { amount ->
                onAction.invoke(TransactionCreateAction.SetNumberPadValue(amount))
            },
        )
    } else if (state.showCategorySelection) {
        CategorySelectionView(state, onAction)
    } else if (state.showAccountSelection) {
        AccountSelectionView(state, onAction)
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            ExpenseManagerTopAppBar(
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationBackClick = {
                    onAction.invoke(TransactionCreateAction.ClosePage)
                },
                title = if (state.showDeleteButton)
                    stringResource(R.string.edit_transaction)
                else
                    stringResource(R.string.create_transaction),
                actions = {
                    if (state.showDeleteButton) {
                        IconButton(onClick = { onAction.invoke(TransactionCreateAction.ShowDeleteDialog) }) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAction.invoke(TransactionCreateAction.Save) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = null,
                    )
                },
                text = {
                    Text(text = stringResource(R.string.save))
                },
            )
        },
    ) { innerPadding ->
        TransactionCreateScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            state = state,
            onAction = onAction,
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AccountSelectionView(
    state: TransactionCreateState,
    onAction: (TransactionCreateAction) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = {
            onAction.invoke(TransactionCreateAction.DismissAccountSelection)
        },
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        AccountSelectionScreen(
            accounts = state.accounts,
            selectedAccount = when (state.accountSelection) {
                AccountSelection.FROM_ACCOUNT -> state.selectedFromAccount
                AccountSelection.TO_ACCOUNT -> state.selectedToAccount
            },
            createNewCallback = {
                onAction.invoke(TransactionCreateAction.OpenAccountCreate(null))
            },
            onItemSelection = {
                onAction.invoke(TransactionCreateAction.SelectAccount(it))
            },
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CategorySelectionView(
    state: TransactionCreateState,
    onAction: (TransactionCreateAction) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = {
            onAction.invoke(TransactionCreateAction.DismissCategorySelection)
        },
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
    ) {
        CategorySelectionScreen(
            categories = state.categories,
            selectedCategory = state.selectedCategory,
            createNewCallback = {
                onAction.invoke(TransactionCreateAction.OpenCategoryCreate(null))
            },
            onItemSelection = {
                onAction.invoke(TransactionCreateAction.SelectCategory(it))
            },
        )
    }
}

@Composable
private fun TransactionCreateScreen(
    state: TransactionCreateState,
    onAction: (TransactionCreateAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.showDateSelection) {
        AppDatePickerDialog(
            selectedDate = state.dateTime,
            onDateSelected = {
                onAction.invoke(TransactionCreateAction.SelectDate(it))
            },
            onDismiss = {
                onAction.invoke(TransactionCreateAction.DismissDateSelection)
            },
        )
    }

    if (state.showTimeSelection) {
        val reminder = state.dateTime.toTime()
        AppTimePickerDialog(
            reminderTimeState = Triple(reminder.hour, reminder.minute, reminder.is24Hour),
            onTimeSelected = {
                val reminderTimeState = ReminderTimeState(it.first, it.second, it.third)
                onAction.invoke(
                    TransactionCreateAction.SelectDate(
                        state.dateTime.toTime(reminderTimeState)
                    ),
                )
            },
            onDismiss = {
                onAction.invoke(TransactionCreateAction.DismissDateSelection)
            },
        )
    }

    TransactionCreateContent(
        modifier = modifier,
        state = state,
        onAction = onAction
    )
}

@Composable
private fun TransactionCreateContent(
    modifier: Modifier,
    state: TransactionCreateState,
    onAction: (TransactionCreateAction) -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsSection(
            title = stringResource(R.string.transaction_type),
            modifier = Modifier.padding(top = 8.dp),
        ) {
            AppCardView {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    TransactionTypeSelectionView(
                        modifier = Modifier.fillMaxWidth(),
                        selectedTransactionType = state.transactionType,
                        onTransactionTypeChange = {
                            onAction.invoke(TransactionCreateAction.ChangeTransactionType(it))
                        },
                    )
                }
            }
        }

        SettingsSection(title = stringResource(R.string.details)) {
            AppCardView {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    DecimalTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.amount.value,
                        isError = state.amount.valueError,
                        onValueChange = state.amount.onValueChange,
                        leadingIconText = state.currency.symbol,
                        label = R.string.amount,
                        errorMessage = stringResource(id = R.string.amount_error_message),
                        trailingIcon = {
                            IconButton(
                                onClick = { onAction.invoke(TransactionCreateAction.ShowNumberPad) },
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Calculate,
                                    contentDescription = null,
                                )
                            }
                        },
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        ClickableTextField(
                            modifier = Modifier.weight(1f),
                            value = state.dateTime.toCompleteDateWithDate(),
                            label = R.string.select_date,
                            leadingIcon = Icons.Outlined.EditCalendar,
                            onClick = {
                                focusManager.clearFocus(force = true)
                                onAction.invoke(TransactionCreateAction.ShowDateSelection)
                            },
                        )
                        ClickableTextField(
                            modifier = Modifier.weight(1f),
                            value = state.dateTime.toTimeAndMinutes(),
                            label = R.string.select_time,
                            leadingIcon = Icons.Outlined.AccessTime,
                            onClick = {
                                onAction.invoke(TransactionCreateAction.ShowTimeSelection)
                            },
                        )
                    }

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.notes.value,
                        singleLine = false,
                        maxLines = 3,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Notes,
                                contentDescription = null,
                            )
                        },
                        placeholder = {
                            Text(text = stringResource(id = R.string.optional_details))
                        },
                        label = {
                            Text(text = stringResource(id = R.string.notes))
                        },
                        onValueChange = {
                            state.notes.onValueChange?.invoke(it)
                        },
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus(force = true)
                            },
                        ),
                    )
                }
            }
        }


        // Category — only for non-transfer
        AnimatedVisibility(
            visible = state.transactionType != TransactionType.TRANSFER,
            enter = fadeIn(tween(200)) + expandVertically(tween(250)),
            exit = fadeOut(tween(150)) + shrinkVertically(tween(200)),
        ) {
            SettingsSection(title = stringResource(R.string.select_category)) {
                CategoryItem(
                    name = state.selectedCategory.name,
                    icon = state.selectedCategory.storedIcon.name,
                    iconBackgroundColor = state.selectedCategory.storedIcon.backgroundColor,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        focusManager.clearFocus(force = true)
                        onAction.invoke(TransactionCreateAction.ShowCategorySelection)
                    },
                    trailingContent = {
                        CategoryItemDefaults.ChevronTrailing()
                    },
                )
            }
        }

        SettingsSection(
            title = stringResource(
                id = if (state.transactionType == TransactionType.TRANSFER)
                    R.string.from_account
                else
                    R.string.select_account,
            )
        ) {
            AccountItem(
                name = state.selectedFromAccount.name,
                icon = state.selectedFromAccount.storedIcon.name,
                iconBackgroundColor = state.selectedFromAccount.storedIcon.backgroundColor,
                amount = state.selectedFromAccount.amount.amountString,
                amountTextColor = state.selectedFromAccount.amountTextColor,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    focusManager.clearFocus(force = true)
                    onAction.invoke(
                        TransactionCreateAction.ShowAccountSelection(AccountSelection.FROM_ACCOUNT),
                    )
                },
                trailingContent = {
                    AccountItemDefaults.ChevronTrailing()
                },
            )
        }

        // To Account — only for transfer
        AnimatedVisibility(
            visible = state.transactionType == TransactionType.TRANSFER,
            enter = fadeIn(tween(200)) + expandVertically(tween(250)),
            exit = fadeOut(tween(150)) + shrinkVertically(tween(200)),
        ) {
            SettingsSection(title = stringResource(R.string.to_account)) {
                AccountItem(
                    name = state.selectedToAccount.name,
                    icon = state.selectedToAccount.storedIcon.name,
                    iconBackgroundColor = state.selectedToAccount.storedIcon.backgroundColor,
                    amount = state.selectedToAccount.amount.amountString,
                    amountTextColor = state.selectedToAccount.amountTextColor,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        focusManager.clearFocus(force = true)
                        onAction.invoke(
                            TransactionCreateAction.ShowAccountSelection(AccountSelection.TO_ACCOUNT),
                        )
                    },
                    trailingContent = {
                        AccountItemDefaults.ChevronTrailing()
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(72.dp))
    }
}

fun Date.toTime(): ReminderTimeState {
    val cal = Calendar.getInstance()
    cal.time = this
    val hours = cal.get(Calendar.HOUR_OF_DAY)
    val minutes = cal.get(Calendar.MINUTE)
    return ReminderTimeState(
        hour = hours,
        minute = minutes,
        is24Hour = false,
    )
}

fun Date.toTime(reminderTimeState: ReminderTimeState): Date {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.set(Calendar.HOUR_OF_DAY, reminderTimeState.hour)
    calendar.set(Calendar.MINUTE, reminderTimeState.minute)
    return calendar.time
}

@AppPreviewsLightAndDarkMode
@Composable
private fun TransactionCreateStateForTransferPreview() {
    val amountField = TextFieldValue(value = "", valueError = false, onValueChange = {})
    NaveenAppsPreviewTheme(padding = 0.dp) {
        TransactionCreateScreenContent(
            state = getTransactionState(amountField, TransactionType.TRANSFER),
            onAction = {},
        )
    }
}

@AppPreviewsLightAndDarkMode
@Composable
private fun TransactionCreateStateForIncomePreview() {
    val amountField = TextFieldValue(value = "", valueError = false, onValueChange = {})
    NaveenAppsPreviewTheme(padding = 0.dp) {
        TransactionCreateScreenContent(
            state = getTransactionState(amountField, TransactionType.INCOME),
            onAction = {},
        )
    }
}

private fun getTransactionState(
    amountField: TextFieldValue<String>,
    transactionType: TransactionType,
): TransactionCreateState =
    TransactionCreateState(
        amount = amountField,
        currency = Currency(symbol = "$", name = ""),
        dateTime = Date(),
        notes = amountField,
        selectedCategory = Category(
            id = "1",
            name = "Shopping",
            type = CategoryType.EXPENSE,
            StoredIcon(
                name = "account_balance_wallet",
                backgroundColor = "#000000",
            ),
            createdOn = Date(),
            updatedOn = Date(),
        ),
        selectedFromAccount = AccountUiModel(
            id = "1",
            name = "Shopping",
            type = AccountType.REGULAR,
            storedIcon = StoredIcon(
                name = "account_balance_wallet",
                backgroundColor = "#000000",
            ),
            amountTextColor = com.naveenapps.expensemanager.core.common.R.color.red_500,
            amount = Amount(0.0, "$ 0.00"),
        ),
        selectedToAccount = AccountUiModel(
            id = "1",
            name = "Shopping",
            type = AccountType.REGULAR,
            storedIcon = StoredIcon(
                name = "account_balance_wallet",
                backgroundColor = "#000000",
            ),
            amountTextColor = com.naveenapps.expensemanager.core.common.R.color.green_500,
            amount = Amount(0.0, "$ 0.00"),
        ),
        accounts = emptyList(),
        categories = emptyList(),
        showDeleteButton = true,
        showDeleteDialog = false,
        showCategorySelection = false,
        showAccountSelection = false,
        showNumberPad = false,
        transactionType = transactionType,
        accountSelection = AccountSelection.FROM_ACCOUNT,
        showTimeSelection = false,
        showDateSelection = false,
    )
