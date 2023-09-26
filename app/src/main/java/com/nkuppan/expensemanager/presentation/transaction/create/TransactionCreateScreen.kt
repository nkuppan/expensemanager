package com.nkuppan.expensemanager.presentation.transaction.create

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
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
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.core.ui.theme.ExpenseManagerTheme
import com.nkuppan.expensemanager.core.ui.theme.NavigationButton
import com.nkuppan.expensemanager.core.ui.theme.widget.ClickableTextField
import com.nkuppan.expensemanager.core.ui.utils.AppDatePickerDialog
import com.nkuppan.expensemanager.core.ui.utils.AppDialog
import com.nkuppan.expensemanager.core.ui.utils.AppTimePickerDialog
import com.nkuppan.expensemanager.core.ui.utils.UiText
import com.nkuppan.expensemanager.data.utils.toTransactionDate
import com.nkuppan.expensemanager.data.utils.toTransactionTimeOnly
import com.nkuppan.expensemanager.domain.model.AccountType
import com.nkuppan.expensemanager.domain.model.Category
import com.nkuppan.expensemanager.domain.model.CategoryType
import com.nkuppan.expensemanager.domain.model.ReminderTimeState
import com.nkuppan.expensemanager.domain.model.TransactionType
import com.nkuppan.expensemanager.presentation.account.list.AccountItem
import com.nkuppan.expensemanager.presentation.account.list.AccountUiModel
import com.nkuppan.expensemanager.presentation.account.selection.AccountSelectionScreen
import com.nkuppan.expensemanager.presentation.category.list.CategoryItem
import com.nkuppan.expensemanager.presentation.category.selection.CategorySelectionScreen
import com.nkuppan.expensemanager.presentation.transaction.numberpad.NumberPadDialogView
import kotlinx.coroutines.CoroutineScope
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

    val scaffoldState = rememberBottomSheetScaffoldState(
        rememberStandardBottomSheetState(
            skipHiddenState = false,
            initialValue = SheetValue.Hidden
        )
    )

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
                    scaffoldState.bottomSheetState.hide()
                }
            }
        )
    }

    val transactionCreated by viewModel.transactionCreated.collectAsState(false)
    if (transactionCreated) {
        LaunchedEffect(key1 = "completed", block = {
            navController.popBackStack()
            scaffoldState.snackbarHostState.showSnackbar(
                message = context.getString(R.string.transaction_create_success)
            )
        })
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            TransactionCreateBottomSheetContent(
                sheetSelection,
                scope,
                viewModel,
                scaffoldState
            )
        }, topBar = {
            TransactionCreateTopActionBar(
                navController,
                transactionId
            ) {
                showDeleteDialog = true
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

        Box(modifier = Modifier.fillMaxSize()) {
            TransactionCreateScreen(
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
                        scope.launch {
                            if (scaffoldState.bottomSheetState.isVisible) {
                                scaffoldState.bottomSheetState.hide()
                            } else {
                                scaffoldState.bottomSheetState.expand()
                            }
                        }
                    }
                }
            )
            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                onClick = viewModel::doSave
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_done),
                    contentDescription = ""
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TransactionCreateTopActionBar(
    navController: NavController,
    transactionId: String?,
    onClick: () -> Unit,
) {
    TopAppBar(navigationIcon = {
        NavigationButton(
            navController,
            navigationIcon = R.drawable.ic_close
        )
    }, title = {
        Row {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically),
                text = stringResource(R.string.transaction)
            )
            if (transactionId?.isNotBlank() == true) {
                IconButton(onClick = onClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete),
                        contentDescription = ""
                    )
                }
            }
        }
    })
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TransactionCreateBottomSheetContent(
    sheetSelection: Int,
    scope: CoroutineScope,
    viewModel: TransactionCreateViewModel,
    scaffoldState: BottomSheetScaffoldState
) {
    if (sheetSelection == 1) {
        val categories by viewModel.categories.collectAsState()
        val selectedCategory by viewModel.selectedCategory.collectAsState()
        CategorySelectionScreen(
            categories = categories,
            selectedCategory = selectedCategory,
        ) { category ->
            scope.launch {
                viewModel.setCategorySelection(category)
                scaffoldState.bottomSheetState.hide()
            }
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
            scope.launch {
                viewModel.setAccountSelection(sheetSelection, account)
                scaffoldState.bottomSheetState.hide()
            }
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
        AppTimePickerDialog(
            modifier = Modifier
                .wrapContentSize()
                .padding(16.dp)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(8.dp)
                ),
            reminderTimeState = (selectedDate ?: Date()).toTime(),
            onTimeSelected = {
                onDateChange?.invoke((selectedDate ?: Date()).toTime(it))
                showTimePicker = false
            },
            onDismiss = {
                showTimePicker = false
            },
        )
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
                value = selectedDate?.toTransactionDate() ?: "",
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
                value = selectedDate?.toTransactionTimeOnly() ?: "",
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
                Text(text = stringResource(id = R.string.amount))
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
                    .background(color = colorResource(id = R.color.grey_light))
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
                    R.string.from_account
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
            amount = selectedFromAccount.amount.asString(context),
            modifier = Modifier
                .fillMaxWidth()
                .background(color = colorResource(id = R.color.grey_light))
                .clickable {
                    focusManager.clearFocus(force = true)
                    openSelection?.invoke(2)
                }
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
        )
        if (selectedTransactionType == TransactionType.TRANSFER) {

            Text(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                    .fillMaxWidth(),
                text = stringResource(id = R.string.to_account),
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Normal,
                color = colorResource(id = R.color.blue_500)
            )

            AccountItem(
                name = selectedToAccount.name,
                icon = selectedToAccount.icon,
                iconBackgroundColor = selectedToAccount.iconBackgroundColor,
                endIcon = R.drawable.ic_arrow_right,
                amount = selectedToAccount.amount.asString(context),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = colorResource(id = R.color.grey_light))
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
            currency = R.drawable.currency_dollar,
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
                amount = UiText.DynamicString("$ 0.00"),
            ),
            selectedToAccount = AccountUiModel(
                id = "1",
                name = "Shopping",
                type = AccountType.REGULAR,
                icon = "ic_calendar",
                iconBackgroundColor = "#000000",
                amount = UiText.DynamicString("$ 0.00"),
            )
        )
    }
}