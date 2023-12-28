package com.naveenapps.expensemanager.feature.transaction.create

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notes
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naveenapps.expensemanager.core.common.utils.toCompleteDateWithDate
import com.naveenapps.expensemanager.core.common.utils.toTimeAndMinutes
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppDatePickerDialog
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppDialog
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppTimePickerDialog
import com.naveenapps.expensemanager.core.designsystem.ui.components.ClickableTextField
import com.naveenapps.expensemanager.core.designsystem.ui.components.DecimalTextField
import com.naveenapps.expensemanager.core.designsystem.ui.components.TopNavigationBarWithDeleteAction
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.designsystem.ui.utils.ItemSpecModifier
import com.naveenapps.expensemanager.core.designsystem.ui.utils.UiText
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.ReminderTimeState
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.feature.account.list.AccountItem
import com.naveenapps.expensemanager.feature.account.selection.AccountSelectionScreen
import com.naveenapps.expensemanager.feature.category.list.CategoryItem
import com.naveenapps.expensemanager.feature.category.selection.CategorySelectionScreen
import com.naveenapps.expensemanager.feature.transaction.R
import com.naveenapps.expensemanager.feature.transaction.numberpad.NumberPadDialogView
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TransactionCreateScreen() {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    var showBottomSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }

    val viewModel: TransactionCreateViewModel = hiltViewModel()

    var sheetSelection by remember { mutableIntStateOf(1) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showNumberPadDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AppDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            onConfirmation = {
                showDeleteDialog = false
                viewModel.deleteTransaction()
            },
            dialogTitle = stringResource(id = R.string.delete),
            dialogText = stringResource(id = R.string.delete_item_message),
            positiveButtonText = stringResource(id = R.string.delete),
            negativeButtonText = stringResource(id = R.string.cancel),
        )
    }

    if (showNumberPadDialog) {
        NumberPadDialogView(
            onConfirm = { amount ->
                showNumberPadDialog = false
                amount ?: return@NumberPadDialogView
                scope.launch {
                    viewModel.setAmount(amount)
                    showBottomSheet = false
                }
            },
        )
    }

    val showDelete by viewModel.showDelete.collectAsState()

    val message by viewModel.message.collectAsState(null)
    if (message != null) {
        LaunchedEffect(key1 = "completed", block = {
            snackbarHostState.showSnackbar(message = message?.asString(context) ?: "")
        })
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    showBottomSheet = false
                    bottomSheetState.hide()
                }
            },
            sheetState = bottomSheetState,
            windowInsets = WindowInsets(0.dp),
            containerColor = MaterialTheme.colorScheme.background,
            tonalElevation = 0.dp,
        ) {
            TransactionCreateBottomSheetContent(
                sheetSelection,
                viewModel,
            ) {
                scope.launch {
                    showBottomSheet = false
                    bottomSheetState.hide()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopNavigationBarWithDeleteAction(
                title = stringResource(id = R.string.transaction),
                showDelete = showDelete,
            ) {
                if (it == 1) {
                    viewModel.closePage()
                } else {
                    showDeleteDialog = true
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::doSave) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = "",
                )
            }
        },
    ) { innerPadding ->

        val amount by viewModel.amount.collectAsState()
        val amountErrorMessage by viewModel.amountErrorMessage.collectAsState()

        val currencyIcon by viewModel.currencyIcon.collectAsState()
        val selectedDate by viewModel.date.collectAsState()
        val selectedTransactionType by viewModel.selectedTransactionType.collectAsState()
        val notes by viewModel.notes.collectAsState()

        val category by viewModel.selectedCategory.collectAsState()
        val selectedFromAccount by viewModel.selectedFromAccount.collectAsState()
        val selectedToAccount by viewModel.selectedToAccount.collectAsState()

        TransactionCreateScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            selectedCategory = category,
            selectedFromAccount = selectedFromAccount,
            selectedToAccount = selectedToAccount,
            currencyIcon = currencyIcon,
            amount = amount,
            amountErrorMessage = amountErrorMessage,
            amountChange = viewModel::setAmount,
            selectedDate = selectedDate,
            onDateChange = viewModel::setDate,
            selectedTransactionType = selectedTransactionType,
            onTransactionTypeChange = viewModel::setTransactionType,
            notes = notes,
            onNotesChange = viewModel::setNotes,
            openSelection = { type ->
                if (type == 4) {
                    showNumberPadDialog = true
                } else {
                    sheetSelection = type
                    showBottomSheet = true
                }
            },
        )
    }
}

@Composable
private fun TransactionCreateBottomSheetContent(
    sheetSelection: Int,
    viewModel: TransactionCreateViewModel,
    hideBottomSheet: () -> Unit,
) {
    if (sheetSelection == 1) {
        val categories by viewModel.categories.collectAsState()
        val selectedCategory by viewModel.selectedCategory.collectAsState()
        CategorySelectionScreen(
            categories = categories,
            selectedCategory = selectedCategory,
            createNewCallback = {
                viewModel.openCategoryCreate()
                hideBottomSheet.invoke()
            },
        ) { category ->
            viewModel.setCategorySelection(category)
            hideBottomSheet.invoke()
        }
    } else {
        val accounts by viewModel.accounts.collectAsState()
        val selectedFromAccount by viewModel.selectedFromAccount.collectAsState()
        val selectedToAccount by viewModel.selectedToAccount.collectAsState()
        AccountSelectionScreen(
            accounts = accounts,
            selectedAccount = if (sheetSelection == 2) {
                selectedFromAccount
            } else {
                selectedToAccount
            },
            createNewCallback = {
                viewModel.openAccountCreate()
                hideBottomSheet.invoke()
            },
        ) { account ->
            viewModel.setAccountSelection(sheetSelection, account)
            hideBottomSheet.invoke()
        }
    }
}

