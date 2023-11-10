package com.naveenapps.expensemanager.presentation.category.transaction

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.naveenapps.expensemanager.R
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.domain.model.UiState
import com.naveenapps.expensemanager.presentation.budget.list.toPercentString
import com.naveenapps.expensemanager.presentation.category.list.getCategoryData
import com.naveenapps.expensemanager.ui.components.IconAndBackgroundView
import com.naveenapps.expensemanager.ui.components.PieChartView
import com.naveenapps.expensemanager.ui.components.SmallIconAndBackgroundView
import com.naveenapps.expensemanager.ui.components.TopNavigationBar
import com.naveenapps.expensemanager.ui.extensions.toColor
import com.naveenapps.expensemanager.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.ui.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.ui.utils.ItemSpecModifier
import com.naveenapps.expensemanager.ui.utils.UiText
import kotlin.random.Random

@SuppressLint("ComposableDestinationInComposeScope")
@Composable
fun CategoryTransactionTabScreen(
    navController: NavController
) {

    val titles = listOf(
        stringResource(id = R.string.income).uppercase(),
        stringResource(id = R.string.spending).uppercase(),
    )
    var tabIndex by remember { mutableIntStateOf(CategoryType.INCOME.ordinal) }

    Scaffold(
        topBar = {
            TopNavigationBar(
                navController = navController,
                title = stringResource(R.string.categories),
                disableBackIcon = true
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("transaction/create")
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = ""
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TabRow(selectedTabIndex = tabIndex) {
                titles.forEachIndexed { index, title ->
                    Tab(
                        text = { Text(title) },
                        selected = tabIndex == index,
                        onClick = { tabIndex = index }
                    )
                }
            }
            CategoryTransactionScreen(
                navController,
                CategoryType.values()[tabIndex]
            )
        }
    }
}

@Composable
private fun CategoryTransactionScreen(
    navController: NavController,
    categoryType: CategoryType
) {
    val viewModel: CategoryTransactionListViewModel = hiltViewModel()
    val uiState by viewModel.categoryTransaction.collectAsState()
    viewModel.setCategoryType(categoryType)
    CategoryTransactionListScreenContent(
        modifier = Modifier.fillMaxSize(),
        uiState = uiState
    ) { _ ->
        navController.navigate("transaction/create")
    }
}

@Composable
fun CategoryTransactionListScreen(
    navController: NavController,
    uiState: UiState<CategoryTransactionUiModel>
) {

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopNavigationBar(
                navController = navController,
                title = stringResource(R.string.categories),
                disableBackIcon = true
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("transaction/create")
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = ""
                )
            }
        }
    ) { innerPadding ->

        CategoryTransactionListScreenContent(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            uiState = uiState
        ) { _ ->
            navController.navigate("transaction/create")
        }
    }
}

@Composable
private fun CategoryTransactionListScreenContent(
    uiState: UiState<CategoryTransactionUiModel>,
    modifier: Modifier = Modifier,
    onItemClick: ((CategoryTransaction) -> Unit)? = null
) {

    val scrollState = rememberLazyListState()
    val context = LocalContext.current

    Box(modifier = modifier) {

        when (uiState) {
            UiState.Empty -> {
                Text(
                    modifier = Modifier
                        .wrapContentSize()
                        .align(Alignment.Center),
                    text = stringResource(id = R.string.no_account_available),
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
                    item {
                        PieChartView(
                            uiState.data.totalAmount.asString(context),
                            uiState.data.pieChartData,
                            chartHeight = 600,
                            hideValues = uiState.data.hideValues,
                            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
                        )
                    }
                    items(uiState.data.categoryTransactions) { categoryTransaction ->
                        CategoryTransactionItem(
                            modifier = Modifier
                                .clickable {
                                    onItemClick?.invoke(categoryTransaction)
                                }
                                .then(ItemSpecModifier),
                            name = categoryTransaction.category.name,
                            icon = categoryTransaction.category.iconName,
                            iconBackgroundColor = categoryTransaction.category.iconBackgroundColor,
                            amount = categoryTransaction.amount.asString(context),
                            percentage = categoryTransaction.percent
                        )
                    }
                    item {
                        if (uiState.data.categoryTransactions.isEmpty()) {
                            Text(
                                text = stringResource(id = R.string.no_transactions_available),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(36.dp),
                                textAlign = TextAlign.Center
                            )
                        }
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
            name = name
        )
        Column(
            modifier = Modifier
                .padding(start = 16.dp)
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
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .align(Alignment.CenterVertically),
                    progress = percentage / 100,
                    color = iconBackgroundColor.toColor(),
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
        }
    }
}


val getPieChartData = listOf(
    PieChartData("Chrome", 34.68f, Color.parseColor("#43A546")),
    PieChartData("Firefox", 16.60F, Color.parseColor("#F44336")),
    PieChartData("Safari", 16.15F, Color.parseColor("#166EF7")),
    PieChartData("Internet Explorer", 15.62F, Color.parseColor("#121212")),
)

fun getRandomCategoryTransactionData(): CategoryTransactionUiModel {
    return CategoryTransactionUiModel(
        pieChartData = getPieChartData,
        totalAmount = UiText.DynamicString("Total \n 100.00$"),
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
        }
    )
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
            iconSize = 12.dp
        )
        Text(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
                .padding(start = 16.dp, end = 16.dp),
            text = name,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            modifier = Modifier.align(Alignment.CenterVertically),
            text = amount,
            style = MaterialTheme.typography.labelSmall
        )
    }
}


@AppPreviewsLightAndDarkMode
@Composable
private fun CategoryTransactionSmallItemPreview() {
    ExpenseManagerTheme {
        CategoryTransactionSmallItem(
            modifier = Modifier
                .fillMaxWidth()
                .then(ItemSpecModifier),
            name = "Utilities",
            icon = "ic_calendar",
            iconBackgroundColor = "#000000",
            amount = "$100.00",
        )
    }
}


@Preview
@Composable
private fun CategoryTransactionItemPreview() {
    ExpenseManagerTheme {
        CategoryTransactionItem(
            modifier = Modifier
                .fillMaxWidth()
                .then(ItemSpecModifier),
            name = "Utilities",
            icon = "ic_calendar",
            iconBackgroundColor = "#000000",
            amount = "$100.00",
            percentage = 0.5f
        )
    }
}

@Preview
@Composable
private fun CategoryTransactionListItemLoadingStatePreview() {
    ExpenseManagerTheme {
        CategoryTransactionListScreen(
            rememberNavController(),
            uiState = UiState.Loading,
        )
    }
}

@Preview
@Composable
private fun CategoryTransactionListItemEmptyStatePreview() {
    ExpenseManagerTheme {
        CategoryTransactionListScreen(
            rememberNavController(),
            uiState = UiState.Empty,
        )
    }
}

@AppPreviewsLightAndDarkMode
@Composable
private fun CategoryTransactionListItemSuccessStatePreview() {
    ExpenseManagerTheme {
        CategoryTransactionListScreen(
            rememberNavController(),
            uiState = UiState.Success(getRandomCategoryTransactionData()),
        )
    }
}

@AppPreviewsLightAndDarkMode
@Composable
private fun CategoryTransactionTabScreenPreview() {
    ExpenseManagerTheme {
        CategoryTransactionTabScreen(
            rememberNavController()
        )
    }
}