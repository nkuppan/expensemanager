package com.nkuppan.expensemanager.presentation.dashboard

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.core.ui.theme.ExpenseManagerTheme
import com.nkuppan.expensemanager.core.ui.utils.UiText
import com.nkuppan.expensemanager.presentation.account.list.AccountItem
import com.nkuppan.expensemanager.presentation.account.list.AccountUiModel
import com.nkuppan.expensemanager.presentation.analysis.AnalysisChartData
import com.nkuppan.expensemanager.presentation.analysis.expense.ChartScreen
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf


@Composable
fun DashboardScreen(navController: NavController) {
    DashboardScreenScaffoldView(navController = navController)
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DashboardScreenScaffoldView(
    navController: NavController
) {
    val viewModel: DashboardViewModel = hiltViewModel()

    val incomeAmount by viewModel.incomeAmountValue.collectAsState()
    val expenseAmount by viewModel.expenseAmountValue.collectAsState()
    val balanceAmount by viewModel.totalIncomeValue.collectAsState()

    val chartData by viewModel.chartData.collectAsState()
    val accounts by viewModel.accounts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.title_home))
                }
            )
        }
    ) { innerPadding ->
        DashboardScreenContent(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            incomeAmount = incomeAmount,
            expenseAmount = expenseAmount,
            balanceAmount = balanceAmount,
            chartData = chartData,
            accounts = accounts,
        )
    }
}

@Composable
private fun DashboardScreenContent(
    incomeAmount: UiText,
    expenseAmount: UiText,
    balanceAmount: UiText,
    chartData: AnalysisChartData?,
    modifier: Modifier = Modifier,
    accounts: List<AccountUiModel> = emptyList(),
) {

    val context = LocalContext.current

    LazyColumn(modifier = modifier) {
        item {
            IncomeExpenseBalanceView(
                incomeAmount = incomeAmount,
                expenseAmount = expenseAmount,
                balanceAmount = balanceAmount
            )
        }
        item {
            if (chartData != null) {
                Column(
                    modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.this_month),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Card(
                        modifier = Modifier.padding(top = 8.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.elevatedCardColors()
                    ) {
                        ChartScreen(
                            modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                            chart = chartData
                        )
                    }
                }
            }
        }
        item {
            Text(
                modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
                text = stringResource(id = R.string.title_accounts),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        items(accounts) { account ->
            AccountItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        //onItemClick?.invoke(account)
                    }
                    .padding(16.dp),
                name = account.name,
                icon = account.icon,
                iconBackgroundColor = account.iconBackgroundColor,
                amount = account.amount.asString(context)
            )
        }
    }
}

@Composable
fun IncomeExpenseBalanceView(
    incomeAmount: UiText,
    expenseAmount: UiText,
    balanceAmount: UiText,
) {

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
    ) {
        Row {
            AmountView(
                color = R.color.red_500,
                icon = R.drawable.ic_arrow_upward,
                amount = expenseAmount,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .weight(1f)
            )
            AmountView(
                color = R.color.green_500,
                icon = R.drawable.ic_arrow_downward,
                amount = incomeAmount,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f)
            )
        }

        Text(
            modifier = Modifier
                .padding(top = 16.dp)
                .background(
                    color = colorResource(id = R.color.black_20),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(12.dp)
                .align(Alignment.CenterHorizontally),
            text = balanceAmount.asString(context),
            fontSize = 14.sp
        )
    }
}

@Composable
fun AmountView(
    @ColorRes color: Int,
    @DrawableRes icon: Int,
    amount: UiText,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    Row(
        modifier = modifier
            .background(
                color = colorResource(id = color),
                shape = RoundedCornerShape(size = 36.dp)
            )
    ) {
        Icon(
            modifier = Modifier
                .padding(8.dp)
                .background(
                    color = colorResource(id = R.color.black_200),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(8.dp),
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = colorResource(id = R.color.white)
        )
        Column(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .fillMaxWidth(),
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.expense),
                color = colorResource(id = R.color.white),
                fontSize = 12.sp
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = amount.asString(context),
                color = colorResource(id = R.color.white),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Preview
@Composable
fun IncomeExpenseBalanceViewPreview() {
    ExpenseManagerTheme {
        DashboardScreenContent(
            modifier = Modifier
                .fillMaxSize(),
            expenseAmount = UiText.DynamicString("$500.0"),
            incomeAmount = UiText.DynamicString("$500.0"),
            balanceAmount = UiText.DynamicString("Balance $500.0"),
            chartData = AnalysisChartData(
                chartData = entryModelOf(
                    listOf(entryOf(0, 1), entryOf(1, 2), entryOf(2, 3)),
                    listOf(entryOf(0, 4), entryOf(1, 3), entryOf(2, 2)),
                ),
                dates = listOf(
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

@Preview
@Composable
fun DashboardScreenPreview() {
    ExpenseManagerTheme {
        DashboardScreen(rememberNavController())
    }
}