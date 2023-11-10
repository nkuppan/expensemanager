package com.naveenapps.expensemanager.presentation.account.create

import androidx.annotation.ColorRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.naveenapps.expensemanager.R
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.presentation.selection.ColorSelectionScreen
import com.naveenapps.expensemanager.presentation.selection.IconAndColorComponent
import com.naveenapps.expensemanager.presentation.selection.IconSelectionScreen
import com.naveenapps.expensemanager.ui.components.AppDialog
import com.naveenapps.expensemanager.ui.components.DecimalTextField
import com.naveenapps.expensemanager.ui.components.StringTextField
import com.naveenapps.expensemanager.ui.components.TopNavigationBarWithDeleteAction
import com.naveenapps.expensemanager.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.ui.utils.UiText
import kotlinx.coroutines.launch


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AccountCreateScreen(
    navController: NavController,
    accountId: String?,
) {

    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }

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
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.account_create_success)
            )
        })
    }

    var showBottomSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
            },
            sheetState = bottomSheetState,
            windowInsets = WindowInsets(0.dp)
        ) {

            AccountCreateBottomSheetContent(
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
                title = stringResource(id = R.string.account),
                actionId = accountId
            ) {
                showDeleteDialog = true
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::saveOrUpdateAccount) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_done),
                    contentDescription = ""
                )
            }
        }
    ) { innerPadding ->

        val name by viewModel.name.collectAsState()
        val nameErrorMessage by viewModel.nameErrorMessage.collectAsState()
        val currentBalance by viewModel.currentBalance.collectAsState()
        val currentBalanceErrorMessage by viewModel.currentBalanceErrorMessage.collectAsState()
        val creditLimit by viewModel.creditLimit.collectAsState()
        val creditLimitErrorMessage by viewModel.creditLimitErrorMessage.collectAsState()
        val currencyIcon by viewModel.currencyIcon.collectAsState()
        val colorValue by viewModel.colorValue.collectAsState()
        val iconValue by viewModel.icon.collectAsState()
        val selectedAccountType by viewModel.accountType.collectAsState()
        val availableCreditLimit by viewModel.availableCreditLimit.collectAsState()
        val availableCreditLimitColor by viewModel.availableCreditLimitColor.collectAsState()

        AccountCreateScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            selectedColor = colorValue,
            selectedIcon = iconValue,
            name = name,
            nameErrorMessage = nameErrorMessage,
            currentBalance = currentBalance,
            currentBalanceErrorMessage = currentBalanceErrorMessage,
            creditLimit = creditLimit,
            creditLimitErrorMessage = creditLimitErrorMessage,
            selectedAccountType = selectedAccountType,
            onAccountTypeChange = viewModel::setAccountType,
            currency = currencyIcon,
            onNameChange = viewModel::setNameChange,
            onCurrentBalanceChange = viewModel::setCurrentBalanceChange,
            onCreditLimitChange = viewModel::setCreditLimitChange,
            availableCreditLimit = availableCreditLimit,
            availableCreditLimitColor = availableCreditLimitColor,
            openColorPicker = {
                scope.launch {
                    if (sheetSelection != 2) {
                        sheetSelection = 2
                    }
                    showBottomSheet = true
                }
            },
            openIconPicker = {
                scope.launch {
                    if (sheetSelection != 1) {
                        sheetSelection = 1
                    }
                    showBottomSheet = true
                }
            }
        )
    }
}

@Composable
private fun AccountCreateBottomSheetContent(
    sheetSelection: Int,
    viewModel: AccountCreateViewModel,
    hideBottomSheet: () -> Unit
) {
    val context = LocalContext.current

    if (sheetSelection == 1) {
        IconSelectionScreen {
            viewModel.setIcon(context.resources.getResourceName(it))
            hideBottomSheet.invoke()
        }
    } else {
        ColorSelectionScreen {
            viewModel.setColorValue(it)
            hideBottomSheet.invoke()
        }
    }
}


@Composable
private fun AccountCreateScreen(
    onAccountTypeChange: ((AccountType) -> Unit),
    modifier: Modifier = Modifier,
    selectedAccountType: AccountType = AccountType.REGULAR,
    name: String = "",
    nameErrorMessage: UiText? = null,
    currentBalance: String = "",
    currentBalanceErrorMessage: UiText? = null,
    currency: Int? = null,
    selectedColor: String = "#000000",
    selectedIcon: String = "account_balance",
    openIconPicker: (() -> Unit)? = null,
    openColorPicker: (() -> Unit)? = null,
    onNameChange: ((String) -> Unit)? = null,
    onCurrentBalanceChange: ((String) -> Unit)? = null,
    creditLimit: String = "",
    creditLimitErrorMessage: UiText? = null,
    onCreditLimitChange: ((String) -> Unit)? = null,
    availableCreditLimit: UiText? = null,
    @ColorRes availableCreditLimitColor: Int = R.color.green_500,
) {

    val context = LocalContext.current

    Column(modifier = modifier) {

        AccountTypeSelectionView(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            selectedAccountType = selectedAccountType,
            onAccountTypeChange = onAccountTypeChange
        )

        StringTextField(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                .fillMaxWidth(),
            value = name,
            errorMessage = nameErrorMessage,
            onValueChange = onNameChange,
            label = R.string.account_name
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
            value = currentBalance,
            errorMessage = currentBalanceErrorMessage,
            onValueChange = onCurrentBalanceChange,
            leadingIcon = currency,
            label = R.string.current_balance,
        )

        if (selectedAccountType == AccountType.CREDIT) {
            DecimalTextField(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                    .fillMaxWidth(),
                value = creditLimit,
                errorMessage = creditLimitErrorMessage,
                onValueChange = onCreditLimitChange,
                leadingIcon = currency,
                label = R.string.credit_limit,
            )
        }
        if (availableCreditLimit != null) {
            Row(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                    .background(
                        color = colorResource(id = availableCreditLimitColor).copy(.1f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(16.dp)
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(id = R.string.available_balance),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = availableCreditLimit.asString(context),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
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
            currency = com.naveenapps.expensemanager.core.data.R.drawable.currency_dollar
        )
    }
}