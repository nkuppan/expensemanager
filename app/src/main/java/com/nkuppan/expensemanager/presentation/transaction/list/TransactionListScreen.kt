package com.nkuppan.expensemanager.presentation.transaction.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.core.ui.extensions.getDrawable
import com.nkuppan.expensemanager.core.ui.theme.ExpenseManagerTheme
import com.nkuppan.expensemanager.core.ui.theme.NavigationButton
import com.nkuppan.expensemanager.core.ui.theme.widget.IconAndBackgroundView
import com.nkuppan.expensemanager.core.ui.utils.UiText
import com.nkuppan.expensemanager.core.ui.utils.getColorValue
import com.nkuppan.expensemanager.domain.model.CategoryType
import com.nkuppan.expensemanager.domain.model.UiState
import com.nkuppan.expensemanager.presentation.transaction.history.TransactionUIModel
import java.util.Date


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TransactionListScreen(
    navController: NavController
) {
    val viewModel: TransactionListViewModel = hiltViewModel()
    val transactionUiState by viewModel.transactions.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    NavigationButton(
                        navController = navController
                    )
                },
                title = {
                    Text(text = stringResource(R.string.transaction))
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("transaction/create")
            }) {
                Image(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = ""
                )
            }
        }
    ) {
        TransactionListScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = it.calculateTopPadding()),
            transactionUiState = transactionUiState,
            onItemClick = { transaction ->
                navController.navigate("transaction/create?transactionId=${transaction.id}")
            }
        )
    }
}

@Composable
private fun TransactionListScreen(
    transactionUiState: UiState<List<TransactionUIModel>>,
    modifier: Modifier = Modifier,
    onItemClick: ((TransactionUIModel) -> Unit)? = null
) {

    val context = LocalContext.current

    val scrollState = rememberLazyListState()

    Box(modifier = modifier) {

        when (transactionUiState) {
            UiState.Empty -> {
                Text(
                    modifier = Modifier
                        .wrapContentSize()
                        .align(Alignment.Center),
                    text = stringResource(id = R.string.no_transactions_available),
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
                    items(transactionUiState.data) {
                        TransactionItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onItemClick?.invoke(it)
                                }
                                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
                            categoryName = it.categoryName,
                            categoryColor = it.categoryBackgroundColor,
                            categoryIcon = it.categoryIcon,
                            accountName = it.accountName,
                            accountIcon = it.accountIcon,
                            accountColor = it.accountColor,
                            amount = it.amount.asString(context),
                            date = it.date,
                            notes = it.notes,
                            categoryType = it.categoryType
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(
    categoryName: String,
    accountName: String,
    accountIcon: String,
    accountColor: String,
    amount: String,
    date: String,
    notes: UiText?,
    modifier: Modifier = Modifier,
    categoryColor: String = "#000000",
    categoryIcon: String = "ic_calendar",
    categoryType: CategoryType = CategoryType.EXPENSE
) {

    val context = LocalContext.current

    Row(modifier = modifier) {
        IconAndBackgroundView(
            modifier = Modifier.align(Alignment.CenterVertically),
            icon = categoryIcon,
            iconBackgroundColor = categoryColor,
            name = categoryName
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text = categoryName
            )
            Row {
                Icon(
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.CenterVertically),
                    painter = painterResource(id = context.getDrawable(accountIcon)),
                    contentDescription = "",
                    tint = Color(getColorValue(accountColor))
                )
                Text(
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .fillMaxWidth(),
                    fontSize = 12.sp,
                    text = accountName,
                    color = Color(getColorValue(accountColor))
                )
            }
            if (notes != null) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = notes.asString(context),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Column(
            modifier = Modifier
                .wrapContentSize()
                .padding(start = 16.dp)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                modifier = Modifier.align(Alignment.End),
                text = amount,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Medium,
                color = if (categoryType == CategoryType.EXPENSE)
                    colorResource(id = R.color.red_500)
                else
                    colorResource(id = R.color.green_500)
            )
            Text(
                modifier = Modifier.align(Alignment.End),
                text = date,
                fontSize = 12.sp,
                fontStyle = FontStyle.Normal,
            )
        }
    }
}

@Preview
@Composable
fun TransactionItemPreview() {
    ExpenseManagerTheme {
        TransactionItem(
            categoryName = "Utilities",
            categoryColor = "#A65A56",
            accountName = "Card-xxx",
            accountIcon = "ic_account",
            accountColor = "#A65A56x",
            amount = "300 ₹",
            date = "15/11/2019",
            notes = UiText.DynamicString("Sample notes given as per transaction"),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
        )
    }
}

@Preview
@Composable
fun TransactionListItemLoadingStatePreview() {
    ExpenseManagerTheme {
        TransactionListScreen(
            transactionUiState = UiState.Loading,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview
@Composable
fun TransactionListItemEmptyStatePreview() {
    ExpenseManagerTheme {
        TransactionListScreen(
            transactionUiState = UiState.Empty,
            modifier = Modifier.fillMaxSize()
        )
    }
}

val DUMMY_DATA = listOf(
    TransactionUIModel(
        id = "1",
        notes = UiText.DynamicString("Transaction One"),
        amount = UiText.DynamicString("Transaction One"),
        categoryName = "Clothing",
        categoryType = CategoryType.EXPENSE,
        categoryBackgroundColor = "#000000",
        categoryIcon = "ic_add",
        accountName = "DB Bank xxxx",
        accountIcon = "ic_account",
        accountColor = "#000000",
        date = Date().toString()
    ),
    TransactionUIModel(
        id = "2",
        notes = UiText.DynamicString("Transaction One"),
        amount = UiText.DynamicString("Transaction One"),
        categoryName = "Clothing",
        categoryType = CategoryType.INCOME,
        categoryBackgroundColor = "#000000",
        categoryIcon = "ic_add",
        accountName = "DB Bank xxxx",
        accountIcon = "ic_account",
        accountColor = "#000000",
        date = Date().toString()
    ),
)

@Preview
@Composable
fun TransactionListItemSuccessStatePreview() {
    ExpenseManagerTheme {
        TransactionListScreen(
            transactionUiState = UiState.Success(
                DUMMY_DATA
            ),
            modifier = Modifier.fillMaxSize()
        )
    }
}