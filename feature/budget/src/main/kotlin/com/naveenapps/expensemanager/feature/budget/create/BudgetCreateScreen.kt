package com.naveenapps.expensemanager.feature.budget.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.common.utils.fromShortMonthAndYearToDate
import com.naveenapps.expensemanager.core.common.utils.toMonth
import com.naveenapps.expensemanager.core.common.utils.toMonthAndYear
import com.naveenapps.expensemanager.core.common.utils.toYearInt
import com.naveenapps.expensemanager.core.designsystem.components.DeleteDialogItem
import com.naveenapps.expensemanager.core.designsystem.components.IconAndColorComponent
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.ClickableTextField
import com.naveenapps.expensemanager.core.designsystem.ui.components.DecimalTextField
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.core.designsystem.ui.components.MonthPicker
import com.naveenapps.expensemanager.core.designsystem.ui.components.SettingRow
import com.naveenapps.expensemanager.core.designsystem.ui.components.SettingsSection
import com.naveenapps.expensemanager.core.designsystem.ui.components.StringTextField
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.TextFieldValue
import com.naveenapps.expensemanager.feature.account.selection.MultipleAccountSelectionScreen
import com.naveenapps.expensemanager.feature.budget.R
import com.naveenapps.expensemanager.feature.category.selection.MultipleCategoriesSelectionScreen
import org.koin.compose.viewmodel.koinViewModel
import java.util.Date

@Composable
fun BudgetCreateScreen(
    viewModel: BudgetCreateViewModel = koinViewModel()
) {

    val state by viewModel.state.collectAsState()

    BudgetCreateScreenContentView(
        state = state,
        onAction = viewModel::processAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetCreateScreenContentView(
    state: BudgetCreateState,
    onAction: (BudgetCreateAction) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    if (state.showDeleteDialog) {
        DeleteDialogItem(
            confirm = {
                onAction.invoke(BudgetCreateAction.Delete)
            },
            dismiss = {
                onAction.invoke(BudgetCreateAction.ClosePage)
            },
        )
    }

    if (state.showAccountSelectionDialog) {
        ModalBottomSheet(
            onDismissRequest = {
                onAction.invoke(BudgetCreateAction.CloseAccountSelectionDialog)
            },
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            MultipleAccountSelectionScreen(
                selectedAccounts = state.selectedAccounts,
            ) { items, selected ->
                onAction.invoke(BudgetCreateAction.SelectAccounts(selected, items))
            }
        }
    }

    if (state.showCategorySelectionDialog) {
        ModalBottomSheet(
            onDismissRequest = {
                onAction.invoke(BudgetCreateAction.CloseCategorySelectionDialog)
            },
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            MultipleCategoriesSelectionScreen(
                selectedCategories = state.selectedCategories,
            ) { items, selected ->
                onAction.invoke(BudgetCreateAction.SelectCategories(selected, items))
            }
        }
    }

    if (state.showMonthSelection) {
        MonthPicker(
            modifier = Modifier
                .wrapContentSize()
                .padding(16.dp)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(8.dp),
                ),
            currentMonth = state.month.value.toMonth(),
            currentYear = state.month.value.toYearInt(),
            confirmButtonCLicked = { month, year ->
                ("$month-$year").fromShortMonthAndYearToDate()?.let {
                    state.month.onValueChange?.invoke(it)
                } ?: run {
                    onAction.invoke(BudgetCreateAction.CloseMonthSelection)
                }
            },
            cancelClicked = {
                onAction.invoke(BudgetCreateAction.CloseMonthSelection)
            },
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
                    onAction.invoke(BudgetCreateAction.ClosePage)
                },
                title = if (state.showDeleteButton)
                    stringResource(R.string.edit_budget)
                else
                    stringResource(R.string.create_budget),
                actions = {
                    if (state.showDeleteButton) {
                        IconButton(onClick = { onAction.invoke(BudgetCreateAction.ShowDeleteDialog) }) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = stringResource(com.naveenapps.expensemanager.feature.category.R.string.delete),
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAction.invoke(BudgetCreateAction.Save) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = null,
                    )
                },
                text = {
                    Text(text = stringResource(com.naveenapps.expensemanager.feature.account.R.string.save))
                },
            )
        },
    ) { innerPadding ->
        BudgetCreateScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            nameField = state.name,
            amountField = state.amount,
            currencyIconField = state.currency.symbol,
            selectedColorField = state.color,
            selectedIconField = state.icon,
            selectedDate = state.month,
            accountCount = if (state.isAllAccountSelected) {
                stringResource(R.string.all)
            } else {
                state.selectedAccounts.size.toString()
            },
            categoriesCount = if (state.isAllCategorySelected) {
                stringResource(R.string.all)
            } else {
                state.selectedCategories.size.toString()
            },
            onAction = onAction,
        )
    }
}