@Composable
private fun TransactionCreateScreen(
    modifier: Modifier = Modifier,
    amount: String = "",
    amountErrorMessage: UiText? = null,
    amountChange: ((String) -> Unit)? = null,
    currencyIcon: String? = null,
    selectedDate: Date? = null,
    onDateChange: ((Date) -> Unit)? = null,
    selectedTransactionType: TransactionType = TransactionType.EXPENSE,
    onTransactionTypeChange: ((TransactionType) -> Unit)? = null,
    selectedCategory: Category,
    selectedFromAccount: AccountUiModel,
    selectedToAccount: AccountUiModel,
    notes: String? = null,
    onNotesChange: ((String) -> Unit)? = null,
    openSelection: ((Int) -> Unit)? = null,
) {
    val focusManager = LocalFocusManager.current

    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        AppDatePickerDialog(
            modifier = Modifier
                .wrapContentSize()
                .padding(16.dp),
            selectedDate = selectedDate ?: Date(),
            onDateSelected = {
                onDateChange?.invoke(it)
                showDatePicker = false
            },
            onDismiss = {
                showDatePicker = false
            },
        )
    }
    var showTimePicker by remember { mutableStateOf(false) }

    if (showTimePicker) {
        val reminder = (selectedDate ?: Date()).toTime()
        AppTimePickerDialog(
            reminderTimeState = Triple(reminder.hour, reminder.minute, reminder.is24Hour),
            onTimeSelected = {
                val reminderTimeState = ReminderTimeState(it.first, it.second, it.third)
                onDateChange?.invoke((selectedDate ?: Date()).toTime(reminderTimeState))
                showTimePicker = false
            },
        ) {
            showTimePicker = false
        }
    }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
    ) {
        TransactionTypeSelectionView(
            modifier = Modifier
                .wrapContentSize()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                .align(Alignment.CenterHorizontally),
            selectedTransactionType = selectedTransactionType,
            onTransactionTypeChange = onTransactionTypeChange ?: {},
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
                value = selectedDate?.toCompleteDateWithDate() ?: "",
                label = R.string.select_date,
                leadingIcon = Icons.Outlined.EditCalendar,
                onClick = {
                    focusManager.clearFocus(force = true)
                    showDatePicker = true
                },
            )
            ClickableTextField(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                value = selectedDate?.toTimeAndMinutes() ?: "",
                label = R.string.select_time,
                leadingIcon = Icons.Outlined.AccessTime,
                onClick = {
                    focusManager.clearFocus(force = true)
                    showTimePicker = true
                },
            )
        }

        DecimalTextField(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                .fillMaxWidth(),
            value = amount,
            errorMessage = amountErrorMessage,
            onValueChange = amountChange,
            leadingIconText = currencyIcon,
            label = R.string.amount,
            trailingIcon = {
                IconButton(
                    onClick = {
                        openSelection?.invoke(4)
                    },
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Calculate,
                        contentDescription = "",
                    )
                }
            },
        )

        if (selectedTransactionType != TransactionType.TRANSFER) {
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
                name = selectedCategory.name,
                icon = selectedCategory.storedIcon.name,
                iconBackgroundColor = selectedCategory.storedIcon.backgroundColor,
                endIcon = Icons.Filled.KeyboardArrowRight,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        focusManager.clearFocus(force = true)
                        openSelection?.invoke(1)
                    }
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            )
        }

        Text(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                .fillMaxWidth(),
            text = stringResource(
                id = if (selectedTransactionType == TransactionType.TRANSFER) {
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
            name = selectedFromAccount.name,
            icon = selectedFromAccount.storedIcon.name,
            iconBackgroundColor = selectedFromAccount.storedIcon.backgroundColor,
            endIcon = Icons.Filled.KeyboardArrowRight,
            amount = selectedFromAccount.amount.amountString,
            amountTextColor = selectedFromAccount.amountTextColor,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    focusManager.clearFocus(force = true)
                    openSelection?.invoke(2)
                }
                .then(ItemSpecModifier),
        )
        if (selectedTransactionType == TransactionType.TRANSFER) {
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
                name = selectedToAccount.name,
                icon = selectedToAccount.storedIcon.name,
                iconBackgroundColor = selectedToAccount.storedIcon.backgroundColor,
                endIcon = Icons.Filled.KeyboardArrowRight,
                amount = selectedToAccount.amount.amountString,
                amountTextColor = selectedFromAccount.amountTextColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        focusManager.clearFocus(force = true)
                        openSelection?.invoke(3)
                    }
                    .padding(16.dp),
            )
        }

        OutlinedTextField(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 12.dp)
                .fillMaxWidth(),
            value = notes ?: "",
            singleLine = true,
            leadingIcon =
            {
                Icon(
                    imageVector = Icons.Filled.Notes,
                    contentDescription = "",
                )
            },
            label = {
                Text(text = stringResource(id = R.string.notes))
            },
            onValueChange = {
                onNotesChange?.invoke(it)
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
    ExpenseManagerTheme {
        TransactionCreateScreen(
            currencyIcon = "$",
            selectedCategory = Category(
                id = "1",
                name = "Shopping",
                type = CategoryType.EXPENSE,
                StoredIcon(
                    name = "ic_calendar",
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
                    name = "ic_calendar",
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
                    name = "ic_calendar",
                    backgroundColor = "#000000",
                ),
                amountTextColor = com.naveenapps.expensemanager.core.common.R.color.green_500,
                amount = Amount(0.0, "$ 0.00"),
            ),
        )
    }
}
