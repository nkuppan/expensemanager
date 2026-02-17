package com.naveenapps.expensemanager.feature.account.create

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.components.DeleteDialogItem
import com.naveenapps.expensemanager.core.designsystem.components.IconAndColorComponent
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.DecimalTextField
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.core.designsystem.ui.components.SettingsSection
import com.naveenapps.expensemanager.core.designsystem.ui.components.StringTextField
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.TextFieldValue
import com.naveenapps.expensemanager.feature.account.R
import org.koin.compose.viewmodel.koinViewModel

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
            dismiss = { onAction.invoke(AccountCreateAction.DismissDeleteDialog) },
        )
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            ExpenseManagerTopAppBar(
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationBackClick = {
                    onAction.invoke(AccountCreateAction.ClosePage)
                },
                title = if (state.showDeleteButton)
                    stringResource(R.string.edit_account)
                else
                    stringResource(R.string.create_account),
                actions = {
                    if (state.showDeleteButton) {
                        IconButton(onClick = { onAction.invoke(AccountCreateAction.ShowDeleteDialog) }) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAction.invoke(AccountCreateAction.Save) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = null,
                    )
                },
                text = {
                    Text(text = stringResource(R.string.save))
                },
            )
        },
    ) { innerPadding ->
        AccountCreateScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            name = state.name,
            amount = state.amount,
            type = state.type,
            currency = state.currency,
            color = state.color,
            icon = state.icon,
            creditLimit = state.creditLimit,
            totalAmount = state.totalAmount,
            totalAmountBackgroundColor = state.totalAmountBackgroundColor,
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
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsSection(
            title = stringResource(R.string.account_type),
            modifier = Modifier.padding(top = 8.dp),
        ) {
            AppCardView {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    AccountTypeSelectionView(
                        modifier = Modifier.fillMaxWidth(),
                        selectedAccountType = type.value,
                        onAccountTypeChange = type.onValueChange!!,
                    )
                }
            }
        }

        // Details
        SettingsSection(title = stringResource(R.string.details)) {
            AppCardView {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    StringTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = name.value,
                        isError = name.valueError,
                        onValueChange = name.onValueChange,
                        label = R.string.account_name,
                        errorMessage = stringResource(id = R.string.account_name_error),
                    )
                    BalanceSectionContent(
                        amount = amount,
                        currency = currency,
                        type = type,
                        creditLimit = creditLimit,
                        totalAmount = totalAmount,
                        totalAmountBackgroundColor = totalAmountBackgroundColor
                    )
                }
            }
        }

        SettingsSection(title = stringResource(R.string.appearance)) {
            AppCardView {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    IconAndColorComponent(
                        modifier = Modifier.fillMaxWidth(),
                        selectedColor = color.value,
                        selectedIcon = icon.value,
                        onColorSelection = color.onValueChange,
                        onIconSelection = icon.onValueChange,
                    )
                }
            }
        }

        // FAB clearance
        Spacer(modifier = Modifier.height(72.dp))
    }
}

@Composable
private fun ColumnScope.BalanceSectionContent(
    amount: TextFieldValue<String>,
    currency: Currency,
    type: TextFieldValue<AccountType>,
    creditLimit: TextFieldValue<String>,
    totalAmount: String,
    totalAmountBackgroundColor: Int
) {
    DecimalTextField(
        modifier = Modifier.fillMaxWidth(),
        value = amount.value,
        isError = amount.valueError,
        errorMessage = stringResource(id = R.string.current_balance_error),
        onValueChange = amount.onValueChange,
        leadingIconText = currency.symbol,
        label = R.string.current_balance,
    )

    // Credit limit — only for credit accounts
    AnimatedVisibility(
        visible = type.value == AccountType.CREDIT,
        enter = fadeIn(tween(200)) + expandVertically(tween(250)),
        exit = fadeOut(tween(150)) + shrinkVertically(tween(200)),
    ) {
        DecimalTextField(
            modifier = Modifier.fillMaxWidth(),
            value = creditLimit.value,
            isError = creditLimit.valueError,
            errorMessage = stringResource(id = R.string.credit_limit_error),
            onValueChange = creditLimit.onValueChange,
            leadingIconText = currency.symbol,
            label = R.string.credit_limit,
        )
    }

    // Available balance summary
    AnimatedVisibility(
        visible = totalAmount.isNotBlank(),
        enter = fadeIn(tween(200)) + expandVertically(tween(250)),
        exit = fadeOut(tween(150)) + shrinkVertically(tween(200)),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            shape = MaterialTheme.shapes.medium,
            color = colorResource(id = totalAmountBackgroundColor).copy(alpha = 0.1f),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.AccountBalanceWallet,
                    contentDescription = null,
                    tint = colorResource(id = totalAmountBackgroundColor),
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(id = R.string.available_balance),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = totalAmount,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = totalAmountBackgroundColor),
                )
            }
        }
    }
}

@AppPreviewsLightAndDarkMode
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
    NaveenAppsPreviewTheme(padding = 0.dp) {
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
