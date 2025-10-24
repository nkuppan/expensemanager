package com.naveenapps.expensemanager.feature.category.details

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import com.naveenapps.expensemanager.core.designsystem.ui.components.NavigationButton
import com.naveenapps.expensemanager.core.designsystem.ui.extensions.getDrawable
import com.naveenapps.expensemanager.core.designsystem.ui.utils.IconSpecModifier
import com.naveenapps.expensemanager.core.designsystem.ui.utils.ItemSpecModifier
import com.naveenapps.expensemanager.core.designsystem.ui.utils.getColorValue
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.feature.category.R
import com.naveenapps.expensemanager.feature.category.transaction.CategoryTransactionItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    viewModel: CategoryDetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            Surface(shadowElevation = 4.dp) {
                Column {
                    TopAppBar(
                        navigationIcon = {
                            NavigationButton(
                                onClick = {
                                    viewModel.closePage()
                                },
                                Icons.Default.Close,
                            )
                        },
                        title = {
                        },
                        actions = {
                            state.categoryTransaction?.let {
                                IconButton(onClick = {
                                    viewModel.openCategoryEditScreen()
                                }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = "",
                                    )
                                }
                            }
                        },
                    )
                    state.categoryTransaction?.let { categoryTransaction ->
                        CategoryTransactionItem(
                            modifier = Modifier.padding(16.dp),
                            name = categoryTransaction.category.name,
                            icon = categoryTransaction.category.storedIcon.name,
                            iconBackgroundColor = categoryTransaction.category.storedIcon.backgroundColor,
                            amount = categoryTransaction.amount.amountString ?: "",
                            percentage = categoryTransaction.percent,
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.openTransactionCreateScreen(null)
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
            if (state.transactions.isEmpty()) {
                Text(
                    modifier = Modifier
                        .wrapContentSize()
                        .align(Alignment.Center),
                    text = stringResource(id = R.string.no_transactions_available),
                    textAlign = TextAlign.Center,
                )
            } else {
                LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
                    items(state.transactions, key = { it.id }) { item ->
                        TransactionItem(
                            categoryName = item.categoryName,
                            fromAccountName = item.fromAccountName,
                            fromAccountIcon = item.fromAccountIcon.name,
                            fromAccountColor = item.fromAccountIcon.backgroundColor,
                            amount = item.amount,
                            date = item.date,
                            notes = item.notes,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.openTransactionCreateScreen(item.id)
                                }
                                .then(ItemSpecModifier),
                            toAccountName = item.toAccountName,
                            toAccountIcon = item.toAccountIcon?.name,
                            toAccountColor = item.toAccountIcon?.backgroundColor,
                            transactionType = item.transactionType,
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
    transactionType: TransactionType = TransactionType.EXPENSE,
) {
    val isTransfer = toAccountName?.isNotBlank()

    Row(modifier = modifier) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .then(IconSpecModifier),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
                .align(Alignment.CenterVertically),
        ) {
            if (isTransfer == true && toAccountIcon != null && toAccountColor != null) {
                AccountNameWithIcon(
                    fromAccountIcon,
                    fromAccountColor,
                    fromAccountName,
                )
                AccountNameWithIcon(
                    toAccountIcon,
                    toAccountColor,
                    toAccountName,
                )
            } else {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = categoryName,
                    style = MaterialTheme.typography.bodyLarge,
                )
                AccountNameWithIcon(
                    fromAccountIcon,
                    fromAccountColor,
                    fromAccountName,
                )
                if (notes?.isNotBlank() == true) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = notes,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .wrapContentSize()
                .padding(start = 16.dp)
                .align(Alignment.CenterVertically),
        ) {
            Text(
                modifier = Modifier.align(Alignment.End),
                text = amount.amountString ?: "",
                style = MaterialTheme.typography.titleMedium,
                color = when (transactionType) {
                    TransactionType.EXPENSE -> colorResource(id = com.naveenapps.expensemanager.core.common.R.color.red_500)
                    TransactionType.INCOME -> colorResource(id = com.naveenapps.expensemanager.core.common.R.color.green_500)
                    else -> Color.Unspecified
                },
            )
            Text(
                modifier = Modifier.align(Alignment.End),
                text = date,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun AccountNameWithIcon(
    fromAccountIcon: String,
    fromAccountColor: String,
    fromAccountName: String,
) {
    val context = LocalContext.current

    Row {
        Icon(
            modifier = Modifier
                .size(12.dp)
                .align(Alignment.CenterVertically),
            painter = painterResource(id = context.getDrawable(fromAccountIcon)),
            contentDescription = "",
            tint = Color(getColorValue(fromAccountColor)),
        )
        Text(
            modifier = Modifier
                .padding(start = 4.dp)
                .fillMaxWidth(),
            text = fromAccountName,
            color = Color(getColorValue(fromAccountColor)),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}
