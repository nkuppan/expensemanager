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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naveenapps.expensemanager.core.designsystem.components.IconAndColorComponent
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppDialog
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

    val snackbarHostState = remember { SnackbarHostState() }

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
            negativeButtonText = stringResource(id = R.string.cancel),
        )
    }

    val showDelete by viewModel.showDelete.collectAsState(null)

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopNavigationBarWithDeleteAction(
                title = stringResource(id = R.string.accounts),
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
            FloatingActionButton(onClick = viewModel::saveOrUpdateAccount) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = "",
                )
            }
        },
    ) { innerPadding ->

        val name by viewModel.nameField.collectAsState()
        val accountType by viewModel.accountTypeField.collectAsState()
        val currentBalance by viewModel.currentBalanceField.collectAsState()
        val creditLimit by viewModel.creditLimitField.collectAsState()
        val currencyIcon by viewModel.currencyIconField.collectAsState()
        val colorValue by viewModel.colorValueField.collectAsState()
        val iconValue by viewModel.iconValueField.collectAsState()
        val availableCreditLimit by viewModel.availableCreditLimit.collectAsState()
        val availableCreditLimitColor by viewModel.availableCreditLimitColor.collectAsState()

        AccountCreateScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            nameField = name,
            currentBalance = currentBalance,
            selectedAccountType = accountType,
            currencyIcon = currencyIcon,
            selectedColor = colorValue,
            selectedIcon = iconValue,
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
            onIconSelection = selectedColor.onValueChange,
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
    ExpenseManagerTheme {
        AccountCreateScreen()
    }
}
