@file:OptIn(ExperimentalMaterial3Api::class)

package com.naveenapps.expensemanager.feature.budget.details

import androidx.annotation.ColorRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naveenapps.expensemanager.core.common.utils.toPercentString
import com.naveenapps.expensemanager.core.designsystem.ui.components.NavigationButton
import com.naveenapps.expensemanager.core.designsystem.ui.utils.ItemSpecModifier
import com.naveenapps.expensemanager.feature.budget.R
import com.naveenapps.expensemanager.feature.transaction.list.TransactionItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetDetailScreen(
    viewModel: BudgetDetailViewModel = hiltViewModel()
) {

    val budget by viewModel.budget.collectAsState()

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
                                Icons.Default.Close
                            )
                        },
                        title = {

                        },
                        actions = {
                            budget?.let {
                                IconButton(onClick = {
                                    viewModel.openBudgetCreateScreen(it.id)
                                }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = ""
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
            FloatingActionButton(onClick = {
                viewModel.openTransactionCreateScreen(null)
            }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = ""
                )
            }
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {

            val transactions = budget?.transactions
            if (transactions.isNullOrEmpty()) {

                Text(
                    modifier = Modifier
                        .wrapContentSize()
                        .align(Alignment.Center),
                    text = stringResource(id = R.string.no_budget_available),
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
                    items(transactions) { item ->
                        TransactionItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.openTransactionCreateScreen(item.id)
                                }
                                .then(ItemSpecModifier),
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
                overflow = TextOverflow.Ellipsis
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
                style = MaterialTheme.typography.labelSmall
            )
        }

        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = "$transactionAmount of $amount",
            style = MaterialTheme.typography.labelSmall
        )
    }
}