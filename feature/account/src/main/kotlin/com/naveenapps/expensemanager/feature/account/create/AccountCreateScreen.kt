package com.naveenapps.expensemanager.feature.account.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naveenapps.expensemanager.core.designsystem.components.DeleteDialogItem
import com.naveenapps.expensemanager.core.designsystem.components.IconAndColorComponent
import com.naveenapps.expensemanager.core.designsystem.ui.components.DecimalTextField
import com.naveenapps.expensemanager.core.designsystem.ui.components.StringTextField
import com.naveenapps.expensemanager.core.designsystem.ui.components.TopNavigationBarWithDeleteAction
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.TextFieldValue
import com.naveenapps.expensemanager.feature.account.R

@Composable
fun AccountCreateScreen(
    viewModel: AccountCreateViewModel = hiltViewModel()
) {

    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
    val isDeleteEnabled by viewModel.isDeleteEnabled.collectAsState()

    val nameField by viewModel.nameField.collectAsState()
    val selectedAccountType by viewModel.accountTypeField.collectAsState()
    val currentBalance by viewModel.currentBalanceField.collectAsState()
    val creditLimit by viewModel.creditLimitField.collectAsState()
    val currencyIcon by viewModel.currencyIconField.collectAsState()
    val selectedColor by viewModel.colorValueField.collectAsState()
    val selectedIcon by viewModel.iconValueField.collectAsState()
    val availableCreditLimit by viewModel.availableCreditLimit.collectAsState()
    val availableCreditLimitColor by viewModel.availableCreditLimitColor.collectAsState()

    AccountCreateScaffoldView(
        showDeleteDialog = showDeleteDialog,
        isDeleteEnabled = isDeleteEnabled,
        nameField = nameField,
        currentBalance = currentBalance,
        selectedAccountType = selectedAccountType,
        currencyIcon = currencyIcon,
        selectedColor = selectedColor,
        selectedIcon = selectedIcon,
        creditLimit = creditLimit,
        availableCreditLimit = availableCreditLimit,
        availableCreditLimitColor = availableCreditLimitColor,
        saveOrUpdateAccount = viewModel::saveOrUpdateAccount,
        deleteAccount = viewModel::deleteAccount,
        openDeleteDialog = viewModel::showDeleteDialog,
        dismissDeleteDialog = viewModel::dismissDeleteDialog,
        closePage = viewModel::closePage
    )
}

@Composable
private fun AccountCreateScaffoldView(
    showDeleteDialog: Boolean,
    isDeleteEnabled: Boolean,
    nameField: TextFieldValue<String>,
    currentBalance: TextFieldValue<String>,
    selectedAccountType: TextFieldValue<AccountType>,
    currencyIcon: TextFieldValue<String>,
    selectedColor: TextFieldValue<String>,
    selectedIcon: TextFieldValue<String>,
    creditLimit: TextFieldValue<String>,
    availableCreditLimit: TextFieldValue<Amount?>,
    availableCreditLimitColor: TextFieldValue<Int>,
    saveOrUpdateAccount: () -> Unit,
    deleteAccount: () -> Unit,
    openDeleteDialog: () -> Unit,
    dismissDeleteDialog: () -> Unit,
    closePage: () -> Unit,
) {

    val snackbarHostState = remember { SnackbarHostState() }

    if (showDeleteDialog) {
        DeleteDialogItem(
            confirm = deleteAccount,
            dismiss = dismissDeleteDialog
        )
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopNavigationBarWithDeleteAction(
                title = stringResource(id = R.string.accounts),
                isDeleteEnabled = isDeleteEnabled,
                onNavigationIconClick = closePage,
                onDeleteActionClick = openDeleteDialog,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = saveOrUpdateAccount) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = "",
                )
            }
        },
    ) { innerPadding ->
        AccountCreateScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            nameField = nameField,
            currentBalance = currentBalance,
            selectedAccountType = selectedAccountType,
            currencyIcon = currencyIcon,
            selectedColor = selectedColor,
            selectedIcon = selectedIcon,
            creditLimit = creditLimit,
            availableCreditLimit = availableCreditLimit,
            availableCreditLimitColor = availableCreditLimitColor
        )
    }
}

