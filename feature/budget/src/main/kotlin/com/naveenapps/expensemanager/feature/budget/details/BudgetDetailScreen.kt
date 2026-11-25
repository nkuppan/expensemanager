@file:OptIn(ExperimentalMaterial3Api::class)

package com.naveenapps.expensemanager.feature.budget.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.designsystem.R as DesignSystemR
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmCardPosition
import com.naveenapps.expensemanager.core.designsystem.em.EmChip
import com.naveenapps.expensemanager.core.designsystem.em.EmChipVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmEmptyState
import com.naveenapps.expensemanager.core.designsystem.em.EmFab
import com.naveenapps.expensemanager.core.designsystem.em.EmIconButton
import com.naveenapps.expensemanager.core.designsystem.em.EmLoading
import com.naveenapps.expensemanager.core.designsystem.em.EmPreviews
import com.naveenapps.expensemanager.core.designsystem.em.EmScaffold
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBar
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBarNavigation
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.TransactionItem
import com.naveenapps.expensemanager.core.designsystem.ui.utils.displayName
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
import com.naveenapps.expensemanager.feature.transaction.list.getTransactionItem
import kotlinx.datetime.LocalDate
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
    EmScaffold(
        topBar = {
            EmTopBar(
                title = state.budget?.title?.text() ?: stringResource(R.string.budgets),
                navigation = EmTopBarNavigation.Close,
                onNavigationClick = closePage,
                actions = {
                    if (state.budget != null) {
                        EmIconButton(
                            icon = Icons.Outlined.Edit,
                            contentDescription = stringResource(R.string.a11y_edit_budget),
                            onClick = openBudgetEditScreen,
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            EmFab(
                icon = Icons.Default.Add,
                contentDescription = stringResource(com.naveenapps.expensemanager.feature.transaction.R.string.add_transaction),
                onClick = { openTransactionCreateScreen.invoke(null) },
            )
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
            EmLoading()
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
        contentPadding = PaddingValues(top = EmTheme.spacing.sm, bottom = 96.dp),
    ) {
        // ── Gauge ──────────────────────────────────────────────
        item(key = "gauge") {
            EmCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = EmTheme.spacing.lg),
            ) {
                BudgetGauge(
                    budget = progress,
                    showTitle = false,
                    onEdit = onEditBudget,
                    modifier = Modifier.padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.xl),
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
                    modifier = Modifier.padding(
                        start = EmTheme.spacing.lg,
                        end = EmTheme.spacing.lg,
                        bottom = EmTheme.spacing.md,
                    ),
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
                    EmChip(
                        label = selectedCategory.category.displayName(),
                        onClick = onClearCategoryFilter,
                        variant = EmChipVariant.Input,
                        onRemove = onClearCategoryFilter,
                    )
                }
            }
        }

        if (transactions.isEmpty()) {
            item(key = "empty") {
                EmEmptyState(
                    illustration = painterResource(id = DesignSystemR.drawable.ic_no_transaction),
                    title = stringResource(id = com.naveenapps.expensemanager.feature.category.R.string.no_transactions_available),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = EmTheme.spacing.xl),
                )
            }
        } else {
            itemsIndexed(
                items = transactions,
                key = { _, item -> "transaction_${item.id}" },
            ) { index, item ->
                EmCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = EmTheme.spacing.lg, end = EmTheme.spacing.lg, bottom = EmTheme.spacing.xxs),
                    position = EmCardPosition.of(index, transactions),
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
                        isRecurring = item.isRecurring,
                        fromAccountName = item.fromAccountName,
                        fromAccountIcon = item.fromAccountIcon.name,
                        fromAccountColor = item.fromAccountIcon.backgroundColor,
                        toAccountName = item.toAccountName,
                        toAccountIcon = item.toAccountIcon?.name,
                        toAccountColor = item.toAccountIcon?.backgroundColor,
                        // Tap opens the transaction for editing, like everywhere else; swiping still
                        // reveals the same Edit action.
                        onClick = { onTransactionClick.invoke(item.id) },
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
            .padding(
                start = EmTheme.spacing.xl,
                end = EmTheme.spacing.lg,
                top = EmTheme.spacing.xl,
                bottom = EmTheme.spacing.sm,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        EmText(
            text = text,
            role = EmTextRole.Overline,
            color = EmTextColor.Supporting,
        )
        trailing?.invoke()
    }
}

@EmPreviews
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
                progress = budget.toBudgetProgress(today = LocalDate(2026, 6, 20), format = { "$$it" }),
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
