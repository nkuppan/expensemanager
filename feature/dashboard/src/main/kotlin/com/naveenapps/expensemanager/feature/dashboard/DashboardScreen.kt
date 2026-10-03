package com.naveenapps.expensemanager.feature.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.components.AmountInfoWidget
import com.naveenapps.expensemanager.core.designsystem.components.AmountInfoWidgetCompact
import com.naveenapps.expensemanager.core.designsystem.components.DashboardWidgetTitle
import com.naveenapps.expensemanager.core.designsystem.components.EmptyItem
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardViewDefaults
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.CategoryTransaction
import com.naveenapps.expensemanager.core.model.CategoryTransactionState
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.ExpenseFlowState
import com.naveenapps.expensemanager.core.model.getDummyPieChartData
import com.naveenapps.expensemanager.feature.account.list.DashBoardAccountItem
import com.naveenapps.expensemanager.feature.account.list.getRandomAccountUiModel
import com.naveenapps.expensemanager.feature.budget.list.DashBoardBudgetItem
import com.naveenapps.expensemanager.feature.budget.list.getRandomBudgetUiModel
import com.naveenapps.expensemanager.feature.category.list.getCategoryData
import com.naveenapps.expensemanager.feature.filter.FilterView
import com.naveenapps.expensemanager.feature.transaction.list.TransactionItem
import com.naveenapps.expensemanager.feature.transaction.list.getTransactionItem
import kotlin.random.Random
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    DashboardScaffoldContent(
        state = state,
        onAction = viewModel::processAction,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DashboardScaffoldContent(
    state: DashboardState,
    onAction: (DashboardAction) -> Unit,
) {
    Scaffold(
        topBar = {
            ExpenseManagerTopAppBar(
                title = stringResource(id = R.string.home),
                actions = {
                    IconButton(
                        onClick = {
                            onAction.invoke(DashboardAction.OpenSettings)
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(id = R.string.settings),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        DashboardScreenContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
            state = state,
            onAction = onAction,
        )
    }
}

@Composable
private fun DashboardScreenContent(
    modifier: Modifier = Modifier,
    state: DashboardState,
    onAction: (DashboardAction) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 78.dp),
    ) {
        item {
            FilterView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 6.dp),
            )
        }
        item {
            GettingStartedSection(state = state, onAction = onAction)
        }
        item("summary") {
            IncomeExpenseBalanceView(
                transactionPeriod = state.transactionPeriod,
                expenseFlowState = state.expenseFlowState,
                isCompact = state.isCompactSummary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            )
        }

        item("account_item") {
            Column {
                DashboardWidgetTitle(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                    title = stringResource(id = com.naveenapps.expensemanager.feature.account.R.string.accounts),
                    onViewAllClick = {
                        onAction.invoke(DashboardAction.OpenAccountList)
                    },
                )
                if (state.accounts.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(state.accounts, key = { it.id }) {
                            DashBoardAccountItem(
                                modifier = Modifier
                                    .wrapContentWidth(),
                                name = it.name,
                                icon = it.storedIcon.name,
                                customImagePath = it.storedIcon.customImagePath,
                                amount = it.amount.amountString ?: "",
                                availableCreditLimit = it.availableCreditLimit?.amountString
                                    ?: "",
                                amountTextColor = colorResource(id = it.amountTextColor),
                                onItemClick = {
                                    onAction.invoke(DashboardAction.OpenAccountEdit(it))
                                },
                            )
                        }
                    }
                } else {
                    EmptyItem(
                        emptyItemText = stringResource(id = com.naveenapps.expensemanager.feature.account.R.string.no_account_available_short),
                        icon = com.naveenapps.expensemanager.core.designsystem.R.drawable.ic_no_accounts,
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.CenterHorizontally),
                    )
                }
            }
        }

        item("category_item") {
            DashboardWidgetTitle(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp),
                title = stringResource(id = R.string.categories),
            )
            AppCardView(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            ) {
                CategoryAmountView(
                    modifier = Modifier.padding(16.dp),
                    categoryTransactionState = state.categoryTransactionState,
                )
            }
        }
        item("budget_item") {
            Column {
                DashboardWidgetTitle(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                    title = stringResource(id = com.naveenapps.expensemanager.feature.budget.R.string.active_budgets),
                    onViewAllClick = {
                        onAction.invoke(DashboardAction.OpenBudgetList)
                    },
                )
                if (state.budgets.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(state.budgets, key = { it.id }) { budget ->
                            DashBoardBudgetItem(
                                modifier = Modifier,
                                name = budget.name,
                                progressBarColor = budget.progressBarColor,
                                amount = budget.amount.amountString,
                                transactionAmount = budget.transactionAmount.amountString,
                                percentage = budget.percent,
                                onItemClick = {
                                    onAction.invoke(DashboardAction.OpenBudgetDetails(budget))
                                },
                            )
                        }
                    }
                } else if (state.showCreateBudgetForMonth != null) {
                    AppCardView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        ),
                        onClick = { onAction.invoke(DashboardAction.OpenBudgetCreate) },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = stringResource(
                                        id = R.string.no_budget_for_month,
                                        state.showCreateBudgetForMonth,
                                    ),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = stringResource(id = R.string.tap_to_set_spending_limit),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                        .copy(alpha = 0.7f),
                                )
                            }
                        }
                    }
                } else {
                    EmptyItem(
                        emptyItemText = stringResource(id = com.naveenapps.expensemanager.feature.budget.R.string.no_budget_available_short),
                        icon = com.naveenapps.expensemanager.core.designsystem.R.drawable.ic_no_budgets,
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.CenterHorizontally),
                    )
                }
            }
        }

        item {
            DashboardWidgetTitle(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp),
                title = stringResource(id = R.string.transaction),
                onViewAllClick = {
                    onAction.invoke(DashboardAction.OpenTransactionList)
                },
            )
        }
        if (state.transactions.isNotEmpty()) {
            itemsIndexed(
                items = state.transactions,
                key = { _, item -> item.id },
            ) { index, transaction ->
                AppCardView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    shape = AppCardViewDefaults.cardShape(index, state.transactions),
                ) {
                    TransactionItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onAction.invoke(DashboardAction.OpenTransactionEdit(transaction))
                            },
                        categoryName = transaction.categoryTitleResId?.let { stringResource(it) }
                            ?: transaction.categoryName,
                        categoryColor = transaction.categoryIcon.backgroundColor,
                        categoryIcon = transaction.categoryIcon.name,
                        amount = transaction.amount,
                        date = transaction.date,
                        notes = transaction.notes,
                        transactionType = transaction.transactionType,
                        fromAccountName = transaction.fromAccountName,
                        fromAccountIcon = transaction.fromAccountIcon.name,
                        fromAccountColor = transaction.fromAccountIcon.backgroundColor,
                        toAccountName = transaction.toAccountName,
                        toAccountIcon = transaction.toAccountIcon?.name,
                        toAccountColor = transaction.toAccountIcon?.backgroundColor,
                    )
                }
            }
        } else {
            item {
                EmptyItem(
                    emptyItemText = stringResource(id = R.string.no_transactions_available),
                    icon = com.naveenapps.expensemanager.core.designsystem.R.drawable.ic_no_transaction,
                    modifier = Modifier
                        .fillMaxSize()
                        .height(320.dp)
                        .padding(horizontal = 16.dp),
                )
            }
        }
    }
}

