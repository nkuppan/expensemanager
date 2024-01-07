package com.naveenapps.expensemanager.feature.budget.create

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.naveenapps.expensemanager.core.designsystem.ui.utils.UiText
import com.naveenapps.expensemanager.core.model.TextFieldValue
import com.naveenapps.expensemanager.feature.account.selection.MultipleAccountSelectionScreen
import com.naveenapps.expensemanager.feature.budget.R
import com.naveenapps.expensemanager.feature.category.selection.MultipleCategoriesSelectionScreen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class BottomSheetSelection {
    NONE,
    ACCOUNT_SELECTION,
    CATEGORY_SELECTION,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetCreateScreen(
    viewModel: BudgetCreateViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()

    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
    val isDeleteEnabled by viewModel.isDeleteEnabled.collectAsState()

    val nameField by viewModel.nameField.collectAsState()
    val amountField by viewModel.amountField.collectAsState()
    val currencyIcon by viewModel.currencyIcon.collectAsState()
    val selectedColorField by viewModel.selectedColorField.collectAsState()
    val selectedIconField by viewModel.selectedIconField.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    val accountCount by viewModel.accountCount.collectAsState()
    val categoriesCount by viewModel.categoriesCount.collectAsState()

    val bottomSheetSelection by viewModel.bottomSheetSelection.collectAsState()

    if (showDeleteDialog) {
        DeleteDialogItem(
            confirm = viewModel::deleteBudget,
            dismiss = viewModel::closeDeleteDialog
        )
    }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (bottomSheetSelection != BottomSheetSelection.NONE) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    viewModel.hideBottomSheet()
                    bottomSheetState.hide()
                }
            },
            sheetState = bottomSheetState,
            windowInsets = WindowInsets(0.dp),
            containerColor = MaterialTheme.colorScheme.background,
            tonalElevation = 0.dp,
        ) {
            BudgetCreateBottomSheetContent(
                bottomSheetSelection,
                viewModel,
            ) {
                scope.launch {
                    viewModel.hideBottomSheet()
                    bottomSheetState.hide()
                }
            }
        }
    }

    BudgetCreateScreenScaffoldView(
        isDeleteEnabled = isDeleteEnabled,
        nameField = nameField,
        amountField = amountField,
        currencyIcon = currencyIcon,
        selectedColorField = selectedColorField,
        selectedIconField = selectedIconField,
        selectedDate = selectedDate,
        accountCount = accountCount,
        categoriesCount = categoriesCount,
        closePage = viewModel::closePage,
        openDeleteDialog = viewModel::openDeleteDialog,
        saveOrUpdateBudget = viewModel::saveOrUpdateBudget,
        openAccountSelection = viewModel::openAccountSelection,
        openCategorySelection = viewModel::openCategorySelection,
    )
}

@Composable
private fun BudgetCreateScreenScaffoldView(
    isDeleteEnabled: Boolean,
    nameField: TextFieldValue<String>,
    amountField: TextFieldValue<String>,
    currencyIcon: TextFieldValue<String>,
    selectedColorField: TextFieldValue<String>,
    selectedIconField: TextFieldValue<String>,
    selectedDate: TextFieldValue<Date>,
    accountCount: UiText,
    categoriesCount: UiText,
    closePage: () -> Unit,
    openDeleteDialog: () -> Unit,
    saveOrUpdateBudget: () -> Unit,
    openAccountSelection: () -> Unit,
    openCategorySelection: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopNavigationBarWithDeleteAction(
                title = stringResource(id = R.string.budgets),
                isDeleteEnabled = isDeleteEnabled,
                onNavigationIconClick = closePage,
                onDeleteActionClick = openDeleteDialog,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = saveOrUpdateBudget) {
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
            nameField = nameField,
            amountField = amountField,
            currencyIconField = currencyIcon,
            selectedColorField = selectedColorField,
            selectedIconField = selectedIconField,
            selectedDate = selectedDate,
            accountCount = accountCount,
            categoriesCount = categoriesCount,
            openAccountSelection = openAccountSelection,
            openCategorySelection = openCategorySelection
        )
    }
}

@Composable
fun BudgetCreateScreen(
    modifier: Modifier,
    nameField: TextFieldValue<String>,
    amountField: TextFieldValue<String>,
    currencyIconField: TextFieldValue<String>,
    selectedColorField: TextFieldValue<String>,
    selectedIconField: TextFieldValue<String>,
    selectedDate: TextFieldValue<Date>,
    accountCount: UiText,
    categoriesCount: UiText,
    openAccountSelection: () -> Unit,
    openCategorySelection: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
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
            leadingIconText = currencyIconField.value,
            label = R.string.budget_amount,
        )

        Divider(
            modifier = Modifier.padding(top = 16.dp),
        )

        SelectedItemView(
            modifier = Modifier
                .clickable {
                    openAccountSelection.invoke()
                }
                .padding(16.dp)
                .fillMaxWidth(),
            title = stringResource(id = R.string.select_account),
            icon = Icons.Default.AccountBalance,
            selectedCount = accountCount.asString(context),
        )

        SelectedItemView(
            modifier = Modifier
                .clickable {
                    openCategorySelection.invoke()
                }
                .padding(16.dp)
                .fillMaxWidth(),
            title = stringResource(id = R.string.select_category),
            icon = Icons.Default.FilterList,
            selectedCount = categoriesCount.asString(context),
        )

        Divider()

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        )
    }
}

@Composable
private fun BudgetCreateBottomSheetContent(
    sheetSelection: BottomSheetSelection,
    viewModel: BudgetCreateViewModel,
    hideBottomSheet: () -> Unit,
) {
    when (sheetSelection) {
        BottomSheetSelection.ACCOUNT_SELECTION -> {
            MultipleAccountSelectionScreen(
                selectedAccounts = viewModel.getSelectedAccounts(),
            ) { items, selected ->
                viewModel.setAccounts(items, selected)
                hideBottomSheet.invoke()
            }
        }

        BottomSheetSelection.CATEGORY_SELECTION -> {
            MultipleCategoriesSelectionScreen(
                selectedCategories = viewModel.getSelectedCategories(),
            ) { items, selected ->
                viewModel.setCategories(items, selected)
                hideBottomSheet.invoke()
            }
        }

        else -> Unit
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
    val selectedCurrencyField = TextFieldValue(
        value = "$", valueError = false, onValueChange = { }
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
        BudgetCreateScreenScaffoldView(
            isDeleteEnabled = true,
            nameField = nameField,
            amountField = amountField,
            currencyIcon = selectedCurrencyField,
            selectedColorField = selectedColorField,
            selectedIconField = selectedIconField,
            selectedDate = dateField,
            accountCount = UiText.DynamicString("All"),
            categoriesCount =  UiText.DynamicString("All"),
            closePage = {},
            openDeleteDialog = {},
            saveOrUpdateBudget = {},
            openAccountSelection = {},
            openCategorySelection = {}
        )
    }
}
