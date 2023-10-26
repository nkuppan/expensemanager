package com.nkuppan.expensemanager.presentation.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.presentation.account.list.AccountItem
import com.nkuppan.expensemanager.presentation.account.list.AccountUiModel
import com.nkuppan.expensemanager.presentation.analysis.AnalysisChartData
import com.nkuppan.expensemanager.presentation.analysis.ChartScreen
import com.nkuppan.expensemanager.presentation.category.transaction.CategoryTransactionUiModel
import com.nkuppan.expensemanager.ui.theme.ExpenseManagerTheme
import com.nkuppan.expensemanager.ui.theme.widget.AppCardView
import com.nkuppan.expensemanager.ui.utils.UiText
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf


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
    val categoryTransaction by viewModel.categoryTransaction.collectAsState()

    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.title_home),
                    style = MaterialTheme.typography.titleLarge
                )
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
    }) { innerPadding ->
        DashboardScreenContent(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            incomeAmount = incomeAmount,
            expenseAmount = expenseAmount,
            balanceAmount = balanceAmount,
            transactionPeriod = transactionPeriod,
            chartData = chartData,
            accounts = accounts,
            categoryTransaction = categoryTransaction,
        )
    }
}

@Composable
private fun DashboardScreenContent(
    incomeAmount: UiText,
    expenseAmount: UiText,
    balanceAmount: UiText,
    transactionPeriod: UiText,
    chartData: AnalysisChartData?,
    modifier: Modifier = Modifier,
    accounts: List<AccountUiModel> = emptyList(),
    categoryTransaction: CategoryTransactionUiModel? = null,
) {

    val context = LocalContext.current

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            IncomeExpenseBalanceView(
                incomeAmount = incomeAmount,
                expenseAmount = expenseAmount,
                balanceAmount = balanceAmount,
                transactionPeriod = transactionPeriod,
            )
        }
        item {
            if (categoryTransaction != null) {
                CategoryAmountView(
                    modifier = Modifier.padding(16.dp),
                    transactionPeriod = transactionPeriod,
                    categoryTransactionUiModel = categoryTransaction
                )
            }
        }
        item {
            if (chartData != null) {
                AppCardView(modifier = Modifier.padding(16.dp)) {
                    Column(
                        modifier = Modifier.padding(8.dp),
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
        }
        item {
            Text(
                modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
                text = stringResource(id = R.string.title_accounts),
                fontWeight = FontWeight.Bold
            )
        }
        items(accounts) { account ->
            AccountItem(modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    //onItemClick?.invoke(account)
                }
                .padding(16.dp),
                name = account.name,
                icon = account.icon,
                iconBackgroundColor = account.iconBackgroundColor,
                amount = account.amount.asString(context))
        }
    }
}

@Preview
@Composable
fun IncomeExpenseBalanceViewPreview() {
    ExpenseManagerTheme {
        DashboardScreenContent(
            modifier = Modifier.fillMaxSize(),
            expenseAmount = UiText.DynamicString("$500.0"),
            incomeAmount = UiText.DynamicString("$200.0"),
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
            )
        )
    }
}