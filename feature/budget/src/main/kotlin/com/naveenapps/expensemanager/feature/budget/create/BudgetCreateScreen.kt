package com.naveenapps.expensemanager.feature.budget.create

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.common.utils.fromShortMonthAndYearToDate
import com.naveenapps.expensemanager.core.common.utils.toMonth
import com.naveenapps.expensemanager.core.common.utils.toMonthAndYear
import com.naveenapps.expensemanager.core.common.utils.toYearInt
import com.naveenapps.expensemanager.core.designsystem.components.DeleteDialogItem
import com.naveenapps.expensemanager.core.designsystem.components.IconAndColorComponent
import com.naveenapps.expensemanager.core.designsystem.components.SelectedItemView
import com.naveenapps.expensemanager.core.designsystem.ui.components.ClickableTextField
import com.naveenapps.expensemanager.core.designsystem.ui.components.DecimalTextField
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.core.designsystem.ui.components.MonthPicker
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
    ) {
        // Period section
        SectionHeader(
            title = stringResource(R.string.period),
            modifier = Modifier.padding(top = 16.dp, bottom = 6.dp),
        )
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

        // Details section
        SectionHeader(
            title = stringResource(com.naveenapps.expensemanager.feature.category.R.string.details),
            modifier = Modifier.padding(top = 20.dp, bottom = 6.dp),
        )
        StringTextField(
            modifier = Modifier.fillMaxWidth(),
            value = nameField.value,
            isError = nameField.valueError,
            errorMessage = stringResource(id = R.string.budget_name_error),
            onValueChange = nameField.onValueChange,
            label = R.string.budget_name,
        )

        Spacer(modifier = Modifier.height(8.dp))

        DecimalTextField(
            modifier = Modifier.fillMaxWidth(),
            value = amountField.value,
            isError = amountField.valueError,
            errorMessage = stringResource(id = R.string.budget_amount_error),
            onValueChange = amountField.onValueChange,
            leadingIconText = currencyIconField,
            label = R.string.budget_amount,
        )

        // Appearance section
        SectionHeader(
            title = stringResource(com.naveenapps.expensemanager.feature.category.R.string.appearance),
            modifier = Modifier.padding(top = 20.dp, bottom = 6.dp),
        )
        IconAndColorComponent(
            modifier = Modifier.fillMaxWidth(),
            selectedColor = selectedColorField.value,
            selectedIcon = selectedIconField.value,
            onColorSelection = selectedColorField.onValueChange,
            onIconSelection = selectedIconField.onValueChange,
        )

        // Scope section
        SectionHeader(
            title = stringResource(R.string.budget_scope),
            modifier = Modifier.padding(top = 20.dp, bottom = 6.dp),
        )

        SelectedItemView(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .clickable {
                    onAction.invoke(BudgetCreateAction.OpenAccountSelectionDialog)
                }
                .padding(vertical = 14.dp),
            title = stringResource(id = R.string.select_account),
            icon = Icons.Default.AccountBalance,
            selectedCount = accountCount,
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
        )

        SelectedItemView(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                .clickable {
                    onAction.invoke(BudgetCreateAction.OpenCategorySelectionDialog)
                }
                .padding(vertical = 14.dp),
            title = stringResource(id = R.string.select_category),
            icon = Icons.Default.FilterList,
            selectedCount = categoriesCount,
        )

        // FAB clearance
        Spacer(modifier = Modifier.height(72.dp))
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier,
    )
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
