@file:OptIn(ExperimentalMaterial3Api::class)

package com.naveenapps.expensemanager.feature.budget.details

import androidx.annotation.ColorRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.common.utils.toPercentString
import com.naveenapps.expensemanager.core.designsystem.components.EmptyItem
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardViewDefaults
import com.naveenapps.expensemanager.core.domain.usecase.budget.BudgetUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.TransactionUiItem
import com.naveenapps.expensemanager.feature.budget.R
import com.naveenapps.expensemanager.feature.transaction.list.TransactionItem
import com.naveenapps.expensemanager.feature.transaction.list.getTransactionItem
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BudgetDetailScreen(
    viewModel: BudgetDetailViewModel = koinViewModel(),
) {

    val budget by viewModel.budget.collectAsState()

    BudgetDetailsScaffoldView(
        budget = budget,
        openBudgetEditScreen = viewModel::openBudgetCreateScreen,
        closePage = viewModel::closePage,
        openTransactionCreateScreen = viewModel::openTransactionCreateScreen
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun BudgetDetailsScaffoldView(
    budget: BudgetUiModel?,
    openBudgetEditScreen: () -> Unit,
    closePage: () -> Unit,
    openTransactionCreateScreen: (String?) -> Unit,
) {

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            Surface(shadowElevation = 2.dp) {
                Column {
                    TopAppBar(
                        navigationIcon = {
                            IconButton(onClick = closePage) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null
                                )
                            }
                        },
                        title = {
                            Text(stringResource(R.string.budgets))
                        },
                        actions = {
                            budget?.let {
                                IconButton(onClick = openBudgetEditScreen) {
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = "",
                                    )
                                }
                            }
                        }
                    )
                    budget?.let {
                        BudgetHeaderItem(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            name = it.name,
                            progressBarColor = it.progressBarColor,
                            amount = it.amount.amountString,
                            transactionAmount = it.transactionAmount.amountString,
                            percentage = it.percent,
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { openTransactionCreateScreen.invoke(null) }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "",
                )
            }
        },
    ) { innerPadding ->
        BudgetDetailContent(
            budget = budget,
            onItemClick = openTransactionCreateScreen,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        )
    }
}

@Composable
private fun BudgetDetailContent(
    budget: BudgetUiModel?,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        val transactions = budget?.transactions
        if (transactions?.isNotEmpty() == true) {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 78.dp,
                    top = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {

                itemsIndexed(
                    items = transactions,
                    key = { _, item -> item.id },
                ) { index, item ->
                    AppCardView(
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = AppCardViewDefaults.cardShape(index, transactions),
                    ) {
                        TransactionItem(
                            modifier = Modifier
                                .fillMaxWidth(),
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
                            onEdit = {
                                onItemClick.invoke(item.id)
                            }
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
        } else {
            EmptyItem(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.Center),
                emptyItemText = stringResource(id = com.naveenapps.expensemanager.feature.category.R.string.no_transactions_available),
                icon = com.naveenapps.expensemanager.core.designsystem.R.drawable.ic_no_transaction,
            )
        }
    }
}

@Composable
fun BudgetHeaderItem(
    name: String,
    @ColorRes progressBarColor: Int,
    amount: String?,
    transactionAmount: String?,
    modifier: Modifier = Modifier,
    percentage: Float = 0.0f,
) {
    Column(modifier = modifier) {
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

@AppPreviewsLightAndDarkMode
@Composable
fun BudgetDetailsScaffoldViewPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        BudgetDetailsScaffoldView(
            budget = BudgetUiModel(
                id = "sample",
                name = "My Own Budget",
                icon = "account_wallet",
                iconBackgroundColor = "#000000",
                progressBarColor = com.naveenapps.expensemanager.core.common.R.color.green_500,
                amount = Amount(100.0, "$100.00"),
                transactionAmount = Amount(100.0, "$100.00"),
                percent = 50.0f,
                transactions = getTransactionItems()
            ),
            openBudgetEditScreen = {},
            closePage = {},
            openTransactionCreateScreen = {},
        )
    }
}

private fun getTransactionItems(total: Int = 10): List<TransactionUiItem> {
    return buildList {
        repeat(total) {
            add(getTransactionItem(it.toString()))
        }
    }
}
