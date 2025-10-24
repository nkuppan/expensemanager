package com.naveenapps.expensemanager.feature.budget.create

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import com.naveenapps.expensemanager.core.common.utils.toMonth
import com.naveenapps.expensemanager.core.common.utils.toMonthAndYear
import com.naveenapps.expensemanager.core.common.utils.toYearInt
import com.naveenapps.expensemanager.core.designsystem.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.components.DeleteDialogItem
import com.naveenapps.expensemanager.core.designsystem.components.IconAndColorComponent
import com.naveenapps.expensemanager.core.designsystem.components.SelectedItemView
import com.naveenapps.expensemanager.core.designsystem.ui.components.ClickableTextField
import com.naveenapps.expensemanager.core.designsystem.ui.components.DecimalTextField
import com.naveenapps.expensemanager.core.designsystem.ui.components.MonthPicker
import com.naveenapps.expensemanager.core.designsystem.ui.components.StringTextField
import com.naveenapps.expensemanager.core.designsystem.ui.components.TopNavigationBarWithDeleteAction
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.TextFieldValue
import com.naveenapps.expensemanager.feature.account.selection.MultipleAccountSelectionScreen
import com.naveenapps.expensemanager.feature.budget.R
import com.naveenapps.expensemanager.feature.category.selection.MultipleCategoriesSelectionScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
            }
        )
    }

    if (state.showAccountSelectionDialog) {
        ModalBottomSheet(
            onDismissRequest = {
                onAction.invoke(BudgetCreateAction.CloseAccountSelectionDialog)
            },
            containerColor = MaterialTheme.colorScheme.background,
            tonalElevation = 0.dp,
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
            containerColor = MaterialTheme.colorScheme.background,
            tonalElevation = 0.dp,
        ) {
            MultipleCategoriesSelectionScreen(
                selectedCategories = state.selectedCategories,
            ) { items, selected ->
                onAction.invoke(BudgetCreateAction.SelectCategories(selected, items))
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopNavigationBarWithDeleteAction(
                title = stringResource(id = R.string.budgets),
                isDeleteEnabled = state.showDeleteButton,
                onNavigationIconClick = {
                    onAction.invoke(BudgetCreateAction.ClosePage)
                },
                onDeleteActionClick = {
                    onAction.invoke(BudgetCreateAction.OpenDeleteDialog)
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                onAction.invoke(BudgetCreateAction.Save)
            }) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = "",
                )
            }
        },
    ) { innerPadding ->

        BudgetCreateScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
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
    modifier: Modifier = Modifier
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
                    shape = RoundedCornerShape(8.dp),
                ),
            currentMonth = selectedDate.value.toMonth(),
            currentYear = selectedDate.value.toYearInt(),
            confirmButtonCLicked = { month, year ->
                SimpleDateFormat("MM-yyyy", Locale.getDefault()).parse("$month-$year")?.let {
                    selectedDate.onValueChange?.invoke(
                        it,
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
            value = selectedDate.value.toMonthAndYear(),
            label = R.string.select_date,
            leadingIcon = Icons.Default.EditCalendar,
            onClick = {
                focusManager.clearFocus(force = true)
                showDatePicker = true
            },
        )

        StringTextField(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                .fillMaxWidth(),
            value = nameField.value,
            isError = nameField.valueError,
            errorMessage = stringResource(id = R.string.budget_name_error),
            onValueChange = nameField.onValueChange,
            label = R.string.budget_name,
        )

        IconAndColorComponent(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                .fillMaxWidth(),
            selectedColor = selectedColorField.value,
            selectedIcon = selectedIconField.value,
            onColorSelection = selectedColorField.onValueChange,
            onIconSelection = selectedIconField.onValueChange,
        )

        DecimalTextField(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                .fillMaxWidth(),
            value = amountField.value,
            isError = amountField.valueError,
            errorMessage = stringResource(id = R.string.budget_amount_error),
            onValueChange = amountField.onValueChange,
            leadingIconText = currencyIconField,
            label = R.string.budget_amount,
        )

        HorizontalDivider(modifier = Modifier.padding(top = 16.dp))

        SelectedItemView(
            modifier = Modifier
                .clickable {
                    onAction.invoke(BudgetCreateAction.OpenAccountSelectionDialog)
                }
                .padding(16.dp)
                .fillMaxWidth(),
            title = stringResource(id = R.string.select_account),
            icon = Icons.Default.AccountBalance,
            selectedCount = accountCount,
        )

        SelectedItemView(
            modifier = Modifier
                .clickable {
                    onAction.invoke(BudgetCreateAction.OpenCategorySelectionDialog)
                }
                .padding(16.dp)
                .fillMaxWidth(),
            title = stringResource(id = R.string.select_category),
            icon = Icons.Default.FilterList,
            selectedCount = categoriesCount,
        )

        HorizontalDivider()

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        )
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
    ExpenseManagerTheme {
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
                selectedAccounts = emptyList()
            ),
            onAction = {},
        )
    }
}
