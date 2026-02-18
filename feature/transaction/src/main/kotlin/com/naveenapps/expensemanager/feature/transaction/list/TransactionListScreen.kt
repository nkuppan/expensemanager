package com.naveenapps.expensemanager.feature.transaction.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.common.utils.fromCompleteDate
import com.naveenapps.expensemanager.core.common.utils.toCompleteDateWithDate
import com.naveenapps.expensemanager.core.common.utils.toDate
import com.naveenapps.expensemanager.core.common.utils.toDay
import com.naveenapps.expensemanager.core.common.utils.toMonthYear
import com.naveenapps.expensemanager.core.designsystem.components.EmptyItem
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.model.TransactionGroup
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.model.TransactionUiItem
import com.naveenapps.expensemanager.feature.filter.FilterView
import com.naveenapps.expensemanager.feature.transaction.R
import org.koin.compose.viewmodel.koinViewModel
import java.util.Date

@Composable
fun TransactionListScreen(
    showBackNavigationIcon: Boolean = false,
    viewModel: TransactionListViewModel = koinViewModel()
) {

    val state by viewModel.state.collectAsState()

    TransactionListScreenContent(
        showBackNavigationIcon,
        state,
        viewModel::processAction
    )
}

@Composable
private fun TransactionListScreenContent(
    showBackNavigationIcon: Boolean,
    state: TransactionListState,
    onAction: (TransactionListAction) -> Unit
) {
    Scaffold(
        topBar = {
            ExpenseManagerTopAppBar(
                title = stringResource(R.string.transaction),
                navigationIcon = if (showBackNavigationIcon) Icons.AutoMirrored.Default.ArrowBack else null,
                navigationBackClick = {
                    onAction.invoke(TransactionListAction.ClosePage)
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onAction.invoke(TransactionListAction.OpenCreateTransaction)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "",
                )
            }
        },
    ) { innerPadding ->
        TransactionListScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
            state = state,
        ) { transaction ->
            onAction.invoke(TransactionListAction.OpenEdiTransaction(transaction.id))
        }
    }
}

@Composable
private fun TransactionListScreen(
    state: TransactionListState,
    modifier: Modifier = Modifier,
    onItemClick: ((TransactionUiItem) -> Unit)? = null,
) {

    LazyColumn(modifier = modifier.fillMaxWidth()) {
        item {
            FilterView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 6.dp),
            )
        }
        if (state.transactionListItem.isEmpty()) {
            item {
                EmptyItem(
                    modifier = Modifier
                        .fillMaxSize()
                        .height(400.dp),
                    emptyItemText = stringResource(id = R.string.no_transactions_available),
                    icon = com.naveenapps.expensemanager.core.designsystem.R.drawable.ic_no_transaction
                )
            }
        } else {

            items(state.transactionListItem) { transactionListItem ->
                when (transactionListItem) {
                    TransactionListItem.Divider -> {
                        HorizontalDivider(
                            modifier = Modifier.padding(
                                top = 8.dp,
                                bottom = 8.dp
                            )
                        )
                    }

                    is TransactionListItem.HeaderItem -> {
                        TransactionHeaderItem(
                            transactionListItem.date,
                            transactionListItem.amountTextColor,
                            transactionListItem.totalAmount,
                        )
                    }

                    is TransactionListItem.TransactionItem -> {
                        val item = transactionListItem.date
                        AppCardView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 2.dp)
                        ) {
                            TransactionItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onItemClick?.invoke(item)
                                    },
                                categoryName = item.categoryName,
                                categoryColor = item.categoryIcon.backgroundColor,
                                categoryIcon = item.categoryIcon.name,
                                amount = item.amount,
                                date = item.date,
                                notes = item.notes,
                                transactionType = item.transactionType,
                                fromAccountName = item.fromAccountName,
                                fromAccountIcon = item.fromAccountIcon.name,
                                fromAccountColor = item.fromAccountIcon.backgroundColor,
                                toAccountName = item.toAccountName,
                                toAccountIcon = item.toAccountIcon?.name,
                                toAccountColor = item.toAccountIcon?.backgroundColor,
                            )
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun TransactionHeaderItem(
    date: String,
    textColor: Int,
    totalAmount: String,
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
            textAlign = TextAlign.Center,
        )
        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
                .align(Alignment.CenterVertically),
        ) {
            Text(
                text = date.fromCompleteDate().toMonthYear(),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = date.fromCompleteDate().toDay(),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Text(
            modifier = Modifier
                .padding(start = 16.dp)
                .align(Alignment.CenterVertically),
            text = totalAmount,
            color = colorResource(id = textColor),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Preview
@Composable
fun TransactionListItemEmptyStatePreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        TransactionListScreen(
            state = TransactionListState(emptyList()),
            modifier = Modifier.fillMaxSize(),
        )
    }
}

val DUMMY_DATA = listOf(
    getTransactionUiState(),
    getTransactionUiState(),
    getTransactionUiState(),
)

fun getTransactionItem(id: String) = TransactionUiItem(
    id = id,
    notes = "Sample Description",
    amount = Amount(amount = 300.0, amountString = "300.00 ₹"),
    categoryName = "Clothing",
    transactionType = TransactionType.EXPENSE,
    categoryIcon = StoredIcon(
        name = "agriculture",
        backgroundColor = "#000000",
    ),
    fromAccountName = "DB Bank xxxx",
    fromAccountIcon = StoredIcon(
        name = "account_balance",
        backgroundColor = "#000000",
    ),
    date = Date().toCompleteDateWithDate(),
)

private fun getTransactionUiState() = TransactionGroup(
    date = "12/10/2023",
    amountTextColor = com.naveenapps.expensemanager.core.common.R.color.red_500,
    totalAmount = Amount(amount = 300.0, amountString = "300.00 ₹"),
    transactions = buildList {
        repeat(3) {
            add(getTransactionItem("1"))
        }
    },
)

@AppPreviewsLightAndDarkMode
@Composable
fun TransactionListItemSuccessStatePreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        TransactionListScreenContent(
            state = TransactionListState(DUMMY_DATA.convertGroupToTransactionListItems()),
            showBackNavigationIcon = true,
            onAction = {}
        )
    }
}
