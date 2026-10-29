@file:OptIn(ExperimentalMaterial3Api::class)

package com.naveenapps.expensemanager.feature.budget.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.R as DesignSystemR
import com.naveenapps.expensemanager.core.designsystem.components.EmptyItem
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardViewDefaults
import com.naveenapps.expensemanager.core.domain.usecase.budget.BudgetUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.BudgetTitle
import com.naveenapps.expensemanager.core.model.TransactionUiItem
import com.naveenapps.expensemanager.feature.budget.R
import com.naveenapps.expensemanager.feature.budget.components.BudgetGauge
import com.naveenapps.expensemanager.feature.budget.components.toBudgetProgress
import com.naveenapps.expensemanager.feature.budget.text
import com.naveenapps.expensemanager.feature.category.transaction.CategorySpendingChartCard
import com.naveenapps.expensemanager.feature.category.transaction.categorySpendingItems
import com.naveenapps.expensemanager.feature.transaction.list.TransactionItem
import com.naveenapps.expensemanager.feature.transaction.list.getTransactionItem
import java.time.LocalDate
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BudgetDetailScreen(
    viewModel: BudgetDetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BudgetDetailsScaffoldView(
        state = state,
        openBudgetEditScreen = viewModel::openBudgetCreateScreen,
        closePage = viewModel::closePage,
        openTransactionCreateScreen = viewModel::openTransactionCreateScreen,
        onCategoryClick = viewModel::toggleCategory,
        onClearCategoryFilter = viewModel::clearCategoryFilter,
    )
}

@Composable
private fun BudgetDetailsScaffoldView(
    state: BudgetDetailState,
    openBudgetEditScreen: () -> Unit,
    closePage: () -> Unit,
    openTransactionCreateScreen: (String?) -> Unit,
    onCategoryClick: (String) -> Unit,
    onClearCategoryFilter: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = closePage) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(DesignSystemR.string.a11y_close),
                        )
                    }
                },
                title = {
                    Text(
                        text = state.budget?.title?.text() ?: stringResource(R.string.budgets),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                actions = {
                    if (state.budget != null) {
                        IconButton(onClick = openBudgetEditScreen) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = stringResource(R.string.a11y_edit_budget),
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                onClick = { openTransactionCreateScreen.invoke(null) },
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(com.naveenapps.expensemanager.feature.transaction.R.string.add_transaction),
                )
            }
        },
    ) { innerPadding ->
        BudgetDetailContent(
            state = state,
            onTransactionClick = openTransactionCreateScreen,
            onEditBudget = openBudgetEditScreen,
            onCategoryClick = onCategoryClick,
            onClearCategoryFilter = onClearCategoryFilter,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        )
    }
}

@Composable
private fun BudgetDetailContent(
    state: BudgetDetailState,
    onTransactionClick: (String) -> Unit,
    onEditBudget: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onClearCategoryFilter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val budget = state.budget
    val progress = state.progress
    if (budget == null || progress == null) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(40.dp))
        }
        return
    }

    val spending = budget.categorySpending
    val categories = spending?.categoryTransactions.orEmpty()
    val selectedCategory = categories.firstOrNull { it.category.id == state.selectedCategoryId }
    val allTransactions = budget.transactions.orEmpty()
    val transactions = remember(allTransactions, selectedCategory) {
        if (selectedCategory == null) {
            allTransactions
        } else {
            val ids = selectedCategory.transaction.map { it.id }.toSet()
            allTransactions.filter { it.id in ids }
        }
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
    ) {
        // ── Gauge ──────────────────────────────────────────────
        item(key = "gauge") {
            AppCardView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                cornerSize = 24.dp,
            ) {
                BudgetGauge(
                    budget = progress,
                    showTitle = false,
                    onEdit = onEditBudget,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
                )
            }
        }

        // ── Spending by category ───────────────────────────────
        if (spending != null && categories.isNotEmpty()) {
            item(key = "chart_header") {
                SectionHeader(text = stringResource(R.string.budget_spending_by_category))
            }
            item(key = "chart") {
                CategorySpendingChartCard(
                    state = spending,
                    centerLabel = stringResource(R.string.budget_spent),
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                )
            }
            categorySpendingItems(
                categoryTransactions = categories,
                onItemClick = { onCategoryClick(it.category.id) },
                selectedCategoryId = state.selectedCategoryId,
            )
        }

        // ── Transactions ───────────────────────────────────────
        item(key = "transactions_header") {
            SectionHeader(text = stringResource(R.string.budget_transactions)) {
                if (selectedCategory != null) {
                    InputChip(
                        selected = true,
                        onClick = onClearCategoryFilter,
                        label = {
                            Text(
                                text = selectedCategory.category.titleResId?.let { stringResource(it) }
                                    ?: selectedCategory.category.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.budget_show_all_transactions),
                                modifier = Modifier.size(InputChipDefaults.IconSize),
                            )
                        },
                    )
                }
            }
        }

        if (transactions.isEmpty()) {
            item(key = "empty") {
                EmptyItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    emptyItemText = stringResource(id = com.naveenapps.expensemanager.feature.category.R.string.no_transactions_available),
                    icon = DesignSystemR.drawable.ic_no_transaction,
                )
            }
        } else {
            itemsIndexed(
                items = transactions,
                key = { _, item -> "transaction_${item.id}" },
            ) { index, item ->
                AppCardView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 2.dp),
                    shape = AppCardViewDefaults.cardShape(index, transactions),
                ) {
                    TransactionItem(
                        modifier = Modifier.fillMaxWidth(),
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
                        onEdit = { onTransactionClick.invoke(item.id) },
                    )
                }
            }
        }
    }
}

/** Small caps section title, as on the Categories tab, with optional content on the right. */
@Composable
private fun SectionHeader(text: String, trailing: (@Composable () -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 16.dp, top = 20.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
        trailing?.invoke()
    }
}

@AppPreviewsLightAndDarkMode
@Composable
fun BudgetDetailsScaffoldViewPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        val budget = BudgetUiModel(
            id = "sample",
            title = BudgetTitle.Custom("Groceries"),
            selectedMonth = "June 2026",
            progressBarColor = com.naveenapps.expensemanager.core.common.R.color.green_500,
            amount = Amount(100.0, "$100.00"),
            transactionAmount = Amount(50.0, "$50.00"),
            percent = 50.0f,
            transactions = getTransactionItems(),
        )
        BudgetDetailsScaffoldView(
            state = BudgetDetailState(
                budget = budget,
                progress = budget.toBudgetProgress(today = LocalDate.of(2026, 6, 20), format = { "$$it" }),
            ),
            openBudgetEditScreen = {},
            closePage = {},
            openTransactionCreateScreen = {},
            onCategoryClick = {},
            onClearCategoryFilter = {},
        )
    }
}

private fun getTransactionItems(total: Int = 10): List<TransactionUiItem> = buildList {
    repeat(total) {
        add(getTransactionItem(it.toString()))
    }
}
