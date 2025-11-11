package com.naveenapps.expensemanager.feature.transaction.create

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.common.utils.toCompleteDateWithDate
import com.naveenapps.expensemanager.core.common.utils.toTimeAndMinutes
import com.naveenapps.expensemanager.core.designsystem.components.DeleteDialogItem
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppDatePickerDialog
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppTimePickerDialog
import com.naveenapps.expensemanager.core.designsystem.ui.components.ClickableTextField
import com.naveenapps.expensemanager.core.designsystem.ui.components.DecimalTextField
import com.naveenapps.expensemanager.core.designsystem.ui.components.TopNavigationBarWithDeleteAction
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.designsystem.ui.utils.ItemSpecModifier
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
import com.naveenapps.expensemanager.feature.account.list.AccountItem
import com.naveenapps.expensemanager.feature.account.selection.AccountSelectionScreen
import com.naveenapps.expensemanager.feature.category.list.CategoryItem
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
            confirm = {
                onAction.invoke(TransactionCreateAction.Delete)
            },
            dismiss = {
                onAction.invoke(TransactionCreateAction.DismissDeleteDialog)
            }
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
            TopNavigationBarWithDeleteAction(
                title = stringResource(id = R.string.transaction),
                isDeleteEnabled = state.showDeleteButton,
                onNavigationIconClick = {
                    onAction.invoke(TransactionCreateAction.ClosePage)
                },
                onDeleteActionClick = {
                    onAction.invoke(TransactionCreateAction.ShowDeleteDialog)
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onAction.invoke(TransactionCreateAction.Save)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = "",
                )
            }
        },
    ) { innerPadding ->

        TransactionCreateScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            state = state,
            onAction = onAction
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
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
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
            }
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CategorySelectionView(
    state: TransactionCreateState,
    onAction: (TransactionCreateAction) -> Unit
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
            }
        )
    }
}

@Composable
private fun TransactionCreateScreen(
    state: TransactionCreateState,
    onAction: (TransactionCreateAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    if (state.showDateSelection) {
        AppDatePickerDialog(
            selectedDate = state.dateTime,
            onDateSelected = {
                onAction.invoke(TransactionCreateAction.SelectDate(it))
            },
            onDismiss = {
                onAction.invoke(TransactionCreateAction.DismissDateSelection)
            }
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
                        state.dateTime.toTime(
                            reminderTimeState
                        )
                    )
                )
            },
            onDismiss = {
                onAction.invoke(TransactionCreateAction.DismissDateSelection)
            }
        )
    }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
    ) {
        TransactionTypeSelectionView(
            modifier = Modifier
                .wrapContentSize()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                .align(Alignment.CenterHorizontally),
            selectedTransactionType = state.transactionType,
            onTransactionTypeChange = {
                onAction.invoke(TransactionCreateAction.ChangeTransactionType(it))
            },
        )
        Row(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                .fillMaxWidth(),
        ) {
            ClickableTextField(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                value = state.dateTime.toCompleteDateWithDate(),
                label = R.string.select_date,
                leadingIcon = Icons.Outlined.EditCalendar,
                onClick = {
                    focusManager.clearFocus(force = true)
                    onAction.invoke(TransactionCreateAction.ShowDateSelection)
                },
            )
            ClickableTextField(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                value = state.dateTime.toTimeAndMinutes(),
                label = R.string.select_time,
                leadingIcon = Icons.Outlined.AccessTime,
                onClick = {
                    onAction.invoke(TransactionCreateAction.ShowTimeSelection)
                },
            )
        }

        DecimalTextField(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                .fillMaxWidth(),
            value = state.amount.value,
            isError = state.amount.valueError,
            onValueChange = state.amount.onValueChange,
            leadingIconText = state.currency.symbol,
            label = R.string.amount,
            errorMessage = stringResource(id = R.string.amount_error_message),
            trailingIcon = {
                IconButton(
                    onClick = {
                        onAction.invoke(TransactionCreateAction.ShowNumberPad)
                    },
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Calculate,
                        contentDescription = "",
                    )
                }
            },
        )

        if (state.transactionType != TransactionType.TRANSFER) {
            Text(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                    .fillMaxWidth(),
                text = stringResource(id = R.string.select_category),
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Normal,
                color = colorResource(id = com.naveenapps.expensemanager.core.common.R.color.blue_500),
            )
            CategoryItem(
                name = state.selectedCategory.name,
                icon = state.selectedCategory.storedIcon.name,
                iconBackgroundColor = state.selectedCategory.storedIcon.backgroundColor,
                endIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        focusManager.clearFocus(force = true)
                        onAction.invoke(TransactionCreateAction.ShowCategorySelection)
                    }
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            )
        }

        Text(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                .fillMaxWidth(),
            text = stringResource(
                id = if (state.transactionType == TransactionType.TRANSFER) {
                    R.string.from_account
                } else {
                    R.string.select_account
                },
            ),
            fontWeight = FontWeight.SemiBold,
            fontStyle = FontStyle.Normal,
            color = colorResource(id = com.naveenapps.expensemanager.core.common.R.color.blue_500),
        )

        AccountItem(
            name = state.selectedFromAccount.name,
            icon = state.selectedFromAccount.storedIcon.name,
            iconBackgroundColor = state.selectedFromAccount.storedIcon.backgroundColor,
            endIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            amount = state.selectedFromAccount.amount.amountString,
            amountTextColor = state.selectedFromAccount.amountTextColor,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    focusManager.clearFocus(force = true)
                    onAction.invoke(
                        TransactionCreateAction.ShowAccountSelection(
                            AccountSelection.FROM_ACCOUNT
                        )
                    )
                }
                .then(ItemSpecModifier),
        )
        if (state.transactionType == TransactionType.TRANSFER) {
            Text(
                modifier = Modifier
                    .then(ItemSpecModifier)
                    .fillMaxWidth(),
                text = stringResource(id = R.string.to_account),
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Normal,
                color = colorResource(id = com.naveenapps.expensemanager.core.common.R.color.blue_500),
            )

            AccountItem(
                name = state.selectedToAccount.name,
                icon = state.selectedToAccount.storedIcon.name,
                iconBackgroundColor = state.selectedToAccount.storedIcon.backgroundColor,
                endIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                amount = state.selectedToAccount.amount.amountString,
                amountTextColor = state.selectedFromAccount.amountTextColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        focusManager.clearFocus(force = true)
                        onAction.invoke(
                            TransactionCreateAction.ShowAccountSelection(
                                AccountSelection.TO_ACCOUNT
                            )
                        )
                    }
                    .padding(16.dp),
            )
        }

        OutlinedTextField(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 12.dp)
                .fillMaxWidth(),
            value = state.notes.value,
            singleLine = true,
            leadingIcon =
            {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Notes,
                    contentDescription = "",
                )
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
            supportingText = {
                Text(text = stringResource(id = R.string.optional_details))
            },
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(36.dp),
        )
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

@Preview
@Composable
private fun TransactionCreateStatePreview() {

    val amountField = TextFieldValue(value = "", valueError = false, onValueChange = {})

    ExpenseManagerTheme {
        TransactionCreateScreenContent(
            state = TransactionCreateState(
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
                showDeleteButton = false,
                showDeleteDialog = false,
                showCategorySelection = false,
                showAccountSelection = false,
                showNumberPad = false,
                transactionType = TransactionType.TRANSFER,
                accountSelection = AccountSelection.FROM_ACCOUNT,
                showTimeSelection = false,
                showDateSelection = false
            ),
            onAction = {}
        )
    }
}
