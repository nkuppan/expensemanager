package com.naveenapps.expensemanager.feature.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naveenapps.expensemanager.core.designsystem.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.components.AmountStatusView
import com.naveenapps.expensemanager.core.designsystem.components.DashboardWidgetTitle
import com.naveenapps.expensemanager.core.designsystem.components.EmptyItem
import com.naveenapps.expensemanager.core.designsystem.ui.extensions.toColor
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.designsystem.ui.utils.ItemSpecModifier
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

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
) {

    val state by viewModel.state.collectAsState()

    DashboardScaffoldContent(
        state = state,
        onAction = viewModel::processAction
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
            TopAppBar(title = {
                Text(
                    text = stringResource(id = R.string.home),
                    style = MaterialTheme.typography.titleLarge,
                )
            }, actions = {
                IconButton(
                    onClick = {
                        onAction.invoke(DashboardAction.OpenSettings)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = stringResource(id = R.string.settings),
                    )
                }
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                onAction.invoke(
                    DashboardAction.OpenTransactionEdit(
                        null
                    )
                )
            }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "",
                )
            }
        },
    ) { innerPadding ->
        DashboardScreenContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
            state = state,
            onAction = onAction
        )
    }
}

@Composable
private fun DashboardScreenContent(
    modifier: Modifier = Modifier,
    state: DashboardState,
    onAction: (DashboardAction) -> Unit,
) {
    LazyColumn(modifier = modifier) {
        item {
            FilterView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 6.dp),
            )
        }
        item {
            IncomeExpenseBalanceView(
                expenseFlowState = state.expenseFlowState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            )
        }
        item {
            DashboardWidgetTitle(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                title = stringResource(id = com.naveenapps.expensemanager.feature.account.R.string.accounts),
                onViewAllClick = {
                    onAction.invoke(DashboardAction.OpenAccountList)
                },
            )
        }
        if (state.accounts.isNotEmpty()) {
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(state.accounts, key = { it.id }) {
                        DashBoardAccountItem(
                            modifier = Modifier
                                .wrapContentWidth()
                                .clickable {
                                    onAction.invoke(DashboardAction.OpenAccountEdit(it))
                                },
                            name = it.name,
                            icon = it.storedIcon.name,
                            amount = it.amount.amountString ?: "",
                            availableCreditLimit = it.availableCreditLimit?.amountString ?: "",
                            amountTextColor = colorResource(id = it.amountTextColor),
                            backgroundColor = it.storedIcon.backgroundColor.toColor()
                                .copy(alpha = .1f),
                        )
                    }
                }
            }
        } else {
            item {
                EmptyItem(
                    emptyItemText = stringResource(id = com.naveenapps.expensemanager.feature.account.R.string.no_account_available_short),
                    icon = com.naveenapps.expensemanager.core.designsystem.R.drawable.ic_no_accounts,
                    modifier = Modifier
                        .fillMaxSize()
                        .height(200.dp)
                )
            }
        }
        item {
            CategoryAmountView(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                categoryTransactionState = state.categoryTransaction,
            )
        }
        item {
            DashboardWidgetTitle(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                title = stringResource(id = com.naveenapps.expensemanager.feature.budget.R.string.budgets),
                onViewAllClick = {
                    onAction.invoke(DashboardAction.OpenBudgetList)
                },
            )
        }
        if (state.budgets.isNotEmpty()) {
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(state.budgets, key = { it.id }) { budget ->
                        DashBoardBudgetItem(
                            modifier = Modifier
                                .clickable {
                                    onAction.invoke(DashboardAction.OpenBudgetDetails(budget))
                                },
                            name = budget.name,
                            backgroundColor = budget.iconBackgroundColor.toColor()
                                .copy(alpha = .1f),
                            progressBarColor = budget.progressBarColor,
                            amount = budget.amount.amountString,
                            transactionAmount = budget.transactionAmount.amountString,
                            percentage = budget.percent,
                        )
                    }
                }
            }
        } else {
            item {
                EmptyItem(
                    emptyItemText = stringResource(id = com.naveenapps.expensemanager.feature.budget.R.string.no_budget_available_short),
                    icon = com.naveenapps.expensemanager.core.designsystem.R.drawable.ic_no_budgets,
                    modifier = Modifier
                        .fillMaxSize()
                        .height(200.dp)
                )
            }
        }
        item {
            DashboardWidgetTitle(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                title = stringResource(id = R.string.transaction),
                onViewAllClick = {
                    onAction.invoke(DashboardAction.OpenTransactionList)
                },
            )
        }
        if (state.transactions.isNotEmpty()) {
            items(state.transactions, key = { it.id }) { transaction ->
                TransactionItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onAction.invoke(DashboardAction.OpenTransactionEdit(transaction))
                        }
                        .then(ItemSpecModifier),
                    categoryName = transaction.categoryName,
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
        } else {
            item {
                EmptyItem(
                    emptyItemText = stringResource(id = R.string.no_transactions_available),
                    icon = com.naveenapps.expensemanager.core.designsystem.R.drawable.ic_no_transaction,
                    modifier = Modifier
                        .fillMaxSize()
                        .height(200.dp)
                )
            }
        }
        item {
            Spacer(modifier = Modifier.padding(64.dp))
        }
    }
}

@Composable
fun IncomeExpenseBalanceView(
    expenseFlowState: ExpenseFlowState,
    modifier: Modifier = Modifier,
    showBalance: Boolean = false,
) {
    AmountStatusView(
        modifier = modifier,
        expenseAmount = expenseFlowState.expense,
        incomeAmount = expenseFlowState.income,
        balanceAmount = expenseFlowState.balance,
        showBalance = showBalance,
    )
}

@AppPreviewsLightAndDarkMode
@Composable
fun DashboardScaffoldContentPreview() {
    ExpenseManagerTheme {
        DashboardScaffoldContent(
            state = DashboardState(
                expenseFlowState = ExpenseFlowState(),
                accounts = getRandomAccountUiModel(5),
                categoryTransaction = CategoryTransactionState(
                    pieChartData = listOf(
                        getDummyPieChartData("", 25.0f),
                        getDummyPieChartData("", 25.0f),
                        getDummyPieChartData("", 25.0f),
                        getDummyPieChartData("", 25.0f)
                    ),
                    totalAmount = Amount(0.0, "Expenses"),
                    categoryTransactions = buildList {
                        repeat(15) {
                            add(
                                CategoryTransaction(
                                    category = getCategoryData(
                                        it, CategoryType.EXPENSE
                                    ),
                                    amount = Amount(0.0, "100.00$"),
                                    percent = Random(100).nextFloat(),
                                    transaction = emptyList()
                                )
                            )
                        }
                    }.take(5)
                ),
                budgets = getRandomBudgetUiModel(5),
                transactions = listOf(getTransactionItem()),
            ),
            onAction = {},
        )
    }
}
