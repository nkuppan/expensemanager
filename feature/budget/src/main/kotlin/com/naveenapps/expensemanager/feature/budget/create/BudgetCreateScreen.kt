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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.common.utils.toMonth
import com.naveenapps.expensemanager.core.common.utils.toMonthAndYear
import com.naveenapps.expensemanager.core.common.utils.toYear
import com.naveenapps.expensemanager.core.common.utils.toYearInt
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountField
import com.naveenapps.expensemanager.core.designsystem.em.EmBottomActionBar
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmDeleteConfirm
import com.naveenapps.expensemanager.core.designsystem.em.EmDivider
import com.naveenapps.expensemanager.core.designsystem.em.EmIconButton
import com.naveenapps.expensemanager.core.designsystem.em.EmModalSheet
import com.naveenapps.expensemanager.core.designsystem.em.EmPickerField
import com.naveenapps.expensemanager.core.designsystem.em.EmPreviews
import com.naveenapps.expensemanager.core.designsystem.em.EmScaffold
import com.naveenapps.expensemanager.core.designsystem.em.EmSection
import com.naveenapps.expensemanager.core.designsystem.em.EmSegmented
import com.naveenapps.expensemanager.core.designsystem.em.EmSettingItem
import com.naveenapps.expensemanager.core.designsystem.em.EmSettingTrailing
import com.naveenapps.expensemanager.core.designsystem.em.EmSnackbarHostState
import com.naveenapps.expensemanager.core.designsystem.em.EmTextField
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBar
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBarNavigation
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.MonthPicker
import com.naveenapps.expensemanager.core.designsystem.ui.components.YearPicker
import com.naveenapps.expensemanager.core.designsystem.utils.ObserveAsEvents
import com.naveenapps.expensemanager.core.domain.usecase.budget.budgetTitle
import com.naveenapps.expensemanager.core.foundation.toMonthAndYearKey
import com.naveenapps.expensemanager.core.foundation.yearKeyToLocalDate
import com.naveenapps.expensemanager.core.model.BudgetPeriod
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.TextFieldValue
import com.naveenapps.expensemanager.feature.account.selection.MultipleAccountSelectionScreen
import com.naveenapps.expensemanager.feature.budget.R
import com.naveenapps.expensemanager.feature.budget.label
import com.naveenapps.expensemanager.feature.budget.text
import com.naveenapps.expensemanager.feature.category.selection.MultipleCategoriesSelectionScreen
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BudgetCreateScreen(
    viewModel: BudgetCreateViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { EmSnackbarHostState() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    ObserveAsEvents(viewModel.errorMessage) { messageRes ->
        coroutineScope.launch { snackbarHostState.showSnackbar(context.getString(messageRes)) }
    }

    BudgetCreateScreenContentView(
        state = state,
        onAction = viewModel::processAction,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
private fun BudgetCreateScreenContentView(
    state: BudgetCreateState,
    onAction: (BudgetCreateAction) -> Unit,
    snackbarHostState: EmSnackbarHostState = remember { EmSnackbarHostState() },
) {
    if (state.showDeleteDialog) {
        EmDeleteConfirm(
            onConfirm = {
                onAction.invoke(BudgetCreateAction.Delete)
            },
            onDismiss = {
                onAction.invoke(BudgetCreateAction.ClosePage)
            },
        )
    }

    if (state.showAccountSelectionDialog) {
        EmModalSheet(
            onDismissRequest = {
                onAction.invoke(BudgetCreateAction.CloseAccountSelectionDialog)
            },
        ) {
            MultipleAccountSelectionScreen(
                selectedAccounts = state.selectedAccounts,
                onItemSelection = { items, selected ->
                    onAction.invoke(BudgetCreateAction.SelectAccounts(selected, items))
                },
                onClose = { onAction.invoke(BudgetCreateAction.CloseAccountSelectionDialog) },
            )
        }
    }

    if (state.showCategorySelectionDialog) {
        EmModalSheet(
            onDismissRequest = {
                onAction.invoke(BudgetCreateAction.CloseCategorySelectionDialog)
            },
        ) {
            MultipleCategoriesSelectionScreen(
                selectedCategories = state.selectedCategories,
                onItemSelection = { items, selected ->
                    onAction.invoke(BudgetCreateAction.SelectCategories(selected, items))
                },
                onClose = { onAction.invoke(BudgetCreateAction.CloseCategorySelectionDialog) },
            )
        }
    }

    if (state.showMonthSelection) {
        if (state.periodType == BudgetPeriod.YEARLY) {
            YearPicker(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(EmTheme.spacing.lg)
                    .background(
                        color = EmTheme.colors.scheme.background,
                        shape = EmTheme.shapes.sm,
                    ),
                currentYear = state.month.value.toYearInt(),
                confirmButtonCLicked = { year ->
                    year.toString().yearKeyToLocalDate()?.atStartOfDayIn(TimeZone.currentSystemDefault())?.let {
                        state.month.onValueChange?.invoke(it)
                    } ?: run {
                        onAction.invoke(BudgetCreateAction.CloseMonthSelection)
                    }
                },
                cancelClicked = {
                    onAction.invoke(BudgetCreateAction.CloseMonthSelection)
                },
            )
        } else {
            MonthPicker(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(EmTheme.spacing.lg)
                    .background(
                        color = EmTheme.colors.scheme.background,
                        shape = EmTheme.shapes.sm,
                    ),
                currentMonth = state.month.value.toMonth(),
                currentYear = state.month.value.toYearInt(),
                confirmButtonCLicked = { month, year ->
                    runCatching { LocalDate(year, month, 1) }.getOrNull()?.atStartOfDayIn(TimeZone.currentSystemDefault())?.let {
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
    }

    EmScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            EmTopBar(
                navigation = EmTopBarNavigation.Back,
                onNavigationClick = {
                    onAction.invoke(BudgetCreateAction.ClosePage)
                },
                title = if (state.showDeleteButton) {
                    stringResource(R.string.edit_budget)
                } else {
                    stringResource(R.string.create_budget)
                },
                actions = {
                    if (state.showDeleteButton) {
                        EmIconButton(
                            icon = Icons.Outlined.Delete,
                            contentDescription = stringResource(com.naveenapps.expensemanager.feature.category.R.string.delete),
                            onClick = { onAction.invoke(BudgetCreateAction.ShowDeleteDialog) },
                            tint = EmTheme.colors.scheme.error,
                        )
                    }
                },
            )
        },
        bottomBar = {
            EmBottomActionBar(
                primaryText = stringResource(com.naveenapps.expensemanager.feature.account.R.string.save),
                onPrimaryClick = { onAction.invoke(BudgetCreateAction.Save) },
                primaryIcon = Icons.Default.Done,
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
            selectedDate = state.month,
            periodType = state.periodType,
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
    amountField: TextFieldValue<String>,
    nameField: TextFieldValue<String>,
    currencyIconField: String,
    selectedDate: TextFieldValue<Instant>,
    accountCount: String,
    categoriesCount: String,
    onAction: (BudgetCreateAction) -> Unit,
    modifier: Modifier = Modifier,
    periodType: BudgetPeriod = BudgetPeriod.MONTHLY,
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier.padding(horizontal = EmTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.lg),
    ) {
        EmSection(
            title = stringResource(R.string.budget_for),
            modifier = Modifier.padding(top = EmTheme.spacing.sm),
        ) {
            EmCard {
                Column(
                    modifier = Modifier.padding(EmTheme.spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.md),
                ) {
                    EmSegmented(
                        options = BudgetPeriod.entries,
                        selected = periodType,
                        onSelect = { onAction.invoke(BudgetCreateAction.SelectPeriodType(it)) },
                        label = { it.label() },
                    )
                    // Optional: left empty, the budget is called what the hint says ("This
                    // month's budget", "October 2026 budget", ...), following the period above.
                    val selectedPeriodKey = if (periodType == BudgetPeriod.YEARLY) {
                        selectedDate.value.toYear()
                    } else {
                        selectedDate.value.toMonthAndYearKey()
                    }
                    EmTextField(
                        value = nameField.value,
                        onValueChange = { nameField.onValueChange?.invoke(it) },
                        label = stringResource(id = R.string.budget_name_optional),
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = budgetTitle(name = null, selectedMonth = selectedPeriodKey, periodType = periodType).text(),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next,
                        ),
                        selectAllOnFocus = true,
                    )
                    EmPickerField(
                        label = stringResource(
                            id = if (periodType == BudgetPeriod.YEARLY) {
                                R.string.select_year
                            } else {
                                R.string.select_month
                            },
                        ),
                        value = if (periodType == BudgetPeriod.YEARLY) {
                            selectedDate.value.toYear()
                        } else {
                            selectedDate.value.toMonthAndYear()
                        },
                        onClick = {
                            focusManager.clearFocus(force = true)
                            onAction.invoke(BudgetCreateAction.ShowMonthSelection)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = Icons.Default.EditCalendar,
                    )
                }
            }
        }

        EmSection(title = stringResource(R.string.what_is_your_budget_limit)) {
            EmCard {
                Column(
                    modifier = Modifier.padding(EmTheme.spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
                ) {
                    EmAmountField(
                        value = amountField.value,
                        onValueChange = { amountField.onValueChange?.invoke(it) },
                        label = stringResource(id = R.string.budget_amount),
                        modifier = Modifier.fillMaxWidth(),
                        isError = amountField.valueError,
                        errorMessage = stringResource(id = R.string.budget_amount_error),
                        imeAction = ImeAction.Done,
                    )
                }
            }
        }

        EmSection(title = stringResource(R.string.budget_scope)) {
            EmCard {
                Column {
                    EmSettingItem(
                        icon = Icons.Default.AccountBalance,
                        title = stringResource(id = R.string.select_account),
                        trailing = EmSettingTrailing.valueOrChevron(accountCount),
                        onClick = {
                            onAction.invoke(BudgetCreateAction.OpenAccountSelectionDialog)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    EmDivider()
                    EmSettingItem(
                        icon = Icons.Default.Category,
                        title = stringResource(id = R.string.select_category),
                        trailing = EmSettingTrailing.valueOrChevron(categoriesCount),
                        onClick = {
                            onAction.invoke(BudgetCreateAction.OpenCategorySelectionDialog)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        // FAB clearance
        Spacer(modifier = Modifier.height(72.dp))
    }
}

@EmPreviews
@Composable
private fun BudgetCreateStatePreview() {
    val amountField = TextFieldValue(
        value = "0.0",
        valueError = false,
        onValueChange = { },
    )
    val dateField = TextFieldValue(
        value = Clock.System.now(),
        valueError = false,
        onValueChange = { },
    )
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        BudgetCreateScreenContentView(
            state = BudgetCreateState(
                isLoading = false,
                name = TextFieldValue(value = "", onValueChange = {}),
                amount = amountField,
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
