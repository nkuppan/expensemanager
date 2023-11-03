package com.nkuppan.expensemanager.presentation.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import com.nkuppan.expensemanager.domain.model.TransactionUIModel
import com.nkuppan.expensemanager.domain.usecase.transaction.AnalysisChartData
import com.nkuppan.expensemanager.presentation.account.list.ACCOUNT_DUMMY_DATA
import com.nkuppan.expensemanager.presentation.account.list.AccountUiModel
import com.nkuppan.expensemanager.presentation.account.list.DashBoardAccountItem
import com.nkuppan.expensemanager.presentation.analysis.ChartScreen
import com.nkuppan.expensemanager.presentation.category.list.getCategoryData
import com.nkuppan.expensemanager.presentation.category.transaction.CategoryTransaction
import com.nkuppan.expensemanager.presentation.category.transaction.CategoryTransactionUiModel
import com.nkuppan.expensemanager.presentation.category.transaction.getDummyPieChartData
import com.nkuppan.expensemanager.presentation.transaction.list.TransactionItem
import com.nkuppan.expensemanager.ui.extensions.toColor
import com.nkuppan.expensemanager.ui.theme.ExpenseManagerTheme
import com.nkuppan.expensemanager.ui.utils.ItemSpecModifier
import com.nkuppan.expensemanager.ui.utils.UiText
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf
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

    val incomeAmount by viewModel.incomeAmountValue.collectAsState()
    val expenseAmount by viewModel.expenseAmountValue.collectAsState()
    val balanceAmount by viewModel.totalIncomeValue.collectAsState()
    val transactionPeriod by viewModel.transactionPeriod.collectAsState()

    val chartData by viewModel.chartData.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val categoryTransaction by viewModel.categoryTransaction.collectAsState()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            DashboardTopAppbar(navController)
        }
    ) { innerPadding ->
        innerPadding.calculateTopPadding()
        DashboardScreenContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
            navController = navController,
            incomeAmount = incomeAmount,
            expenseAmount = expenseAmount,
            balanceAmount = balanceAmount,
            transactionPeriod = transactionPeriod,
            chartData = chartData,
            accounts = accounts,
            transactions = transactions,
            categoryTransaction = categoryTransaction
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DashboardTopAppbar(navController: NavController) {
    TopAppBar(
        title = {
            Row {
                Image(
                    modifier = Modifier
                        .clickable(
                            onClick = {

                            }
                        )
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
        },
        actions = {
            IconButton(onClick = {
                navController.navigate("settings")
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = stringResource(id = R.string.settings)
                )
            }
        }
    )
}

@Composable
private fun DashboardScreenContent(
    navController: NavController,
    incomeAmount: UiText,
    expenseAmount: UiText,
    balanceAmount: UiText,
    transactionPeriod: UiText,
    chartData: AnalysisChartData,
    modifier: Modifier = Modifier,
    accounts: List<AccountUiModel> = emptyList(),
    transactions: List<TransactionUIModel> = emptyList(),
    categoryTransaction: CategoryTransactionUiModel,
) {

    val context = LocalContext.current

    LazyColumn(modifier = modifier) {
        item {
            IncomeExpenseBalanceView(
                incomeAmount = incomeAmount,
                expenseAmount = expenseAmount,
                balanceAmount = balanceAmount,
                transactionPeriod = transactionPeriod,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            )
        }
        item(accounts) {
            DashboardWidgetTitle(
                title = stringResource(id = R.string.title_accounts),
                onViewAllClick = {
                    navController.navigate("account")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
            )
        }
        item {
            if (accounts.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .padding(top = 16.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(accounts) {
                        DashBoardAccountItem(
                            modifier = Modifier
                                .wrapContentWidth(),
                            name = it.name,
                            icon = it.icon,
                            amount = it.amount,
                            amountTextColor = if (it.isDeclining) {
                                colorResource(id = R.color.red_500)
                            } else {
                                colorResource(id = R.color.green_500)
                            },
                            backgroundColor = it.iconBackgroundColor.toColor().copy(alpha = .1f),
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .height(200.dp)
                        .fillParentMaxWidth()
                ) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = stringResource(id = R.string.no_account_available)
                    )
                }
            }
        }
        item {
            CategoryAmountView(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                transactionPeriod = transactionPeriod,
                categoryTransactionUiModel = categoryTransaction
            )
        }
        item {
            Surface(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                shape = RoundedCornerShape(8.dp),
                tonalElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    WidgetHeader(
                        title = stringResource(id = R.string.analysis),
                        subTitle = transactionPeriod.asString(context)
                    )
                    ChartScreen(chart = chartData)
                }
            }
        }
        item {
            DashboardWidgetTitle(
                title = stringResource(id = R.string.transaction),
                onViewAllClick = {
                    navController.navigate("transaction")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
            )
        }
        items(transactions) { transaction ->
            TransactionItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        //onItemClick?.invoke(transaction)
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
    }
}

@Composable
private fun DashboardWidgetTitle(
    title: String,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .clickable { onViewAllClick.invoke() },
            text = stringResource(id = R.string.view_all).uppercase(),
            style = MaterialTheme.typography.labelMedium
        )
    }
}


@Preview
@Composable
fun IncomeExpenseBalanceViewPreview() {
    ExpenseManagerTheme {
        DashboardScreenContent(
            navController = rememberNavController(),
            incomeAmount = UiText.DynamicString("$200.0"),
            expenseAmount = UiText.DynamicString("$500.0"),
            balanceAmount = UiText.DynamicString("$300.0"),
            transactionPeriod = UiText.DynamicString("This Month(Oct 2023)"),
            chartData = AnalysisChartData(
                chartData = entryModelOf(
                    listOf(entryOf(0, 1), entryOf(1, 2), entryOf(2, 3)),
                    listOf(entryOf(0, 4), entryOf(1, 3), entryOf(2, 2)),
                ), dates = listOf(
                    "08/09",
                    "18/09",
                    "21/09",
                    "24/09",
                    "28/09",
                    "18/09",
                    "21/09",
                    "24/09",
                    "28/09",
                    "28/09"
                )
            ),
            accounts = ACCOUNT_DUMMY_DATA,
            modifier = Modifier.fillMaxSize(),
            categoryTransaction = CategoryTransactionUiModel(
                pieChartData = listOf(
                    getDummyPieChartData(""),
                    getDummyPieChartData(""),
                    getDummyPieChartData(""),
                    getDummyPieChartData("")
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