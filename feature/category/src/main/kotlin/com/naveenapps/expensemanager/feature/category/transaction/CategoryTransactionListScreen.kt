package com.naveenapps.expensemanager.feature.category.transaction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.common.utils.UiState
import com.naveenapps.expensemanager.core.common.utils.toPercentString
import com.naveenapps.expensemanager.core.designsystem.components.EmptyItem
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.core.designsystem.ui.components.IconAndBackgroundView
import com.naveenapps.expensemanager.core.designsystem.ui.components.PieChartUiData
import com.naveenapps.expensemanager.core.designsystem.ui.components.PieChartView
import com.naveenapps.expensemanager.core.designsystem.ui.components.SmallIconAndBackgroundView
import com.naveenapps.expensemanager.core.designsystem.ui.extensions.toColor
import com.naveenapps.expensemanager.core.designsystem.ui.utils.ItemSpecModifier
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.CategoryTransaction
import com.naveenapps.expensemanager.core.model.CategoryTransactionState
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.PieChartData
import com.naveenapps.expensemanager.core.model.isExpense
import com.naveenapps.expensemanager.feature.category.R
import com.naveenapps.expensemanager.feature.category.list.getCategoryData
import com.naveenapps.expensemanager.feature.filter.FilterView
import org.koin.compose.viewmodel.koinViewModel
import kotlin.random.Random


