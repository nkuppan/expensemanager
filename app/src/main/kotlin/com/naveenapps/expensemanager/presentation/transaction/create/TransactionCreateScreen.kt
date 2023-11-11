package com.naveenapps.expensemanager.presentation.transaction.create

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.naveenapps.expensemanager.R
import com.naveenapps.expensemanager.core.common.utils.toCompleteDate
import com.naveenapps.expensemanager.core.common.utils.toTimeAndMinutes
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppDatePickerDialog
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppDialog
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppTimePickerDialog
import com.naveenapps.expensemanager.core.designsystem.ui.components.ClickableTextField
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
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.presentation.account.list.AccountItem
import com.naveenapps.expensemanager.presentation.account.selection.AccountSelectionScreen
import com.naveenapps.expensemanager.presentation.category.list.CategoryItem
import com.naveenapps.expensemanager.presentation.category.selection.CategorySelectionScreen
import com.naveenapps.expensemanager.presentation.transaction.numberpad.NumberPadDialogView
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TransactionCreateScreen(
    navController: NavController,
    transactionId: String?,
) {

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
            negativeButtonText = stringResource(id = R.string.cancel)
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
            }
        )
    }

    val transactionCreated by viewModel.transactionCreated.collectAsState(false)
    if (transactionCreated) {
        LaunchedEffect(key1 = "completed", block = {
            navController.popBackStack()
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.transaction_create_success)
            )
        })
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
            },
            sheetState = bottomSheetState,
            windowInsets = WindowInsets(0.dp)
        ) {

            TransactionCreateBottomSheetContent(
                sheetSelection,
                viewModel,
            ) {
                showBottomSheet = false
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopNavigationBarWithDeleteAction(
                navController = navController,
                title = stringResource(id = R.string.transaction),
                actionId = transactionId
            ) {
                showDeleteDialog = true
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::doSave) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_done),
                    contentDescription = ""
                )
            }
        }
    ) { innerPadding ->

        val amount by viewModel.amount.collectAsState()
        val amountErrorMessage by viewModel.amountErrorMessage.collectAsState()

        val currency by viewModel.currencyIcon.collectAsState()
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
            currency = currency,
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
            }
        )
    }
}


