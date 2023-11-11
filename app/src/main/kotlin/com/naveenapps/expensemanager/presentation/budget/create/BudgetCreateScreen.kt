package com.naveenapps.expensemanager.presentation.budget.create

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
import androidx.compose.material.icons.filled.EditCalendar
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.naveenapps.expensemanager.R
import com.naveenapps.expensemanager.core.common.utils.toMonth
import com.naveenapps.expensemanager.core.common.utils.toMonthAndYear
import com.naveenapps.expensemanager.core.common.utils.toYearInt
import com.naveenapps.expensemanager.core.designsystem.components.ColorSelectionScreen
import com.naveenapps.expensemanager.core.designsystem.components.IconAndColorComponent
import com.naveenapps.expensemanager.core.designsystem.components.IconSelectionScreen
import com.naveenapps.expensemanager.core.designsystem.components.SelectedItemView
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppDialog
import com.naveenapps.expensemanager.core.designsystem.ui.components.ClickableTextField
import com.naveenapps.expensemanager.core.designsystem.ui.components.DecimalTextField
import com.naveenapps.expensemanager.core.designsystem.ui.components.MonthPicker
import com.naveenapps.expensemanager.core.designsystem.ui.components.StringTextField
import com.naveenapps.expensemanager.core.designsystem.ui.components.TopNavigationBarWithDeleteAction
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.designsystem.ui.utils.UiText
import com.naveenapps.expensemanager.feature.category.selection.MultipleCategoriesSelectionScreen
import com.naveenapps.expensemanager.presentation.budget.create.BudgetCreateSheetSelection.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


enum class BudgetCreateSheetSelection {
    ICON_SELECTION,
    COLOR_SELECTION,
    ACCOUNT_SELECTION,
    CATEGORY_SELECTION
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetCreateScreen(navController: NavController, budgetId: String?) {

    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }

    val viewModel: BudgetCreateViewModel = hiltViewModel()

    var sheetSelection by remember { mutableStateOf(COLOR_SELECTION) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AppDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            onConfirmation = {
                viewModel.deleteBudget()
                showDeleteDialog = false
            },
            dialogTitle = stringResource(id = R.string.delete),
            dialogText = stringResource(id = R.string.delete_item_message),
            positiveButtonText = stringResource(id = R.string.delete),
            negativeButtonText = stringResource(id = R.string.cancel)
        )
    }

    val budgetCreated by viewModel.budgetUpdated.collectAsState(false)
    if (budgetCreated) {
        LaunchedEffect(key1 = "completed", block = {
            navController.popBackStack()
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.budget_create_success)
            )
        })
    }

    val errorMessage by viewModel.errorMessage.collectAsState(null)
    if (errorMessage != null) {
        LaunchedEffect(key1 = "errorMessage", block = {
            snackbarHostState.showSnackbar(
                message = errorMessage!!.asString(context)
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
            BudgetCreateBottomSheetContent(
                sheetSelection,
                viewModel
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
                title = stringResource(id = R.string.budgets),
                actionId = budgetId
            ) {
                showDeleteDialog = true
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::saveOrUpdateBudget) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_done),
                    contentDescription = ""
                )
            }
        }
    ) { innerPadding ->

        val name by viewModel.name.collectAsState()
        val nameErrorMessage by viewModel.nameErrorMessage.collectAsState()
        val amount by viewModel.amount.collectAsState()
        val amountErrorMessage by viewModel.amountErrorMessage.collectAsState()
        val currencyIcon by viewModel.currencyIcon.collectAsState()
        val colorValue by viewModel.colorValue.collectAsState()
        val iconValue by viewModel.icon.collectAsState()
        val selectedDate by viewModel.date.collectAsState()

        val accountCount by viewModel.accountCount.collectAsState()
        val categoriesCount by viewModel.categoriesCount.collectAsState()

        BudgetCreateScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            selectedColor = colorValue,
            selectedIcon = iconValue,
            name = name,
            nameErrorMessage = nameErrorMessage,
            amount = amount,
            amountErrorMessage = amountErrorMessage,
            selectedDate = selectedDate,
            currency = currencyIcon,
            accountCount = accountCount,
            categoriesCount = categoriesCount,
            onNameChange = viewModel::setNameChange,
            onAmountChange = viewModel::setAmountChange,
            onDateChange = viewModel::setDate,
            openColorPicker = {
                scope.launch {
                    val oldSelection = sheetSelection
                    if (oldSelection != COLOR_SELECTION) {
                        sheetSelection = COLOR_SELECTION
                    }
                    showBottomSheet = true
                }
            },
            openIconPicker = {
                scope.launch {
                    val oldSelection = sheetSelection
                    if (oldSelection != ICON_SELECTION) {
                        sheetSelection = ICON_SELECTION
                    }
                    showBottomSheet = true
                }
            },
            openAccountSelection = {
                scope.launch {
                    val oldSelection = sheetSelection
                    if (oldSelection != ACCOUNT_SELECTION) {
                        sheetSelection = ACCOUNT_SELECTION
                    }
                    showBottomSheet = true
                }
            },
            openCategorySelection = {
                scope.launch {
                    val oldSelection = sheetSelection
                    if (oldSelection != CATEGORY_SELECTION) {
                        sheetSelection = CATEGORY_SELECTION
                    }
                    showBottomSheet = true
                }
            }
        )
    }
}