@Composable
private fun AccountCreateScreen(
    modifier: Modifier = Modifier,
    nameField: TextFieldValue<String>,
    currentBalance: TextFieldValue<String>,
    selectedAccountType: TextFieldValue<AccountType>,
    currencyIcon: TextFieldValue<String>,
    selectedColor: TextFieldValue<String>,
    selectedIcon: TextFieldValue<String>,
    creditLimit: TextFieldValue<String>,
    availableCreditLimit: TextFieldValue<Amount?>,
    availableCreditLimitColor: TextFieldValue<Int>,
) {
    Column(modifier = modifier) {
        AccountTypeSelectionView(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            selectedAccountType = selectedAccountType.value,
            onAccountTypeChange = selectedAccountType.onValueChange!!,
        )

        StringTextField(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                .fillMaxWidth(),
            value = nameField.value,
            isError = nameField.valueError,
            onValueChange = nameField.onValueChange,
            label = R.string.account_name,
            errorMessage = stringResource(id = R.string.account_name_error),
        )

        IconAndColorComponent(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                .fillMaxWidth(),
            selectedColor = selectedColor.value,
            selectedIcon = selectedIcon.value,
            onColorSelection = selectedColor.onValueChange,
            onIconSelection = selectedIcon.onValueChange,
        )

        DecimalTextField(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                .fillMaxWidth(),
            value = currentBalance.value,
            isError = currentBalance.valueError,
            errorMessage = stringResource(id = R.string.current_balance_error),
            onValueChange = currentBalance.onValueChange,
            leadingIconText = currencyIcon.value,
            label = R.string.current_balance,
        )

        if (selectedAccountType.value == AccountType.CREDIT) {
            DecimalTextField(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                    .fillMaxWidth(),
                value = creditLimit.value,
                isError = creditLimit.valueError,
                errorMessage = stringResource(id = R.string.credit_limit_error),
                onValueChange = creditLimit.onValueChange,
                leadingIconText = currencyIcon.value,
                label = R.string.credit_limit,
            )
        }
        if (availableCreditLimit.value != null) {
            Row(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                    .background(
                        color = colorResource(id = availableCreditLimitColor.value).copy(.1f),
                        shape = RoundedCornerShape(4.dp),
                    )
                    .padding(16.dp),
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(id = R.string.available_balance),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = availableCreditLimit.value?.amountString ?: "",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        )
    }
}

@Preview
@Composable
private fun AccountCreateStatePreview() {
    val nameField = TextFieldValue(
        value = "", valueError = false, onValueChange = { }
    )
    val selectedColorField = TextFieldValue(
        value = "#FFFFF", valueError = false, onValueChange = { }
    )
    val selectedIconField = TextFieldValue(
        value = "account_balance_wallet", valueError = false, onValueChange = { }
    )
    val amountField = TextFieldValue<Amount?>(
        value = Amount(amount = 0.0),
        valueError = false,
        onValueChange = { }
    )
    val colorField = TextFieldValue(
        value = com.naveenapps.expensemanager.core.common.R.color.red_500,
        valueError = false,
        onValueChange = { }
    )
    val accountField = TextFieldValue(
        value = AccountType.REGULAR,
        valueError = false,
        onValueChange = { }
    )
    ExpenseManagerTheme {
        AccountCreateScaffoldView(
            showDeleteDialog = true,
            isDeleteEnabled = true,
            nameField = nameField,
            currentBalance = nameField,
            selectedAccountType = accountField,
            currencyIcon = nameField,
            selectedColor = selectedColorField,
            selectedIcon = selectedIconField,
            creditLimit = nameField,
            availableCreditLimit = amountField,
            availableCreditLimitColor = colorField,
            saveOrUpdateAccount = { /*TODO*/ },
            deleteAccount = { /*TODO*/ },
            openDeleteDialog = { /*TODO*/ },
            dismissDeleteDialog = { /*TODO*/ }) {

        }
    }
}