@Composable
fun BudgetCreateScreen(
    nameField: TextFieldValue<String>,
    amountField: TextFieldValue<String>,
    currencyIconField: String,
    selectedColorField: TextFieldValue<String>,
    selectedIconField: TextFieldValue<String>,
    selectedDate: TextFieldValue<Date>,
    accountCount: String,
    categoriesCount: String,
    onAction: (BudgetCreateAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsSection(
            title = stringResource(R.string.period),
            modifier = Modifier.padding(top = 8.dp),
        ) {
            AppCardView {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    ClickableTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = selectedDate.value.toMonthAndYear(),
                        label = R.string.select_date,
                        leadingIcon = Icons.Default.EditCalendar,
                        onClick = {
                            focusManager.clearFocus(force = true)
                            onAction.invoke(BudgetCreateAction.ShowMonthSelection)
                        },
                    )
                }
            }
        }

        SettingsSection(title = stringResource(R.string.details)) {
            AppCardView {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StringTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = nameField.value,
                        isError = nameField.valueError,
                        errorMessage = stringResource(id = R.string.budget_name_error),
                        onValueChange = nameField.onValueChange,
                        label = R.string.budget_name,
                    )

                    DecimalTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = amountField.value,
                        isError = amountField.valueError,
                        errorMessage = stringResource(id = R.string.budget_amount_error),
                        onValueChange = amountField.onValueChange,
                        leadingIconText = currencyIconField,
                        label = R.string.budget_amount,
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
                        selectedColor = selectedColorField.value,
                        selectedIcon = selectedIconField.value,
                        onColorSelection = selectedColorField.onValueChange,
                        onIconSelection = selectedIconField.onValueChange,
                    )
                }
            }
        }

        SettingsSection(title = stringResource(R.string.budget_scope)) {
            AppCardView {
                Column {
                    SettingRow(
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(id = R.string.select_account),
                        icon = Icons.Default.AccountBalance,
                        value = accountCount,
                        onClick = {
                            onAction.invoke(BudgetCreateAction.OpenAccountSelectionDialog)
                        },
                        showDivider = true
                    )
                    SettingRow(
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(id = R.string.select_category),
                        icon = Icons.Default.Category,
                        value = categoriesCount,
                        onClick = {
                            onAction.invoke(BudgetCreateAction.OpenCategorySelectionDialog)
                        }
                    )
                }
            }
        }


        // FAB clearance
        Spacer(modifier = Modifier.height(72.dp))
    }
}

@AppPreviewsLightAndDarkMode
@Composable
private fun BudgetCreateStatePreview() {
    val nameField = TextFieldValue(
        value = "", valueError = false, onValueChange = { }
    )
    val selectedColorField = TextFieldValue(
        value = "#000000", valueError = false, onValueChange = { }
    )
    val selectedIconField = TextFieldValue(
        value = "account_balance_wallet", valueError = false, onValueChange = { }
    )
    val amountField = TextFieldValue(
        value = "0.0",
        valueError = false,
        onValueChange = { }
    )
    val dateField = TextFieldValue(
        value = Date(),
        valueError = false,
        onValueChange = { }
    )
    NaveenAppsPreviewTheme(padding = 0.dp) {
        BudgetCreateScreenContentView(
            state = BudgetCreateState(
                isLoading = false,
                name = nameField,
                amount = amountField,
                icon = selectedIconField,
                color = selectedColorField,
                month = dateField,
                isAllCategorySelected = true,
                isAllAccountSelected = true,
                currency = Currency(symbol = "$", name = ""),
                showDeleteDialog = false,
                showDeleteButton = true,
                showAccountSelectionDialog = false,
                showCategorySelectionDialog = false,
                selectedCategories = emptyList(),
                selectedAccounts = emptyList(),
                showMonthSelection = false,
            ),
            onAction = {},
        )
    }
}
