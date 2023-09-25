package com.nkuppan.expensemanager.presentation.budget.list

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.core.ui.theme.ExpenseManagerTheme
import com.nkuppan.expensemanager.core.ui.theme.NavigationButton
import com.nkuppan.expensemanager.core.ui.theme.widget.IconAndBackgroundView
import com.nkuppan.expensemanager.core.ui.utils.UiText
import com.nkuppan.expensemanager.domain.model.UiState


@Composable
fun BudgetListScreen(
    navController: NavController
) {
    val viewModel: BudgetListViewModel = hiltViewModel()
    val budgetUiState by viewModel.budgets.collectAsState()
    BudgetListScreenScaffoldView(navController, budgetUiState)
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
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
            TopAppBar(
                navigationIcon = {
                    NavigationButton(navController)
                },
                title = {
                    Text(text = stringResource(R.string.budgets))
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("budget/create")
            }) {
                Image(
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
                            amount = budget.amount.asString(context),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onItemClick?.invoke(budget)
                                }
                                .padding(16.dp)
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
    amount: String?,
    modifier: Modifier = Modifier,
    percentage: Float = 0.0f
) {

    Row(modifier = modifier) {
        IconAndBackgroundView(
            modifier = Modifier.align(Alignment.CenterVertically),
            icon = icon,
            iconBackgroundColor = iconBackgroundColor,
            name = name
        )
        Column(
            modifier = Modifier.align(Alignment.CenterVertically),
        ) {

            Row {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    text = name
                )
                if (amount != null) {
                    Text(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .align(Alignment.CenterVertically),
                        text = amount,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Row {
                LinearProgressIndicator(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    progress = percentage,
                    color = Color(android.graphics.Color.parseColor(iconBackgroundColor)),
                    strokeCap = StrokeCap.Round
                )
                Text(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .align(Alignment.CenterVertically),
                    text = percentage.toString(),
                    fontSize = 12.sp
                )
            }
        }
    }
}

val DUMMY_DATA = listOf(
    BudgetUiModel(
        id = "1",
        name = "Cash",
        icon = "ic_budget",
        iconBackgroundColor = "#000000",
        amount = UiText.DynamicString("$100.00")
    ),
    BudgetUiModel(
        id = "2",
        name = "Bank Budget - xxxx",
        icon = "ic_budget_balance",
        iconBackgroundColor = "#000000",
        amount = UiText.DynamicString("$100.00")
    ),
    BudgetUiModel(
        id = "3",
        name = "Credit Card - xxxx",
        icon = "credit_card",
        iconBackgroundColor = "#000000",
        amount = UiText.DynamicString("$100.00")
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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