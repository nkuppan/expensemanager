package com.nkuppan.expensemanager.presentation.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.domain.model.TransactionUiItem
import com.nkuppan.expensemanager.domain.usecase.budget.BudgetUiModel
import com.nkuppan.expensemanager.presentation.account.list.ACCOUNT_DUMMY_DATA
import com.nkuppan.expensemanager.presentation.account.list.AccountUiModel
import com.nkuppan.expensemanager.presentation.account.list.DashBoardAccountItem
import com.nkuppan.expensemanager.presentation.budget.list.DashBoardBudgetItem
import com.nkuppan.expensemanager.presentation.category.list.getCategoryData
import com.nkuppan.expensemanager.presentation.category.transaction.CategoryTransaction
import com.nkuppan.expensemanager.presentation.category.transaction.CategoryTransactionUiModel
import com.nkuppan.expensemanager.presentation.category.transaction.getDummyPieChartData
import com.nkuppan.expensemanager.presentation.transaction.list.TransactionItem
import com.nkuppan.expensemanager.ui.extensions.toColor
import com.nkuppan.expensemanager.ui.theme.ExpenseManagerTheme
import com.nkuppan.expensemanager.ui.utils.ItemSpecModifier
import com.nkuppan.expensemanager.ui.utils.UiText
import kotlin.random.Random


@Composable
fun DashboardScreen(navController: NavController) {
    DashboardScreenScaffoldView(navController = navController)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardScreenScaffoldView(
    navController: NavController
) {
    val viewModel: DashboardViewModel = hiltViewModel()

    val amountUiState by viewModel.amountUiState.collectAsState()

    val accounts by viewModel.accounts.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val budgets by viewModel.budgets.collectAsState()
    val categoryTransaction by viewModel.categoryTransaction.collectAsState()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
        DashboardTopAppbar(navController)
    }) { innerPadding ->
        innerPadding.calculateTopPadding()
        DashboardScreenContent(
            navController = navController,
            amountUiState = amountUiState,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
            accounts = accounts,
            transactions = transactions,
            budgets = budgets,
            categoryTransaction = categoryTransaction
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DashboardTopAppbar(navController: NavController) {
    TopAppBar(title = {
        Row {
            Image(
                modifier = Modifier
                    .clickable(onClick = {

                    })
                    .size(32.dp)
                    .border(
                        1.5.dp, MaterialTheme.colorScheme.primary, CircleShape
                    )
                    .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .clip(CircleShape)
                    .align(Alignment.CenterVertically),
                painter = painterResource(id = R.drawable.someone_else),
                contentScale = ContentScale.Crop,
                contentDescription = null,
            )
            Column(
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.hello),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = stringResource(id = R.string.guest),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }, actions = {
        IconButton(onClick = {
            navController.navigate("settings")
        }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_settings),
                contentDescription = stringResource(id = R.string.settings)
            )
        }
    })
}

@Composable
private fun DashboardScreenContent(
    navController: NavController,
    amountUiState: AmountUiState,
    modifier: Modifier = Modifier,
    accounts: List<AccountUiModel> = emptyList(),
    transactions: List<TransactionUiItem> = emptyList(),
    budgets: List<BudgetUiModel> = emptyList(),
    categoryTransaction: CategoryTransactionUiModel,
) {

    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
    ) {
        item {
            FilterView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, end = 6.dp)
            )
        }
        item {
            IncomeExpenseBalanceView(
                amountUiState = amountUiState,
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
                title = stringResource(id = R.string.title_accounts),
                onViewAllClick = {
                    navController.navigate("account")
                },
            )
        }
        if (accounts.isNotEmpty()) {
            item(accounts) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(accounts) {
                        DashBoardAccountItem(
                            modifier = Modifier.wrapContentWidth(),
                            name = it.name,
                            icon = it.icon,
                            amount = it.amount,
                            amountTextColor = colorResource(id = it.amountTextColor),
                            backgroundColor = it.iconBackgroundColor.toColor().copy(alpha = .1f),
                        )
                    }
                }
            }
        } else {
            item {
                DashboardEmptyView(stringResource(id = R.string.no_transactions_available))
            }
        }
        item {
            CategoryAmountView(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                categoryTransactionUiModel = categoryTransaction
            )
        }
        item {
            DashboardWidgetTitle(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                title = stringResource(id = R.string.budgets),
                onViewAllClick = {
                    navController.navigate("budget")
                }
            )
        }
        if (budgets.isNotEmpty()) {
            item(budgets) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(budgets) { budget ->
                        DashBoardBudgetItem(
                            modifier = Modifier
                                .clickable {
                                    navController.navigate("budget/create?budgetId=${budget.id}")
                                },
                            name = budget.name,
                            backgroundColor = budget.iconBackgroundColor.toColor()
                                .copy(alpha = .1f),
                            progressBarColor = budget.progressBarColor,
                            amount = budget.amount.asString(context),
                            transactionAmount = budget.transactionAmount.asString(context),
                            percentage = budget.percent,
                        )
                    }
                }
            }
        } else {
            item {
                DashboardEmptyView(stringResource(id = R.string.no_budget_available))
            }
        }
        item {
            DashboardWidgetTitle(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                title = stringResource(id = R.string.transaction),
                onViewAllClick = {
                    navController.navigate("transaction")
                }
            )
        }
        if (transactions.isNotEmpty()) {
            items(transactions, key = {
                it.id
            }) { transaction ->
                TransactionItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate("transaction/create?transactionId=${transaction.id}")
                        }
                        .then(ItemSpecModifier),
                    categoryName = transaction.categoryName,
                    categoryColor = transaction.categoryBackgroundColor,
                    categoryIcon = transaction.categoryIcon,
                    amount = transaction.amount.asString(context),
                    date = transaction.date,
                    notes = transaction.notes,
                    transactionType = transaction.transactionType,
                    fromAccountName = transaction.fromAccountName,
                    fromAccountIcon = transaction.fromAccountIcon,
                    fromAccountColor = transaction.fromAccountColor,
                    toAccountName = transaction.toAccountName,
                    toAccountIcon = transaction.toAccountIcon,
                    toAccountColor = transaction.toAccountColor,
                )
            }
        } else {
            item {
                DashboardEmptyView(stringResource(id = R.string.no_transactions_available))
            }
        }
        item {
            Spacer(modifier = Modifier.padding(24.dp))
        }
    }
}

