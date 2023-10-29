package com.nkuppan.expensemanager.presentation.budget.list

import android.annotation.SuppressLint
import androidx.annotation.ColorRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.domain.model.UiState
import com.nkuppan.expensemanager.ui.theme.ExpenseManagerTheme
import com.nkuppan.expensemanager.ui.theme.widget.IconAndBackgroundView
import com.nkuppan.expensemanager.ui.theme.widget.TopNavigationBar
import com.nkuppan.expensemanager.ui.utils.ItemSpecModifier
import com.nkuppan.expensemanager.ui.utils.UiText


@Composable
fun BudgetListScreen(
    navController: NavController
) {
    val viewModel: BudgetListViewModel = hiltViewModel()
    val budgetUiState by viewModel.budgets.collectAsState()
    BudgetListScreenScaffoldView(navController, budgetUiState)
}

@Composable
private fun BudgetListScreenScaffoldView(
    navController: NavController,
    budgetUiState: UiState<List<BudgetUiModel>>
) {

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopNavigationBar(
                navController = navController,
                title = stringResource(R.string.budgets)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("budget/create")
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = ""
                )
            }
        }
    ) { innerPadding ->

        BudgetListScreenContent(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            budgetUiState = budgetUiState
        ) { budget ->
            navController.navigate("budget/create?budgetId=${budget.id}")
        }
    }
}

@Composable
private fun BudgetListScreenContent(
    budgetUiState: UiState<List<BudgetUiModel>>,
    modifier: Modifier = Modifier,
    onItemClick: ((BudgetUiModel) -> Unit)? = null
) {

    val scrollState = rememberLazyListState()
    val context = LocalContext.current

    Box(modifier = modifier) {

        when (budgetUiState) {
            UiState.Empty -> {
                Text(
                    modifier = Modifier
                        .wrapContentSize()
                        .align(Alignment.Center),
                    text = stringResource(id = R.string.no_budget_available),
                    textAlign = TextAlign.Center
                )
            }

            UiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(72.dp)
                        .align(Alignment.Center)
                )
            }

            is UiState.Success -> {

                LazyColumn(state = scrollState) {
                    items(budgetUiState.data) { budget ->
                        BudgetItem(
                            name = budget.name,
                            icon = budget.icon,
                            iconBackgroundColor = budget.iconBackgroundColor,
                            progressBarColor = budget.progressBarColor,
                            amount = budget.amount.asString(context),
                            transactionAmount = budget.transactionAmount.asString(context),
                            percentage = budget.percent,
                            modifier = Modifier
                                .clickable {
                                    onItemClick?.invoke(budget)
                                }
                                .then(ItemSpecModifier),
                        )
                    }
                    item {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp)
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("DiscouragedApi")
@Composable
fun BudgetItem(
    name: String,
    icon: String,
    iconBackgroundColor: String,
    @ColorRes progressBarColor: Int,
    amount: String?,
    transactionAmount: String?,
    modifier: Modifier = Modifier,
    percentage: Float = 0.0f
) {

    Row(modifier = modifier) {
        IconAndBackgroundView(
            modifier = Modifier
                .align(Alignment.CenterVertically),
            icon = icon,
            iconBackgroundColor = iconBackgroundColor,
            name = name
        )
        Column(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(start = 16.dp),
        ) {

            Row {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    text = name,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (amount != null) {
                    Text(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .align(Alignment.CenterVertically),
                        text = amount,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            Row(modifier = Modifier.padding(top = 4.dp)) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .align(Alignment.CenterVertically),
                    progress = percentage / 100,
                    color = colorResource(id = progressBarColor),
                    strokeCap = StrokeCap.Round
                )
                Text(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .align(Alignment.CenterVertically),
                    text = percentage.toPercentString(),
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = "$transactionAmount of $amount",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

fun Float.toPercentString(): String {
    return String.format("%.2f %%", this)
}

val DUMMY_DATA = listOf(
    BudgetUiModel(
        id = "1",
        name = "Cash",
        icon = "ic_budget",
        iconBackgroundColor = "#000000",
        amount = UiText.DynamicString("$100.00"),
        transactionAmount = UiText.DynamicString("$100.00"),
        progressBarColor = R.color.orange_500,
        percent = 0.9f
    ),
    BudgetUiModel(
        id = "2",
        name = "Bank Budget - xxxx",
        icon = "ic_budget_balance",
        iconBackgroundColor = "#000000",
        amount = UiText.DynamicString("$100.00"),
        transactionAmount = UiText.DynamicString("$100.00"),
        progressBarColor = R.color.orange_500,
        percent = 0.9f
    ),
    BudgetUiModel(
        id = "3",
        name = "Credit Card - xxxx",
        icon = "credit_card",
        iconBackgroundColor = "#000000",
        amount = UiText.DynamicString("$100.00"),
        transactionAmount = UiText.DynamicString("$100.00"),
        progressBarColor = R.color.orange_500,
        percent = 0.9f
    ),
)

@Preview
@Composable
private fun BudgetItemPreview() {
    ExpenseManagerTheme {
        BudgetItem(
            name = "Utilities",
            icon = "ic_calendar",
            iconBackgroundColor = "#000000",
            amount = "$100.00",
            transactionAmount = "$78.00",
            modifier = ItemSpecModifier,
            progressBarColor = R.color.orange_500,
            percentage = 78.8f
        )
    }
}

@Preview
@Composable
private fun BudgetListItemLoadingStatePreview() {
    ExpenseManagerTheme {
        BudgetListScreenScaffoldView(
            rememberNavController(),
            budgetUiState = UiState.Loading,
        )
    }
}

@Preview
@Composable
private fun BudgetListItemEmptyStatePreview() {
    ExpenseManagerTheme {
        BudgetListScreenScaffoldView(
            rememberNavController(),
            budgetUiState = UiState.Empty,
        )
    }
}

@Preview
@Composable
private fun BudgetListItemSuccessStatePreview() {
    ExpenseManagerTheme {
        BudgetListScreenScaffoldView(
            rememberNavController(),
            budgetUiState = UiState.Success(
                DUMMY_DATA
            ),
        )
    }
}