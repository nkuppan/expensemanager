package com.naveenapps.expensemanager.feature.transaction.list

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import com.naveenapps.expensemanager.core.common.utils.UiState
import com.naveenapps.expensemanager.core.common.utils.fromCompleteDate
import com.naveenapps.expensemanager.core.common.utils.toCompleteDate
import com.naveenapps.expensemanager.core.common.utils.toDate
import com.naveenapps.expensemanager.core.common.utils.toDay
import com.naveenapps.expensemanager.core.common.utils.toMonthYear
import com.naveenapps.expensemanager.core.designsystem.ui.components.IconAndBackgroundView
import com.naveenapps.expensemanager.core.designsystem.ui.components.TopNavigationBar
import com.naveenapps.expensemanager.core.designsystem.ui.extensions.getDrawable
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.designsystem.ui.utils.ItemSpecModifier
import com.naveenapps.expensemanager.core.designsystem.ui.utils.getColorValue
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.model.TransactionUiItem
import com.naveenapps.expensemanager.core.model.TransactionUiState
import com.naveenapps.expensemanager.feature.datefilter.FilterView
import com.naveenapps.expensemanager.feature.transaction.R
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
                    imageVector = Icons.Default.Add,
                    contentDescription = ""
                )
            }
        }
    ) { innerPadding ->
        TransactionListScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
            transactionUiState = transactionUiState
        ) { transaction ->
            navController.navigate("transaction/create?transactionId=${transaction.id}")
        }
    }
}

@Composable
private fun TransactionListScreen(
    transactionUiState: UiState<List<TransactionUiState>>,
    modifier: Modifier = Modifier,
    onItemClick: ((TransactionUiItem) -> Unit)? = null
) {

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
                    item {
                        FilterView(modifier = Modifier.fillMaxWidth())
                    }
                    items(transactionUiState.data) {
                        TransactionGroupItem(
                            it, onItemClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionGroupItem(
    transactionUiState: TransactionUiState,
    onItemClick: ((TransactionUiItem) -> Unit)?,
    isLastItem: Boolean = false
) {
    Column {
        TransactionHeaderItem(
            transactionUiState.date,
            transactionUiState.amountTextColor,
            transactionUiState.totalAmount
        )
        transactionUiState.transactions.forEach {
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
                amount = it.amount,
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
        if (isLastItem.not()) {
            Divider(modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))
        }
    }
}

@Composable
fun TransactionHeaderItem(
    date: String,
    textColor: Int,
    totalAmount: Amount
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 4.dp),
    ) {
        Text(
            modifier = Modifier.align(Alignment.CenterVertically),
            text = date.fromCompleteDate().toDate(),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
                .align(Alignment.CenterVertically),
        ) {
            Text(
                text = date.fromCompleteDate().toMonthYear(),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = date.fromCompleteDate().toDay(),
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(
            modifier = Modifier
                .padding(start = 16.dp)
                .align(Alignment.CenterVertically),
            text = totalAmount.amountString ?: "",
            color = colorResource(id = textColor),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
fun TransactionItem(
    categoryName: String,
    fromAccountName: String,
    fromAccountIcon: String,
    fromAccountColor: String,
    amount: Amount,
    date: String,
    notes: String?,
    modifier: Modifier = Modifier,
    toAccountName: String? = null,
    toAccountIcon: String? = null,
    toAccountColor: String? = null,
    categoryColor: String = "#000000",
    categoryIcon: String = "ic_calendar",
    transactionType: TransactionType = TransactionType.EXPENSE
) {
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
                if (notes?.isNotBlank() == true) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = notes,
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
                text = amount.amountString ?: "",
                style = MaterialTheme.typography.titleMedium,
                color = when (transactionType) {
                    TransactionType.EXPENSE -> colorResource(id = com.naveenapps.expensemanager.core.common.R.color.red_500)
                    TransactionType.INCOME -> colorResource(id = com.naveenapps.expensemanager.core.common.R.color.green_500)
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

@com.naveenapps.expensemanager.core.designsystem.AppPreviewsLightAndDarkMode
@Composable
fun TransactionUiStatePreview() {
    ExpenseManagerTheme {
        TransactionItem(
            categoryName = "Utilities",
            categoryColor = "#A65A56",
            fromAccountName = "Card-xxx",
            fromAccountIcon = "account_balance",
            fromAccountColor = "#A65A56x",
            amount = Amount(amount = 300.0, amountString = "300 ₹"),
            date = "15/11/2019",
            notes = "Sample notes given as per transaction",
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
        )
    }
}

@Preview
@Composable
fun TransactionItemPreview() {
    ExpenseManagerTheme {
        TransactionGroupItem(
            getTransactionUiState(),
            {}
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
    getTransactionUiState(),
    getTransactionUiState(),
    getTransactionUiState(),
)

private fun getTransactionItem() = TransactionUiItem(
    id = "1",
    notes = "Sample Description",
    amount = Amount(amount = 300.0, amountString = "300.00 ₹"),
    categoryName = "Clothing",
    transactionType = TransactionType.EXPENSE,
    categoryBackgroundColor = "#000000",
    categoryIcon = "car_rental",
    fromAccountName = "DB Bank xxxx",
    fromAccountIcon = "account_balance",
    fromAccountColor = "#000000",
    date = Date().toCompleteDate()
)

private fun getTransactionUiState() = TransactionUiState(
    date = "12/10/2023",
    amountTextColor = com.naveenapps.expensemanager.core.common.R.color.red_500,
    totalAmount = Amount(amount = 300.0, amountString = "300.00 ₹"),
    transactions = buildList {
        repeat(3) {
            add(getTransactionItem())
        }
    }
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