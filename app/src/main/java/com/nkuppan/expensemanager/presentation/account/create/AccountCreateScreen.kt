package com.nkuppan.expensemanager.presentation.account.create

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.core.ui.theme.ExpenseManagerTheme
import com.nkuppan.expensemanager.core.ui.theme.NavigationButton
import com.nkuppan.expensemanager.core.ui.utils.AppDialog
import com.nkuppan.expensemanager.core.ui.utils.UiText
import com.nkuppan.expensemanager.domain.model.AccountType
import com.nkuppan.expensemanager.presentation.selection.ColorSelectionScreen
import com.nkuppan.expensemanager.presentation.selection.IconAndColorComponent
import com.nkuppan.expensemanager.presentation.selection.IconSelectionScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AccountCreateScreen(
    navController: NavController,
    accountId: String?,
) {

    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    val scaffoldState = rememberBottomSheetScaffoldState(
        rememberStandardBottomSheetState(
            skipHiddenState = false,
            initialValue = SheetValue.Hidden
        )
    )

    val viewModel: AccountCreateViewModel = hiltViewModel()

    var sheetSelection by remember { mutableIntStateOf(1) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AppDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            onConfirmation = {
                viewModel.deleteAccount()
                showDeleteDialog = false
            },
            dialogTitle = stringResource(id = R.string.delete),
            dialogText = stringResource(id = R.string.delete_item_message),
            positiveButtonText = stringResource(id = R.string.delete),
            negativeButtonText = stringResource(id = R.string.cancel)
        )
    }

    val accountCreated by viewModel.accountUpdated.collectAsState(false)
    if (accountCreated) {
        LaunchedEffect(key1 = "completed", block = {
            navController.popBackStack()
            scaffoldState.snackbarHostState.showSnackbar(
                message = context.getString(R.string.account_create_success)
            )
        })
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            AccountCreateBottomSheetContent(
                sheetSelection,
                scope,
                viewModel,
                scaffoldState
            )
        }, topBar = {
            AccountCreateTopActionBar(
                navController,
                accountId
            ) {
                showDeleteDialog = true
            }
        }
    ) { innerPadding ->

        val name by viewModel.name.collectAsState()
        val nameErrorMessage by viewModel.nameErrorMessage.collectAsState()
        val currentBalance by viewModel.currentBalance.collectAsState()
        val currentBalanceErrorMessage by viewModel.currentBalanceErrorMessage.collectAsState()
        val currencyIcon by viewModel.currencyIcon.collectAsState()
        val colorValue by viewModel.colorValue.collectAsState()
        val iconValue by viewModel.icon.collectAsState()
        val selectedAccountType by viewModel.accountType.collectAsState()

        Box(modifier = Modifier.fillMaxSize()) {
            AccountCreateScreen(
                modifier = Modifier.padding(innerPadding),
                selectedColor = colorValue,
                selectedIcon = iconValue,
                name = name,
                nameErrorMessage = nameErrorMessage,
                currentBalance = currentBalance,
                currentBalanceErrorMessage = currentBalanceErrorMessage,
                selectedAccountType = selectedAccountType,
                onAccountTypeChange = viewModel::setAccountType,
                currency = currencyIcon,
                onNameChange = {
                    viewModel.setNameChange(it)
                },
                onCurrentBalanceChange = {
                    viewModel.setCurrentBalanceChange(it)
                },
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
                onClick = viewModel::saveOrUpdateAccount
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
private fun AccountCreateTopActionBar(
    navController: NavController,
    accountId: String?,
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
                text = stringResource(R.string.account)
            )
            if (accountId?.isNotBlank() == true) {
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
private fun AccountCreateBottomSheetContent(
    sheetSelection: Int,
    scope: CoroutineScope,
    viewModel: AccountCreateViewModel,
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
private fun AccountCreateScreen(
    onAccountTypeChange: ((AccountType) -> Unit),
    modifier: Modifier = Modifier,
    selectedAccountType: AccountType = AccountType.BANK_ACCOUNT,
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

    Column(modifier = modifier) {

        AccountTypeSelectionView(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            selectedAccountType = selectedAccountType,
            onAccountTypeChange = onAccountTypeChange
        )

        OutlinedTextField(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                .fillMaxWidth(),
            value = name,
            singleLine = true,
            label = {
                Text(text = stringResource(id = R.string.account_name))
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
private fun AccountCreateStatePreview() {
    ExpenseManagerTheme {
        AccountCreateScreen(
            onAccountTypeChange = {

            },
            currency = R.drawable.currency_dollar
        )
    }
}