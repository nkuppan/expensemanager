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
import org.koin.compose.viewmodel.koinViewModel
import com.naveenapps.expensemanager.core.designsystem.components.DeleteDialogItem
import com.naveenapps.expensemanager.core.designsystem.components.IconAndColorComponent
import com.naveenapps.expensemanager.core.designsystem.ui.components.DecimalTextField
import com.naveenapps.expensemanager.core.designsystem.ui.components.StringTextField
import com.naveenapps.expensemanager.core.designsystem.ui.components.TopNavigationBarWithDeleteAction
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.TextFieldValue
import com.naveenapps.expensemanager.feature.account.R

@Composable
fun AccountCreateScreen(
    viewModel: AccountCreateViewModel = koinViewModel()
) {

    val state by viewModel.state.collectAsState()

    AccountCreateScaffoldView(
        state = state,
        onAction = viewModel::processAction,
    )
}

@Composable
private fun AccountCreateScaffoldView(
    state: AccountCreateState,
    onAction: (AccountCreateAction) -> Unit,
) {

    val snackbarHostState = remember { SnackbarHostState() }

    if (state.showDeleteDialog) {
        DeleteDialogItem(
            confirm = { onAction.invoke(AccountCreateAction.Delete) },
            dismiss = { onAction.invoke(AccountCreateAction.DismissDeleteDialog) }
        )
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopNavigationBarWithDeleteAction(
                title = stringResource(id = R.string.accounts),
                isDeleteEnabled = state.showDeleteButton,
                onNavigationIconClick = { onAction.invoke(AccountCreateAction.ClosePage) },
                onDeleteActionClick = { onAction.invoke(AccountCreateAction.ShowDeleteDialog) },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAction.invoke(AccountCreateAction.Save) }) {
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
            name = state.name,
            amount = state.amount,
            type = state.type,
            currency = state.currency,
            color = state.color,
            icon = state.icon,
            creditLimit = state.creditLimit,
            totalAmount = state.totalAmount,
            totalAmountBackgroundColor = state.totalAmountBackgroundColor
        )
    }
}

@Composable
private fun AccountCreateScreen(
    modifier: Modifier = Modifier,
    name: TextFieldValue<String>,
    amount: TextFieldValue<String>,
    type: TextFieldValue<AccountType>,
    currency: Currency,
    color: TextFieldValue<String>,
    icon: TextFieldValue<String>,
    creditLimit: TextFieldValue<String>,
    totalAmount: String,
    totalAmountBackgroundColor: Int,
) {
    Column(modifier = modifier) {
        AccountTypeSelectionView(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            selectedAccountType = type.value,
            onAccountTypeChange = type.onValueChange!!,
        )

        StringTextField(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                .fillMaxWidth(),
            value = name.value,
            isError = name.valueError,
            onValueChange = name.onValueChange,
            label = R.string.account_name,
            errorMessage = stringResource(id = R.string.account_name_error),
        )

        IconAndColorComponent(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                .fillMaxWidth(),
            selectedColor = color.value,
            selectedIcon = icon.value,
            onColorSelection = color.onValueChange,
            onIconSelection = icon.onValueChange,
        )

        DecimalTextField(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                .fillMaxWidth(),
            value = amount.value,
            isError = amount.valueError,
            errorMessage = stringResource(id = R.string.current_balance_error),
            onValueChange = amount.onValueChange,
            leadingIconText = currency.symbol,
            label = R.string.current_balance,
        )

        if (type.value == AccountType.CREDIT) {
            DecimalTextField(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                    .fillMaxWidth(),
                value = creditLimit.value,
                isError = creditLimit.valueError,
                errorMessage = stringResource(id = R.string.credit_limit_error),
                onValueChange = creditLimit.onValueChange,
                leadingIconText = currency.symbol,
                label = R.string.credit_limit,
            )
        }
        if (totalAmount.isNotBlank()) {
            Row(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                    .background(
                        color = colorResource(id = totalAmountBackgroundColor).copy(.1f),
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
                    text = totalAmount,
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
        value = "#000000", valueError = false, onValueChange = { }
    )
    val selectedIconField = TextFieldValue(
        value = "account_balance_wallet", valueError = false, onValueChange = { }
    )
    val accountField = TextFieldValue(
        value = AccountType.CREDIT,
        valueError = false,
        onValueChange = { }
    )
    ExpenseManagerTheme {
        AccountCreateScaffoldView(
            state = AccountCreateState(
                name = nameField,
                type = accountField,
                color = selectedColorField,
                icon = selectedIconField,
                creditLimit = nameField,
                currency = Currency("$", ""),
                totalAmount = "$ 0.0",
                totalAmountBackgroundColor = com.naveenapps.expensemanager.core.common.R.color.green_500,
                amount = nameField,
                showDeleteButton = false,
                showDeleteDialog = false,
            ),
            onAction = {},
        )
    }
}