@Composable
fun IncomeExpenseBalanceView(
    expenseFlowState: ExpenseFlowState,
    transactionPeriod: String,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
) {
    if (isCompact) {
        AmountInfoWidgetCompact(
            modifier = modifier,
            expenseAmount = expenseFlowState.expense,
            incomeAmount = expenseFlowState.income,
            balanceAmount = expenseFlowState.balance,
            transactionPeriod = transactionPeriod,
        )
    } else {
        AmountInfoWidget(
            modifier = modifier,
            expenseAmount = expenseFlowState.expense,
            incomeAmount = expenseFlowState.income,
            balanceAmount = expenseFlowState.balance,
            transactionPeriod = transactionPeriod,
        )
    }
}

@AppPreviewsLightAndDarkMode
@Composable
fun DashboardScaffoldContentPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        DashboardScaffoldContent(
            state = DashboardState(
                expenseFlowState = ExpenseFlowState(),
                accounts = getRandomAccountUiModel(5),
                categoryTransactionState = CategoryTransactionState(
                    pieChartData = listOf(
                        getDummyPieChartData("", 25.0f),
                        getDummyPieChartData("", 25.0f),
                        getDummyPieChartData("", 25.0f),
                        getDummyPieChartData("", 25.0f),
                    ),
                    totalAmount = Amount(0.0, "Expenses"),
                    categoryTransactions = buildList {
                        repeat(15) {
                            add(
                                CategoryTransaction(
                                    category = getCategoryData(
                                        it,
                                        CategoryType.EXPENSE,
                                    ),
                                    amount = Amount(0.0, "100.00$"),
                                    percent = Random(100).nextFloat(),
                                    transaction = emptyList(),
                                ),
                            )
                        }
                    }.take(5),
                    categoryType = CategoryType.EXPENSE,
                ),
                budgets = getRandomBudgetUiModel(5),
                transactions = listOf(
                    getTransactionItem("1"),
                    getTransactionItem("2"),
                    getTransactionItem("3"),
                ),
                transactionPeriod = "This month (Feb 2026)",
            ),
            onAction = {},
        )
    }
}

// Only new users get the checklist; long-time users (and anyone who closed it) never see it.
private const val GETTING_STARTED_MAX_TRANSACTIONS = 10