@Composable
fun DashboardWidgetTitle(
    title: String, modifier:
    Modifier = Modifier,
    onViewAllClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
    ) {
        Text(
            text = title, style = MaterialTheme.typography.headlineSmall
        )
        if (onViewAllClick != null) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable { onViewAllClick.invoke() },
                text = stringResource(id = R.string.view_all).uppercase(),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun DashboardEmptyView(emptyViewMessage: String) {
    Box(
        modifier = Modifier
            .height(200.dp)
            .fillMaxWidth()
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center), text = emptyViewMessage
        )
    }
}


@Preview
@Composable
fun IncomeExpenseBalanceViewPreview() {
    ExpenseManagerTheme {
        DashboardScreenContent(
            navController = rememberNavController(),
            amountUiState = AmountUiState(),
            modifier = Modifier.fillMaxSize(),
            accounts = ACCOUNT_DUMMY_DATA,
            categoryTransaction = CategoryTransactionUiModel(
                pieChartData = listOf(
                    getDummyPieChartData("", 25.0f),
                    getDummyPieChartData("", 25.0f),
                    getDummyPieChartData("", 25.0f),
                    getDummyPieChartData("", 25.0f)
                ),
                totalAmount = UiText.DynamicString("Expenses"),
                categoryTransactions = buildList {
                    repeat(15) {
                        add(
                            CategoryTransaction(
                                category = getCategoryData(it),
                                amount = UiText.DynamicString("100$"),
                                percent = Random(100).nextFloat(),
                                transaction = emptyList()
                            )
                        )
                    }
                }.take(5)
            )
        )
    }
}