@Composable
fun CategoryTransactionTabScreen(
    viewModel: CategoryTransactionListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    CategoryTransactionTabScreenContent(
        state = state,
        onAction = viewModel::processAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryTransactionTabScreenContent(
    state: UiState<CategoryTransactionState>,
    onAction: (CategoryTransactionAction) -> Unit
) {
    Scaffold(
        topBar = {
            ExpenseManagerTopAppBar(
                title = stringResource(R.string.categories),
                actions = {
                    IconButton(onClick = {
                        onAction.invoke(CategoryTransactionAction.OpenCategoryList)
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = stringResource(id = R.string.edit),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                onAction.invoke(CategoryTransactionAction.OpenTransactionCreate)
            }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "",
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            CategoryTransactionListScreenContent(
                state = state,
                changeChart = {
                    onAction.invoke(CategoryTransactionAction.SwitchCategoryType)
                },
                onItemClick = {
                    onAction.invoke(CategoryTransactionAction.OpenCategoryDetails(it))
                }
            )
        }
    }
}

@Composable
private fun CategoryTransactionListScreenContent(
    state: UiState<CategoryTransactionState>,
    changeChart: () -> Unit,
    onItemClick: (CategoryTransaction) -> Unit,
) {
    when (state) {
        UiState.Empty -> {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    modifier = Modifier
                        .wrapContentSize()
                        .align(Alignment.Center),
                    text = stringResource(id = R.string.no_transactions_available),
                    textAlign = TextAlign.Center,
                )
            }
        }

        UiState.Loading -> {
            Box(modifier = Modifier.fillMaxWidth()) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(72.dp)
                        .align(Alignment.Center),
                )
            }
        }

        is UiState.Success -> {
            val indicationSource = remember { MutableInteractionSource() }

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    FilterView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 6.dp),
                    )
                }
                item {
                    PieChartView(
                        if (state.data.categoryType.isExpense()) {
                            stringResource(id = R.string.expense)
                        } else {
                            stringResource(id = R.string.income)
                        } + "\n" + state.data.totalAmount.amountString,
                        chartData = state.data.pieChartData.map {
                            PieChartUiData(
                                it.name,
                                it.value,
                                it.color.toColorInt(),
                            )
                        },
                        chartHeight = 600,
                        hideValues = state.data.hideValues,
                        modifier = Modifier
                            .padding(top = 16.dp, bottom = 16.dp)
                            .clickable(indicationSource, null) {
                                changeChart.invoke()
                            },
                    )
                }

                if (state.data.categoryTransactions.isEmpty()) {
                    item {
                        EmptyItem(
                            modifier = Modifier.fillMaxSize(),
                            emptyItemText = stringResource(id = R.string.no_transactions_available),
                            icon = com.naveenapps.expensemanager.core.designsystem.R.drawable.ic_no_grouping_available
                        )
                    }
                } else {
                    items(
                        state.data.categoryTransactions,
                        key = { it.category.id }
                    ) { categoryTransaction ->
                        CategoryTransactionItem(
                            modifier = Modifier
                                .clickable {
                                    onItemClick.invoke(categoryTransaction)
                                }
                                .then(ItemSpecModifier),
                            name = categoryTransaction.category.name,
                            icon = categoryTransaction.category.storedIcon.name,
                            iconBackgroundColor = categoryTransaction.category.storedIcon.backgroundColor,
                            amount = categoryTransaction.amount.amountString ?: "",
                            percentage = categoryTransaction.percent,
                        )
                    }
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

@Composable
fun CategoryTransactionItem(
    modifier: Modifier,
    name: String,
    icon: String,
    iconBackgroundColor: String,
    amount: String,
    percentage: Float,
) {
    Row(modifier = modifier) {
        IconAndBackgroundView(
            modifier = Modifier.align(Alignment.CenterVertically),
            icon = icon,
            iconBackgroundColor = iconBackgroundColor,
            name = name,
        )
        Column(
            modifier = Modifier
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
                Text(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .align(Alignment.CenterVertically),
                    text = amount,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Row {
                LinearProgressIndicator(
                    progress = { percentage / 100 },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .align(Alignment.CenterVertically),
                    color = iconBackgroundColor.toColor(),
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
        }
    }
}

@Composable
fun CategoryTransactionSmallItem(
    name: String,
    icon: String,
    iconBackgroundColor: String,
    amount: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        SmallIconAndBackgroundView(
            icon = icon,
            iconBackgroundColor = iconBackgroundColor,
            modifier = Modifier.align(Alignment.CenterVertically),
            name = name,
            iconSize = 12.dp,
        )
        Text(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
                .padding(start = 16.dp, end = 16.dp),
            text = name,
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            modifier = Modifier.align(Alignment.CenterVertically),
            text = amount,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

val getPieChartData = listOf(
    PieChartData("Chrome", 34.68f, "#43A546"),
    PieChartData("Firefox", 16.60F, "#F44336"),
    PieChartData("Safari", 16.15F, "#166EF7"),
    PieChartData("Internet Explorer", 15.62F, "#121212"),
)

fun getRandomCategoryTransactionData(): CategoryTransactionState {
    return CategoryTransactionState(
        pieChartData = getPieChartData,
        totalAmount = Amount(300.0, "300.00$"),
        categoryTransactions = buildList {
            repeat(15) {
                add(
                    CategoryTransaction(
                        category = getCategoryData(it, CategoryType.EXPENSE),
                        amount = Amount(300.0, "300.00$"),
                        percent = Random(100).nextFloat(),
                        transaction = emptyList(),
                    ),
                )
            }
        },
        categoryType = CategoryType.EXPENSE
    )
}

fun getUiState(): UiState<CategoryTransactionState> {
    return UiState.Success(data = getRandomCategoryTransactionData())
}

@AppPreviewsLightAndDarkMode
@Composable
private fun CategoryTransactionSmallItemPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        CategoryTransactionSmallItem(
            modifier = Modifier
                .fillMaxWidth()
                .then(ItemSpecModifier),
            name = "Utilities",
            icon = "account_balance",
            iconBackgroundColor = "#000000",
            amount = "$100.00",
        )
    }
}

@AppPreviewsLightAndDarkMode
@Composable
private fun CategoryTransactionItemPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        CategoryTransactionItem(
            modifier = Modifier
                .fillMaxWidth()
                .then(ItemSpecModifier),
            name = "Utilities",
            icon = "account_balance",
            iconBackgroundColor = "#000000",
            amount = "$100.00",
            percentage = 0.5f,
        )
    }
}

@AppPreviewsLightAndDarkMode
@Composable
private fun CategoryTransactionTabScreenPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        CategoryTransactionTabScreenContent(
            state = getUiState(),
            onAction = {}
        )
    }
}
