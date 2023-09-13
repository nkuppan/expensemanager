package com.nkuppan.expensemanager.presentation.transaction.create

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Button
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.core.ui.theme.NavigationButton
import com.nkuppan.expensemanager.core.ui.utils.AppDatePickerDialog
import com.nkuppan.expensemanager.core.ui.utils.AppDialog
import com.nkuppan.expensemanager.core.ui.utils.UiText
import com.nkuppan.expensemanager.presentation.selection.ColorSelectionScreen
import com.nkuppan.expensemanager.presentation.selection.IconAndColorComponent
import com.nkuppan.expensemanager.presentation.selection.IconSelectionScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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

    if (showDeleteDialog) {
        AppDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            onConfirmation = {
                showDeleteDialog = false
            },
            dialogTitle = stringResource(id = R.string.delete),
            dialogText = stringResource(id = R.string.delete_item_message),
            positiveButtonText = stringResource(id = R.string.delete),
            negativeButtonText = stringResource(id = R.string.cancel)
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

        Box(modifier = Modifier.fillMaxSize()) {
            TransactionCreateScreen()
            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                onClick = viewModel::onSaveClick
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
    val context = LocalContext.current

    if (sheetSelection == 1) {
        IconSelectionScreen {
            scope.launch {
                scaffoldState.bottomSheetState.hide()
            }
        }
    } else {
        ColorSelectionScreen {
            scope.launch {
                scaffoldState.bottomSheetState.hide()
            }
        }
    }
}


@Composable
private fun TransactionCreateScreen(
    modifier: Modifier = Modifier,
    name: String = "",
    nameErrorMessage: UiText? = null,
    currentBalance: String = "",
    currentBalanceErrorMessage: UiText? = null,
    currency: Int? = null,
    selectedColor: String = "#000000",
    selectedIcon: String = "ic_calendar",
    openIconPicker: (() -> Unit)? = null,
    openColorPicker: (() -> Unit)? = null,
    onNameChange: ((String) -> Unit)? = null,
    onCurrentBalanceChange: ((String) -> Unit)? = null,
) {

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var showDatePicker by remember {
        mutableStateOf(false)
    }

    var selectedDate by remember {
        mutableStateOf(Date().toString())
    }

    if (showDatePicker) {
        AppDatePickerDialog(
            modifier = Modifier.wrapContentSize(),
            onDateSelected = {
                selectedDate = it
                showDatePicker = false
            },
            onDismiss = {
                showDatePicker = false
            },
        )
    }

    Column(modifier = modifier) {

        Button(onClick = {
            showDatePicker = true
        }) {
            Text(text = "Select Date")
            Text(text = selectedDate)
        }

        OutlinedTextField(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                .fillMaxWidth(),
            value = name,
            singleLine = true,
            label = {
                Text(text = stringResource(id = R.string.amount))
            },
            onValueChange = {
                onNameChange?.invoke(it)
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            isError = nameErrorMessage != null,
            supportingText = if (nameErrorMessage != null) {
                { Text(text = nameErrorMessage.asString(context)) }
            } else {
                null
            }
        )
        val textMeasurer = rememberTextMeasurer()

        OutlinedTextField(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                .fillMaxWidth(),
            value = currentBalance,
            singleLine = true,
            leadingIcon = if (currency != null) {
                {
                    Icon(painter = painterResource(id = currency), contentDescription = "")
                }
            } else {
                null
            },
            label = {
                Text(text = stringResource(id = R.string.current_balance))
            },
            onValueChange = {
                onCurrentBalanceChange?.invoke(it)
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus(force = true)
                }
            ),
            isError = currentBalanceErrorMessage != null,
            supportingText = if (currentBalanceErrorMessage != null) {
                { Text(text = currentBalanceErrorMessage.asString(context)) }
            } else {
                null
            }
        )

        IconAndColorComponent(
            selectedColor,
            selectedIcon,
            openColorPicker,
            openIconPicker
        )
    }
}

@Preview
@Composable
private fun TransactionCreateStatePreview() {
    MaterialTheme {
        TransactionCreateScreen(
            currency = R.drawable.currency_dollar
        )
    }
}