@Composable
private fun TransactionCreateBottomSheetContent(
    sheetSelection: Int,
    viewModel: TransactionCreateViewModel,
    hideBottomSheet: () -> Unit
) {
    if (sheetSelection == 1) {
        val categories by viewModel.categories.collectAsState()
        val selectedCategory by viewModel.selectedCategory.collectAsState()
        CategorySelectionScreen(
            categories = categories,
            selectedCategory = selectedCategory,
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
    currency: Int? = null,
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

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var showDatePicker by remember {
        mutableStateOf(false)
    }

    if (showDatePicker) {
        AppDatePickerDialog(
            modifier = Modifier
                .wrapContentSize()
                .padding(16.dp)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(8.dp)
                ),
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
    var showTimePicker by remember {
        mutableStateOf(false)
    }

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
        modifier = modifier
            .verticalScroll(rememberScrollState())
    ) {
        TransactionTypeSelectionView(
            modifier = Modifier
                .wrapContentSize()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                .align(Alignment.CenterHorizontally),
            selectedTransactionType = selectedTransactionType,
            onTransactionTypeChange = onTransactionTypeChange ?: {}
        )
        Row(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                .fillMaxWidth()
        ) {
            ClickableTextField(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                value = selectedDate?.toCompleteDate() ?: "",
                label = R.string.select_date,
                leadingIcon = R.drawable.ic_calendar,
                onClick = {
                    focusManager.clearFocus(force = true)
                    showDatePicker = true
                }
            )
            ClickableTextField(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                value = selectedDate?.toTimeAndMinutes() ?: "",
                label = R.string.select_time,
                leadingIcon = R.drawable.ic_time,
                onClick = {
                    focusManager.clearFocus(force = true)
                    showTimePicker = true
                }
            )
        }

        OutlinedTextField(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                .fillMaxWidth(),
            value = amount,
            singleLine = true,
            leadingIcon = if (currency != null) {
                {
                    Icon(painter = painterResource(id = currency), contentDescription = "")
                }
            } else {
                null
            },
            trailingIcon =
            {
                IconButton(
                    onClick = {
                        openSelection?.invoke(4)
                    },
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_calculater),
                        contentDescription = ""
                    )
                }
            },
            label = {
                Text(text = stringResource(id = com.naveenapps.expensemanager.core.data.R.string.amount))
            },
            onValueChange = {
                amountChange?.invoke(it)
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus(force = true)
                }
            ),
            isError = amountErrorMessage != null,
            supportingText = if (amountErrorMessage != null) {
                { Text(text = amountErrorMessage.asString(context)) }
            } else {
                null
            }
        )
        if (selectedTransactionType != TransactionType.TRANSFER) {
            Text(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                    .fillMaxWidth(),
                text = stringResource(id = R.string.select_category),
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Normal,
                color = colorResource(id = R.color.blue_500)
            )
            CategoryItem(
                name = selectedCategory.name,
                icon = selectedCategory.iconName,
                iconBackgroundColor = selectedCategory.iconBackgroundColor,
                endIcon = R.drawable.ic_arrow_right,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        focusManager.clearFocus(force = true)
                        openSelection?.invoke(1)
                    }
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
            )
        }

        Text(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                .fillMaxWidth(),
            text = stringResource(
                id = if (selectedTransactionType == TransactionType.TRANSFER) {
                    com.naveenapps.expensemanager.core.data.R.string.from_account
                } else {
                    R.string.select_account
                }
            ),
            fontWeight = FontWeight.SemiBold,
            fontStyle = FontStyle.Normal,
            color = colorResource(id = R.color.blue_500)
        )

        AccountItem(
            name = selectedFromAccount.name,
            icon = selectedFromAccount.icon,
            iconBackgroundColor = selectedFromAccount.iconBackgroundColor,
            endIcon = R.drawable.ic_arrow_right,
            amount = selectedFromAccount.amount.amountString,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    focusManager.clearFocus(force = true)
                    openSelection?.invoke(2)
                }
                .then(ItemSpecModifier)
        )
        if (selectedTransactionType == TransactionType.TRANSFER) {

            Text(
                modifier = Modifier
                    .then(ItemSpecModifier)
                    .fillMaxWidth(),
                text = stringResource(id = com.naveenapps.expensemanager.core.data.R.string.to_account),
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Normal,
                color = colorResource(id = R.color.blue_500)
            )

            AccountItem(
                name = selectedToAccount.name,
                icon = selectedToAccount.icon,
                iconBackgroundColor = selectedToAccount.iconBackgroundColor,
                endIcon = R.drawable.ic_arrow_right,
                amount = selectedToAccount.amount.amountString,
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
                    painter = painterResource(id = R.drawable.ic_notes),
                    contentDescription = ""
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
                }
            ),
            supportingText = {
                Text(text = stringResource(id = R.string.optional_details))
            }
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(36.dp)
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
        is24Hour = false
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
            currency = com.naveenapps.expensemanager.core.data.R.drawable.currency_dollar,
            selectedCategory = Category(
                id = "1",
                name = "Shopping",
                type = CategoryType.EXPENSE,
                iconName = "ic_calendar",
                iconBackgroundColor = "#000000",
                createdOn = Date(),
                updatedOn = Date()
            ),
            selectedFromAccount = AccountUiModel(
                id = "1",
                name = "Shopping",
                type = AccountType.REGULAR,
                icon = "ic_calendar",
                iconBackgroundColor = "#000000",
                amountTextColor = R.color.red_500,
                amount = Amount(0.0, "$ 0.00"),
            ),
            selectedToAccount = AccountUiModel(
                id = "1",
                name = "Shopping",
                type = AccountType.REGULAR,
                icon = "ic_calendar",
                iconBackgroundColor = "#000000",
                amountTextColor = R.color.green_500,
                amount = Amount(0.0, "$ 0.00"),
            )
        )
    }
}