@Composable
private fun BudgetCreateBottomSheetContent(
    sheetSelection: BudgetCreateSheetSelection,
    viewModel: BudgetCreateViewModel,
    hideBottomSheet: () -> Unit
) {
    val context = LocalContext.current

    when (sheetSelection) {
        ICON_SELECTION -> {
            IconSelectionScreen {
                viewModel.setIcon(context.resources.getResourceName(it))
                hideBottomSheet.invoke()
            }
        }

        COLOR_SELECTION -> {
            ColorSelectionScreen {
                viewModel.setColorValue(it)
                hideBottomSheet.invoke()
            }
        }

        ACCOUNT_SELECTION -> {
            com.naveenapps.expensemanager.feature.account.selection.MultipleAccountSelectionScreen { items, selected ->
                viewModel.setAccounts(items, selected)
                hideBottomSheet.invoke()
            }
        }

        CATEGORY_SELECTION -> {
            MultipleCategoriesSelectionScreen { items, selected ->
                viewModel.setCategories(items, selected)
                hideBottomSheet.invoke()
            }
        }
    }
}


@Composable
private fun BudgetCreateScreen(
    modifier: Modifier = Modifier,
    name: String = "",
    nameErrorMessage: UiText? = null,
    amount: String = "",
    amountErrorMessage: UiText? = null,
    currency: Int? = null,
    selectedColor: String = "#000000",
    selectedIcon: String = "savings",
    selectedDate: Date? = null,
    categoriesCount: UiText? = null,
    accountCount: UiText? = null,
    openIconPicker: (() -> Unit)? = null,
    openColorPicker: (() -> Unit)? = null,
    onNameChange: ((String) -> Unit)? = null,
    onAmountChange: ((String) -> Unit)? = null,
    onDateChange: ((Date) -> Unit)? = null,
    openAccountSelection: (() -> Unit)? = null,
    openCategorySelection: (() -> Unit)? = null,
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
                    shape = RoundedCornerShape(8.dp)
                ),
            currentMonth = (selectedDate ?: Date()).toMonth(),
            currentYear = (selectedDate ?: Date()).toYearInt(),
            confirmButtonCLicked = { month, year ->
                SimpleDateFormat("MM-yyyy", Locale.getDefault()).parse("${month}-${year}")?.let {
                    onDateChange?.invoke(
                        it
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
            value = selectedDate?.toMonthAndYear() ?: "",
            label = R.string.select_date,
            leadingIcon = Icons.Default.EditCalendar,
            onClick = {
                focusManager.clearFocus(force = true)
                showDatePicker = true
            }
        )

        StringTextField(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                .fillMaxWidth(),
            value = name,
            errorMessage = nameErrorMessage,
            onValueChange = onNameChange,
            label = R.string.budget_name
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
            value = amount,
            errorMessage = amountErrorMessage,
            onValueChange = onAmountChange,
            leadingIcon = currency,
            label = R.string.budget_amount,
        )

        Divider(
            modifier = Modifier.padding(top = 16.dp)
        )

        SelectedItemView(
            modifier = Modifier
                .clickable {
                    openAccountSelection?.invoke()
                }
                .padding(16.dp)
                .fillMaxWidth(),
            title = stringResource(id = R.string.select_account),
            icon = painterResource(id = com.naveenapps.expensemanager.core.designsystem.R.drawable.savings),
            selectedCount = accountCount?.asString(context)
                ?: stringResource(id = com.naveenapps.expensemanager.core.data.R.string.all_time)
        )

        SelectedItemView(
            modifier = Modifier
                .clickable {
                    openCategorySelection?.invoke()
                }
                .padding(16.dp)
                .fillMaxWidth(),
            title = stringResource(id = R.string.select_category),
            icon = painterResource(id = R.drawable.ic_filter_list),
            selectedCount = categoriesCount?.asString(context)
                ?: stringResource(id = com.naveenapps.expensemanager.core.data.R.string.all_time)
        )

        Divider()

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
    }
}

@Preview
@Composable
private fun BudgetCreateStatePreview() {
    ExpenseManagerTheme {
        BudgetCreateScreen(
            currency = com.naveenapps.expensemanager.core.common.R.drawable.currency_dollar
        )
    }
}