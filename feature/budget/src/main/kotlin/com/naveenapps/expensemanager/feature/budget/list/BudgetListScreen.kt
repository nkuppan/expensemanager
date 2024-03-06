package com.naveenapps.expensemanager.feature.budget.list

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naveenapps.expensemanager.core.common.utils.UiState
import com.naveenapps.expensemanager.core.common.utils.toPercentString
import com.naveenapps.expensemanager.core.designsystem.components.EmptyItem
import com.naveenapps.expensemanager.core.designsystem.components.LoadingItem
import com.naveenapps.expensemanager.core.designsystem.ui.components.IconAndBackgroundView
import com.naveenapps.expensemanager.core.designsystem.ui.components.TopNavigationBar
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.designsystem.ui.utils.ItemSpecModifier
import com.naveenapps.expensemanager.core.designsystem.ui.utils.getIncomeBGColor
import com.naveenapps.expensemanager.core.domain.usecase.budget.BudgetUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.feature.budget.R

@Composable
fun BudgetListScreen(
    viewModel: BudgetListViewModel = hiltViewModel(),
) {
    val budgetUiState by viewModel.budgets.collectAsState()

    BudgetListScreenContent(
        budgetUiState = budgetUiState,
        closePage = viewModel::closePage,
        openCreatePage = viewModel::openCreateScreen
    )
}

@Composable
private fun BudgetListScreenContent(
    budgetUiState: UiState<List<BudgetUiModel>>,
    closePage: () -> Unit,
    openCreatePage: (BudgetUiModel?) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopNavigationBar(
                onClick = closePage,
                title = stringResource(R.string.budgets),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { openCreatePage.invoke(null) }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "",
                )
            }
        },
    ) { innerPadding ->
        BudgetListScreenContent(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            budgetUiState = budgetUiState,
            onItemClick = openCreatePage
        )
    }
}

@Composable
private fun BudgetListScreenContent(
    budgetUiState: UiState<List<BudgetUiModel>>,
    onItemClick: ((BudgetUiModel) -> Unit),
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberLazyListState()

    Box(modifier = modifier) {
        when (budgetUiState) {
            UiState.Empty -> {
                EmptyItem(
                    emptyItemText = stringResource(id = R.string.no_budget_available),
                    icon = com.naveenapps.expensemanager.core.designsystem.R.drawable.ic_no_budgets,
                    modifier = Modifier.fillMaxSize()
                )
            }

            UiState.Loading -> {
                LoadingItem()
            }

            is UiState.Success -> {
                LazyColumn(state = scrollState) {
                    items(
                        budgetUiState.data,
                        key = { it.id }
                    ) { budget ->
                        BudgetItem(
                            name = budget.name,
                            icon = budget.icon,
                            iconBackgroundColor = budget.iconBackgroundColor,
                            progressBarColor = budget.progressBarColor,
                            amount = budget.amount,
                            transactionAmount = budget.transactionAmount,
                            percentage = budget.percent,
                            modifier = Modifier
                                .clickable {
                                    onItemClick.invoke(budget)
                                }
                                .then(ItemSpecModifier),
                        )
                    }
                    item {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
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
    amount: Amount?,
    transactionAmount: Amount?,
    modifier: Modifier = Modifier,
    percentage: Float = 0.0f,
) {
    Row(modifier = modifier) {
        IconAndBackgroundView(
            modifier = Modifier
                .align(Alignment.CenterVertically),
            icon = icon,
            iconBackgroundColor = iconBackgroundColor,
            name = name,
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
                    style = MaterialTheme.typography.bodyLarge,
                )
                if (amount != null) {
                    Text(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .align(Alignment.CenterVertically),
                        text = amount.amountString ?: "",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
            Row(modifier = Modifier.padding(top = 4.dp)) {
                LinearProgressIndicator(
                    progress = { percentage / 100 },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .align(Alignment.CenterVertically),
                    color = colorResource(id = progressBarColor),
                    strokeCap = StrokeCap.Round,
                )
                Text(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .align(Alignment.CenterVertically),
                    text = percentage.toPercentString(),
                    style = MaterialTheme.typography.labelSmall,
                )
            }

            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = "${transactionAmount?.amountString} of ${amount?.amountString}",
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
fun DashBoardBudgetItem(
    name: String,
    @ColorRes progressBarColor: Int,
    amount: String?,
    transactionAmount: String?,
    modifier: Modifier = Modifier,
    percentage: Float = 0.0f,
    backgroundColor: Color,
) {
    Surface(
        modifier = modifier,
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .width(260.dp),
        ) {
            Row {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    text = name,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(start = 4.dp),
                    text = transactionAmount ?: "",
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
            Row(modifier = Modifier.padding(top = 4.dp)) {
                LinearProgressIndicator(
                    progress = { percentage / 100 },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .align(Alignment.CenterVertically),
                    color = colorResource(id = progressBarColor),
                    strokeCap = StrokeCap.Round,
                )
                Text(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .align(Alignment.CenterVertically),
                    text = percentage.toPercentString(),
                    style = MaterialTheme.typography.labelSmall,
                )
            }

            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = "$transactionAmount of $amount",
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

fun getRandomBudgetUiModel(size: Int): List<BudgetUiModel> {
    return buildList {
        repeat(size) {
            add(getBudgetUiModel(it.toString()))
        }
    }
}

private fun getBudgetUiModel(id: String) = BudgetUiModel(
    id = id,
    name = "Cash",
    icon = "account_balance",
    iconBackgroundColor = "#000000",
    amount = Amount(amount = 300.0, amountString = "300.00 ₹"),
    transactionAmount = Amount(amount = 300.0, amountString = "300.00 ₹"),
    progressBarColor = com.naveenapps.expensemanager.core.common.R.color.orange_500,
    percent = 0.9f,
)

@Preview
@Composable
private fun BudgetItemPreview() {
    ExpenseManagerTheme {
        BudgetItem(
            name = "Utilities",
            icon = "account_balance",
            iconBackgroundColor = "#000000",
            amount = Amount(amount = 300.0, amountString = "300.00 ₹"),
            transactionAmount = Amount(amount = 300.0, amountString = "300.00 ₹"),
            modifier = ItemSpecModifier,
            progressBarColor = com.naveenapps.expensemanager.core.common.R.color.orange_500,
            percentage = 78.8f,
        )
    }
}

@Preview
@Composable
private fun DashboardBudgetItemPreview() {
    ExpenseManagerTheme {
        DashBoardBudgetItem(
            name = "This Month Budget",
            progressBarColor = com.naveenapps.expensemanager.core.common.R.color.orange_500,
            amount = "$100.00",
            transactionAmount = "$78.00",
            modifier = ItemSpecModifier,
            percentage = 78.8f,
            backgroundColor = getIncomeBGColor(),
        )
    }
}

@Preview
@Composable
private fun BudgetListItemLoadingStatePreview() {
    ExpenseManagerTheme {
        BudgetListScreenContent(
            budgetUiState = UiState.Loading,
            closePage = {},
            openCreatePage = {}
        )
    }
}

@Preview
@Composable
private fun BudgetListItemEmptyStatePreview() {
    ExpenseManagerTheme {
        BudgetListScreenContent(
            budgetUiState = UiState.Empty,
            closePage = {},
            openCreatePage = {}
        )
    }
}

@Preview
@Composable
private fun BudgetListItemSuccessStatePreview() {
    ExpenseManagerTheme {
        BudgetListScreenContent(
            budgetUiState = UiState.Success(
                getRandomBudgetUiModel(5),
            ),
            closePage = {},
            openCreatePage = {}
        )
    }
}
