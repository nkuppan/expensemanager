package com.nkuppan.expensemanager.presentation.category.transaction

import androidx.compose.foundation.Image
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
import com.nkuppan.expensemanager.common.ui.extensions.toColor
import com.nkuppan.expensemanager.common.ui.theme.ExpenseManagerTheme
import com.nkuppan.expensemanager.common.ui.theme.widget.IconAndBackgroundView
import com.nkuppan.expensemanager.common.ui.theme.widget.TopNavigationBar
import com.nkuppan.expensemanager.common.ui.utils.ItemSpecModifier
import com.nkuppan.expensemanager.common.ui.utils.UiText
import com.nkuppan.expensemanager.domain.model.UiState
import com.nkuppan.expensemanager.presentation.budget.list.toPercentString
import com.nkuppan.expensemanager.presentation.category.list.getCategoryData
import kotlin.random.Random

@Composable
fun CategoryTransactionListScreen(
    navController: NavController
) {
    val viewModel: CategoryTransactionListViewModel = hiltViewModel()
    val uiState by viewModel.categoryTransaction.collectAsState()
    CategoryTransactionListScreen(navController, uiState)
}

@Composable
fun CategoryTransactionListScreen(
    navController: NavController,
    uiState: UiState<List<CategoryTransaction>>
) {

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopNavigationBar(
                navController = navController,
                title = stringResource(R.string.categories)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("account/create")
            }) {
                Image(
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
        ) { account ->
            navController.navigate("transaction/create")
        }
    }
}

@Composable
private fun CategoryTransactionListScreenContent(
    uiState: UiState<List<CategoryTransaction>>,
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
                    items(uiState.data) { categoryTransaction ->
                        CategoryTransactionItem(
                            modifier = ItemSpecModifier
                                .clickable {
                                    onItemClick?.invoke(categoryTransaction)
                                },
                            name = categoryTransaction.category.name,
                            icon = categoryTransaction.category.iconName,
                            iconBackgroundColor = categoryTransaction.category.iconBackgroundColor,
                            amount = categoryTransaction.amount.asString(context),
                            percentage = categoryTransaction.percent
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
            modifier = Modifier
                .align(Alignment.CenterVertically),
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
                    color = if (true)
                        colorResource(id = R.color.red_500)
                    else
                        colorResource(id = R.color.green_500)
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

fun getRandomCategoryTransactionData(): List<CategoryTransaction> {
    return buildList {
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
}


@Preview
@Composable
private fun CategoryTransactionItemPreview() {
    ExpenseManagerTheme {
        CategoryTransactionItem(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
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

@Preview
@Composable
private fun CategoryTransactionListItemSuccessStatePreview() {
    ExpenseManagerTheme {
        CategoryTransactionListScreen(
            rememberNavController(),
            uiState = UiState.Success(getRandomCategoryTransactionData()),
        )
    }
}