// Display order of the "Get started" checklist.
private enum class SetupStep { FirstExpense, Accounts, Budget, Currency }

@Composable
private fun GettingStartedSection(
    state: DashboardState,
    onAction: (DashboardAction) -> Unit,
) {
    val done = mapOf(
        SetupStep.FirstExpense to state.hasCreatedTransaction,
        SetupStep.Accounts to state.accounts.isNotEmpty(),
        SetupStep.Budget to state.hasCurrentMonthBudget,
        // A currency is always chosen during setup; the row stays as a reminder it can be changed.
        SetupStep.Currency to true,
    )
    val visible = !state.isGettingStartedDismissed &&
        state.transactionCount < GETTING_STARTED_MAX_TRANSACTIONS &&
        done.values.any { !it }
    if (!visible) return

    GettingStartedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        done = done,
        onStepClick = { step ->
            when (step) {
                SetupStep.FirstExpense -> onAction(DashboardAction.OpenTransactionEdit(null))
                SetupStep.Accounts -> onAction(DashboardAction.OpenAccountList)
                SetupStep.Budget -> onAction(DashboardAction.OpenBudgetCreate)
                SetupStep.Currency -> onAction(DashboardAction.OpenCurrency)
            }
        },
        onDismiss = { onAction(DashboardAction.DismissGettingStarted) },
    )
}

/**
 * "Get started" checklist on Home for new users: a progress bar plus the setup steps, each
 * ticked off automatically. The next unfinished step is expanded with its own button, so
 * there's always one obvious thing to do. Closable; disappears once every step is done.
 */
@Composable
private fun GettingStartedCard(
    done: Map<SetupStep, Boolean>,
    onStepClick: (SetupStep) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val steps = SetupStep.entries
    val completed = steps.count { done[it] == true }
    val nextStep = steps.firstOrNull { done[it] != true }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(modifier = Modifier.padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 8.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.getting_started_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(id = R.string.getting_started_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(id = R.string.getting_started_hide),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.padding(end = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LinearProgressIndicator(
                    progress = { completed / steps.size.toFloat() },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer,
                    strokeCap = StrokeCap.Round,
                    gapSize = 0.dp,
                    drawStopIndicator = {},
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(id = R.string.getting_started_progress, completed, steps.size),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            steps.forEachIndexed { index, step ->
                SetupStepRow(
                    number = index + 1,
                    step = step,
                    isDone = done[step] == true,
                    isNext = step == nextStep,
                    onClick = { onStepClick(step) },
                )
            }
        }
    }
}

@Composable
private fun SetupStepRow(
    number: Int,
    step: SetupStep,
    isDone: Boolean,
    isNext: Boolean,
    onClick: () -> Unit,
) {
    val (title, description, action) = when (step) {
        SetupStep.FirstExpense -> Triple(
            R.string.step_expense_title,
            R.string.step_expense_description,
            R.string.first_expense_action,
        )

        SetupStep.Accounts -> Triple(
            R.string.step_accounts_title,
            R.string.step_accounts_description,
            R.string.step_accounts_action,
        )

        SetupStep.Budget -> Triple(
            R.string.step_budget_title,
            R.string.step_budget_description,
            R.string.step_budget_action,
        )

        SetupStep.Currency -> Triple(
            R.string.step_currency_title,
            R.string.step_currency_description,
            R.string.step_currency_action,
        )
    }

    Surface(
        // Done rows stay tappable, e.g. to change the currency or edit accounts later.
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isNext) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 12.dp, top = 6.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            StepBadge(number = number, isDone = isDone, isNext = isNext)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = title),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isNext) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isDone) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    textDecoration = if (isDone) TextDecoration.LineThrough else null,
                )
                if (!isDone) {
                    Text(
                        text = stringResource(id = description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (isNext && action != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        if (step == SetupStep.FirstExpense) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(text = stringResource(id = action), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            if (!isDone && !isNext) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterVertically),
                )
            }
        }
    }
}

/** Green check when done, a filled number for the next step, an outlined number otherwise. */
@Composable
private fun StepBadge(number: Int, isDone: Boolean, isNext: Boolean) {
    val primary = MaterialTheme.colorScheme.primary
    Surface(
        modifier = Modifier.size(28.dp),
        shape = CircleShape,
        color = if (isDone || isNext) primary else Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        border = if (isDone || isNext) null else BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isDone) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            } else {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isNext) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

@AppPreviewsLightAndDarkMode
@Composable
private fun GettingStartedCardPreview() {
    ExpenseManagerPreviewTheme(padding = 16.dp) {
        GettingStartedCard(
            done = mapOf(
                SetupStep.FirstExpense to false,
                SetupStep.Accounts to true,
                SetupStep.Budget to false,
                SetupStep.Currency to true,
            ),
            onStepClick = {},
            onDismiss = {},
        )
    }
}
