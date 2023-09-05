package com.nkuppan.expensemanager.feature.transaction.list

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nkuppan.expensemanager.core.model.CategoryType
import com.nkuppan.expensemanager.core.model.UiState
import com.nkuppan.expensemanager.core.ui.utils.UiText
import com.nkuppan.expensemanager.feature.transaction.R
import com.nkuppan.expensemanager.feature.transaction.history.TransactionUIModel
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
                    painter = painterResource(id = com.nkuppan.expensemanager.core.ui.R.drawable.ic_add),
                    contentDescription = ""
                )
            }
        }
    ) {
        TransactionListScreen(
            modifier = Modifier.padding(top = it.calculateTopPadding()),
            transactionUiState = transactionUiState
        )
    }
}

@Composable
private fun TransactionListScreen(
    transactionUiState: UiState<List<TransactionUIModel>>,
    modifier: Modifier = Modifier
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
                            categoryName = it.categoryName,
                            accountName = it.accountName,
                            accountIcon = it.accountIcon,
                            notes = it.notes.asString(context),
                            amount = it.amount.asString(context),
                            date = it.date,
                            transactionColor = it.categoryBackgroundColor,
                            categoryType = it.categoryType,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(16.dp)
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
    @DrawableRes accountIcon: Int,
    notes: String?,
    amount: String,
    date: String,
    modifier: Modifier = Modifier,
    transactionColor: String = "#000000",
    categoryType: CategoryType = CategoryType.EXPENSE
) {
    Row(modifier = modifier) {
        Box(
            modifier = Modifier
                .padding(end = 16.dp)
                .size(36.dp)
                .align(Alignment.CenterVertically),
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                onDraw = {
                    drawCircle(color = Color(android.graphics.Color.parseColor(transactionColor)))
                }
            )
            Image(
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.Center),
                painter = painterResource(
                    id =
                    if (categoryType == CategoryType.EXPENSE)
                        R.drawable.ic_arrow_upward
                    else
                        R.drawable.ic_arrow_downward
                ),
                contentDescription = "",
                colorFilter = ColorFilter.tint(color = Color.White)
            )
        }
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
            Row(
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Image(
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.CenterVertically),
                    painter = painterResource(id = accountIcon),
                    contentDescription = ""
                )
                Text(
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .fillMaxWidth(),
                    fontSize = 12.sp,
                    text = accountName
                )
            }
            /*if (notes?.isNotBlank() == true) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = notes,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }*/
        }
        Column(
            modifier = Modifier
                .wrapContentSize()
                .padding(start = 16.dp)
        ) {
            Text(
                modifier = Modifier.align(Alignment.End),
                text = amount,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Medium,
                color = if (categoryType == CategoryType.EXPENSE)
                    Color.Red
                else
                    Color.Green
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
    MaterialTheme {
        TransactionItem(
            categoryName = "Utilities",
            notes = "Something people want to describe about the spendings",
            transactionColor = "#FFFFFF",
            amount = "300 ₹",
            date = "15/11/2019",
            accountName = "",
            accountIcon = com.nkuppan.expensemanager.feature.account.R.drawable.ic_card,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        )
    }
}

@Preview
@Composable
fun TransactionListItemLoadingStatePreview() {
    MaterialTheme {
        TransactionListScreen(
            transactionUiState = UiState.Loading,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview
@Composable
fun TransactionListItemEmptyStatePreview() {
    MaterialTheme {
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
        accountName = "",
        accountIcon = com.nkuppan.expensemanager.core.ui.R.drawable.ic_add,
        date = Date().toString()
    ),
    TransactionUIModel(
        id = "1",
        notes = UiText.DynamicString("Transaction One"),
        amount = UiText.DynamicString("Transaction One"),
        categoryName = "Clothing",
        categoryType = CategoryType.INCOME,
        categoryBackgroundColor = "#000000",
        accountName = "",
        accountIcon = com.nkuppan.expensemanager.core.ui.R.drawable.ic_add,
        date = Date().toString()
    ),
)

@Preview
@Composable
fun TransactionListItemSuccessStatePreview() {
    MaterialTheme {
        TransactionListScreen(
            transactionUiState = UiState.Success(
                DUMMY_DATA
            ),
            modifier = Modifier.fillMaxSize()
        )
    }
}