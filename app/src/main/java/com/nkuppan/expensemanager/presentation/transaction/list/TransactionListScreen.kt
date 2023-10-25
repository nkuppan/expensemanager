package com.nkuppan.expensemanager.presentation.transaction.list

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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.data.utils.toTransactionDate
import com.nkuppan.expensemanager.domain.model.TransactionType
import com.nkuppan.expensemanager.domain.model.UiState
import com.nkuppan.expensemanager.presentation.transaction.history.TransactionUIModel
import com.nkuppan.expensemanager.ui.extensions.getDrawable
import com.nkuppan.expensemanager.ui.theme.ExpenseManagerTheme
import com.nkuppan.expensemanager.ui.theme.widget.IconAndBackgroundView
import com.nkuppan.expensemanager.ui.theme.widget.TopNavigationBar
import com.nkuppan.expensemanager.ui.utils.ItemSpecModifier
import com.nkuppan.expensemanager.ui.utils.UiText
import com.nkuppan.expensemanager.ui.utils.getColorValue
import java.util.Date


@Composable
fun TransactionListScreen(
    navController: NavController
) {
    val viewModel: TransactionListViewModel = hiltViewModel()
    val transactionUiState by viewModel.transactions.collectAsState()
    Scaffold(
        topBar = {
            TopNavigationBar(
                navController = navController,
                title = stringResource(R.string.transaction),
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
    ) {
        TransactionListScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
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
                                .then(ItemSpecModifier),
                            categoryName = it.categoryName,
                            categoryColor = it.categoryBackgroundColor,
                            categoryIcon = it.categoryIcon,
                            amount = it.amount.asString(context),
                            date = it.date,
                            notes = it.notes,
                            transactionType = it.transactionType,
                            fromAccountName = it.fromAccountName,
                            fromAccountIcon = it.fromAccountIcon,
                            fromAccountColor = it.fromAccountColor,
                            toAccountName = it.toAccountName,
                            toAccountIcon = it.toAccountIcon,
                            toAccountColor = it.toAccountColor,
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
    fromAccountName: String,
    fromAccountIcon: String,
    fromAccountColor: String,
    amount: String,
    date: String,
    notes: UiText?,
    modifier: Modifier = Modifier,
    toAccountName: String? = null,
    toAccountIcon: String? = null,
    toAccountColor: String? = null,
    categoryColor: String = "#000000",
    categoryIcon: String = "ic_calendar",
    transactionType: TransactionType = TransactionType.EXPENSE
) {

    val context = LocalContext.current
    val isTransfer = toAccountName?.isNotBlank()

    Row(modifier = modifier) {
        IconAndBackgroundView(
            modifier = Modifier.align(Alignment.CenterVertically),
            icon = if (isTransfer == true) {
                "ic_transfer_account"
            } else {
                categoryIcon
            },
            iconBackgroundColor =
            if (isTransfer == true) {
                "#166EF7"
            } else {
                categoryColor
            },
            name = categoryName
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
                .align(Alignment.CenterVertically)
        ) {
            if (isTransfer == true && toAccountIcon != null && toAccountColor != null) {
                AccountNameWithIcon(
                    fromAccountIcon,
                    fromAccountColor,
                    fromAccountName
                )
                AccountNameWithIcon(
                    toAccountIcon,
                    toAccountColor,
                    toAccountName
                )
            } else {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = categoryName,
                    style = MaterialTheme.typography.bodyLarge
                )
                AccountNameWithIcon(
                    fromAccountIcon,
                    fromAccountColor,
                    fromAccountName
                )
                if (notes != null) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = notes.asString(context),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
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
                style = MaterialTheme.typography.titleMedium,
                color = when (transactionType) {
                    TransactionType.EXPENSE -> colorResource(id = R.color.red_500)
                    TransactionType.INCOME -> colorResource(id = R.color.green_500)
                    else -> Color.Unspecified
                }
            )
            Text(
                modifier = Modifier.align(Alignment.End),
                text = date,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun AccountNameWithIcon(
    fromAccountIcon: String,
    fromAccountColor: String,
    fromAccountName: String
) {

    val context = LocalContext.current

    Row {
        Icon(
            modifier = Modifier
                .size(12.dp)
                .align(Alignment.CenterVertically),
            painter = painterResource(id = context.getDrawable(fromAccountIcon)),
            contentDescription = "",
            tint = Color(getColorValue(fromAccountColor))
        )
        Text(
            modifier = Modifier
                .padding(start = 4.dp)
                .fillMaxWidth(),
            text = fromAccountName,
            color = Color(getColorValue(fromAccountColor)),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Preview
@Composable
fun TransactionItemPreview() {
    ExpenseManagerTheme {
        TransactionItem(
            categoryName = "Utilities",
            categoryColor = "#A65A56",
            fromAccountName = "Card-xxx",
            fromAccountIcon = "ic_account",
            fromAccountColor = "#A65A56x",
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
        notes = UiText.DynamicString("Sample Description"),
        amount = UiText.DynamicString("100.00$"),
        categoryName = "Clothing",
        transactionType = TransactionType.EXPENSE,
        categoryBackgroundColor = "#000000",
        categoryIcon = "ic_add",
        fromAccountName = "DB Bank xxxx",
        fromAccountIcon = "ic_account",
        fromAccountColor = "#000000",
        date = Date().toTransactionDate()
    ),
    TransactionUIModel(
        id = "2",
        notes = UiText.DynamicString("Sample Description"),
        amount = UiText.DynamicString("100.00$"),
        categoryName = "Utilities",
        transactionType = TransactionType.INCOME,
        categoryBackgroundColor = "#000000",
        categoryIcon = "ic_add",
        fromAccountName = "DB Bank xxxx",
        fromAccountIcon = "ic_account",
        fromAccountColor = "#000000",
        date = Date().toTransactionDate()
    ),
    TransactionUIModel(
        id = "3",
        notes = UiText.DynamicString("New Description about expense"),
        amount = UiText.DynamicString("100.00$"),
        categoryName = "Utilities",
        transactionType = TransactionType.TRANSFER,
        categoryBackgroundColor = "#000000",
        categoryIcon = "ic_add",
        date = Date().toTransactionDate(),
        fromAccountName = "DB Bank xxxx",
        fromAccountIcon = "ic_account",
        fromAccountColor = "#000000",
        toAccountName = "Another DB Bank xxxx",
        toAccountIcon = "ic_account",
        toAccountColor = "#000000"
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