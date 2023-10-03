package com.nkuppan.expensemanager.presentation.budget.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.common.ui.theme.ExpenseManagerTheme
import com.nkuppan.expensemanager.common.ui.theme.widget.AppDialog
import com.nkuppan.expensemanager.common.ui.theme.widget.ClickableTextField
import com.nkuppan.expensemanager.common.ui.theme.widget.DecimalTextField
import com.nkuppan.expensemanager.common.ui.theme.widget.MonthPicker
import com.nkuppan.expensemanager.common.ui.theme.widget.StringTextField
import com.nkuppan.expensemanager.common.ui.theme.widget.TopNavigationBarWithDeleteAction
import com.nkuppan.expensemanager.common.ui.utils.UiText
import com.nkuppan.expensemanager.data.utils.toTransactionMonth
import com.nkuppan.expensemanager.data.utils.toTransactionMonthValue
import com.nkuppan.expensemanager.data.utils.toTransactionYearValue
import com.nkuppan.expensemanager.presentation.selection.ColorSelectionScreen
import com.nkuppan.expensemanager.presentation.selection.IconAndColorComponent
import com.nkuppan.expensemanager.presentation.selection.IconSelectionScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetCreateScreen(navController: NavController, budgetId: String?) {

    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    val scaffoldState = rememberBottomSheetScaffoldState(
        rememberStandardBottomSheetState(
            skipHiddenState = false,
            initialValue = SheetValue.Hidden
        )
    )

    val viewModel: BudgetCreateViewModel = hiltViewModel()

    var sheetSelection by remember { mutableIntStateOf(1) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AppDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            onConfirmation = {
                viewModel.deleteBudget()
                showDeleteDialog = false
            },
            dialogTitle = stringResource(id = R.string.delete),
            dialogText = stringResource(id = R.string.delete_item_message),
            positiveButtonText = stringResource(id = R.string.delete),
            negativeButtonText = stringResource(id = R.string.cancel)
        )
    }

    val budgetCreated by viewModel.budgetUpdated.collectAsState(false)
    if (budgetCreated) {
        LaunchedEffect(key1 = "completed", block = {
            navController.popBackStack()
            scaffoldState.snackbarHostState.showSnackbar(
                message = context.getString(R.string.budget_create_success)
            )
        })
    }

    val errorMessage by viewModel.errorMessage.collectAsState(null)
    if (errorMessage != null) {
        LaunchedEffect(key1 = "errorMessage", block = {
            scaffoldState.snackbarHostState.showSnackbar(
                message = errorMessage!!.asString(context)
            )
        })
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            BudgetCreateBottomSheetContent(
                sheetSelection,
                scope,
                viewModel,
                scaffoldState
            )
        }, topBar = {
            TopNavigationBarWithDeleteAction(
                navController = navController,
                title = stringResource(id = R.string.budgets),
                actionId = budgetId
            ) {
                showDeleteDialog = true
            }
        }
    ) { innerPadding ->

        val name by viewModel.name.collectAsState()
        val nameErrorMessage by viewModel.nameErrorMessage.collectAsState()
        val amount by viewModel.amount.collectAsState()
        val amountErrorMessage by viewModel.amountErrorMessage.collectAsState()
        val currencyIcon by viewModel.currencyIcon.collectAsState()
        val colorValue by viewModel.colorValue.collectAsState()
        val iconValue by viewModel.icon.collectAsState()
        val selectedDate by viewModel.date.collectAsState()

        Box(modifier = Modifier.fillMaxSize()) {
            BudgetCreateScreen(
                modifier = Modifier.padding(innerPadding),
                selectedColor = colorValue,
                selectedIcon = iconValue,
                name = name,
                nameErrorMessage = nameErrorMessage,
                amount = amount,
                amountErrorMessage = amountErrorMessage,
                selectedDate = selectedDate,
                currency = currencyIcon,
                onNameChange = viewModel::setNameChange,
                onAmountChange = viewModel::setAmountChange,
                onDateChange = viewModel::setDate,
                openColorPicker = {
                    scope.launch {
                        if (sheetSelection != 2) {
                            sheetSelection = 2
                            scaffoldState.bottomSheetState.expand()
                        } else {
                            if (scaffoldState.bottomSheetState.isVisible) {
                                scaffoldState.bottomSheetState.hide()
                            } else {
                                scaffoldState.bottomSheetState.expand()
                            }
                        }
                    }
                },
                openIconPicker = {
                    scope.launch {
                        if (sheetSelection != 1) {
                            sheetSelection = 1
                            scaffoldState.bottomSheetState.expand()
                        } else {
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
                onClick = viewModel::saveOrUpdateBudget
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
private fun BudgetCreateBottomSheetContent(
    sheetSelection: Int,
    scope: CoroutineScope,
    viewModel: BudgetCreateViewModel,
    scaffoldState: BottomSheetScaffoldState
) {
    val context = LocalContext.current

    if (sheetSelection == 1) {
        IconSelectionScreen {
            scope.launch {
                viewModel.setIcon(context.resources.getResourceName(it))
                scaffoldState.bottomSheetState.hide()
            }
        }
    } else {
        ColorSelectionScreen {
            scope.launch {
                viewModel.setColorValue(it)
                scaffoldState.bottomSheetState.hide()
            }
        }
    }
}


@Composable
private fun BudgetCreateScreen(
    modifier: Modifier = Modifier,
    name: String = "",
    nameErrorMessage: UiText? = null,
    amount: String = "",
    amountErrorMessage: UiText? = null,
    currency: Int? = null,
    selectedColor: String = "#000000",
    selectedIcon: String = "savings",
    selectedDate: Date? = null,
    openIconPicker: (() -> Unit)? = null,
    openColorPicker: (() -> Unit)? = null,
    onNameChange: ((String) -> Unit)? = null,
    onAmountChange: ((String) -> Unit)? = null,
    onDateChange: ((Date) -> Unit)? = null,
) {
    val focusManager = LocalFocusManager.current

    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        MonthPicker(
            modifier = Modifier
                .wrapContentSize()
                .padding(16.dp)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(8.dp)
                ),
            currentMonth = (selectedDate ?: Date()).toTransactionMonthValue(),
            currentYear = (selectedDate ?: Date()).toTransactionYearValue(),
            confirmButtonCLicked = { month, year ->
                SimpleDateFormat("MM-yyyy", Locale.getDefault()).parse("${month}-${year}")?.let {
                    onDateChange?.invoke(
                        it
                    )
                }
                showDatePicker = false
            },
            cancelClicked = {
                showDatePicker = false
            },
        )
    }

    Column(modifier = modifier) {

        ClickableTextField(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                .fillMaxWidth(),
            value = selectedDate?.toTransactionMonth() ?: "",
            label = R.string.select_date,
            leadingIcon = R.drawable.ic_calendar,
            onClick = {
                focusManager.clearFocus(force = true)
                showDatePicker = true
            }
        )

        StringTextField(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                .fillMaxWidth(),
            value = name,
            errorMessage = nameErrorMessage,
            onValueChange = onNameChange,
            label = R.string.budget_name
        )

        IconAndColorComponent(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                .fillMaxWidth(),
            selectedColor = selectedColor,
            selectedIcon = selectedIcon,
            openColorPicker = openColorPicker,
            openIconPicker = openIconPicker
        )

        DecimalTextField(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                .fillMaxWidth(),
            value = amount,
            errorMessage = amountErrorMessage,
            onValueChange = onAmountChange,
            leadingIcon = currency,
            label = R.string.budget_amount,
        )

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
    }
}

@Preview
@Composable
private fun BudgetCreateStatePreview() {
    ExpenseManagerTheme {
        BudgetCreateScreen(
            currency = R.drawable.currency_dollar
        )
    }
}