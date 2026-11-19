package com.naveenapps.expensemanager.feature.category.transaction

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.common.utils.UiState
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
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.CategoryTransaction
import com.naveenapps.expensemanager.core.model.CategoryTransactionState
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.PieChartData
import com.naveenapps.expensemanager.core.model.isExpense
import com.naveenapps.expensemanager.feature.category.R
import com.naveenapps.expensemanager.feature.category.list.getCategoryData
import com.naveenapps.expensemanager.feature.filter.FilterView
import kotlin.random.Random
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CategoryTransactionTabScreen(
    // False inside Home's tabs, where the bottom bar's centre "+" adds a transaction instead.
    showAddButton: Boolean = true,
    viewModel: CategoryTransactionListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    CategoryTransactionTabScreenContent(
        state = state,
        onAction = viewModel::processAction,
        showAddButton = showAddButton,
    )
}

@Composable
fun CategoryTransactionTabScreenContent(
    state: UiState<CategoryTransactionState>,
    onAction: (CategoryTransactionAction) -> Unit,
    showAddButton: Boolean = true,
) {
    EmScaffold(
        topBar = {
            EmTopBar(
                title = stringResource(R.string.categories),
                actions = {
                    EmIconButton(
                        icon = Icons.Outlined.Edit,
                        contentDescription = stringResource(id = R.string.edit),
                        onClick = {
                            onAction.invoke(CategoryTransactionAction.OpenCategoryList)
                        },
                    )
                },
            )
        },
        floatingActionButton = {
            if (showAddButton) {
                EmFab(
                    icon = Icons.Default.Add,
                    contentDescription = "",
                    onClick = {
                        onAction.invoke(CategoryTransactionAction.OpenTransactionCreate)
                    },
                )
            }
        },
    ) { innerPadding ->
        CategoryTransactionListScreenContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
            state = state,
            changeChart = {
                onAction.invoke(CategoryTransactionAction.SwitchCategoryType)
            },
            onItemClick = {
                onAction.invoke(CategoryTransactionAction.OpenCategoryDetails(it))
            },
        )
    }
}

@Composable
private fun CategoryTransactionListScreenContent(
    state: UiState<CategoryTransactionState>,
    changeChart: () -> Unit,
    onItemClick: (CategoryTransaction) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        UiState.Empty -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = EmTheme.spacing.xxxl),
                ) {
                    EmText(
                        text = stringResource(id = R.string.no_transactions_available),
                        role = EmTextRole.ItemTitle,
                        color = EmTextColor.Supporting,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        UiState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                EmLoading()
            }
        }

        is UiState.Success -> {
            val isExpense = state.data.categoryType.isExpense()
            val categoryTransactions = state.data.categoryTransactions

            LazyColumn(
                modifier = modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    bottom = 88.dp,
                ),
            ) {
                item {
                    FilterView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = EmTheme.spacing.sm),
                    )
                }
                // ── Pie chart card ─────────────────────────────────
                item(key = "chart") {
                    CategorySpendingChartCard(
                        state = state.data,
                        centerLabel = if (isExpense) {
                            stringResource(id = R.string.expense)
                        } else {
                            stringResource(id = R.string.income)
                        },
                        onChartClick = changeChart,
                        modifier = Modifier.padding(vertical = EmTheme.spacing.md, horizontal = EmTheme.spacing.lg),
                        footer = {
                            // Tap hint
                            EmText(
                                text = stringResource(R.string.tap_chart_to_switch_to) +
                                    getCategoryGroupTitleInverted(isExpense),
                                modifier = Modifier.padding(bottom = EmTheme.spacing.md),
                                role = EmTextRole.Supporting,
                                color = EmTextColor.Supporting,
                            )
                        },
                    )
                }

                item(key = "section_header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = EmTheme.spacing.sm,
                                bottom = EmTheme.spacing.sm,
                                start = EmTheme.spacing.lg,
                                end = EmTheme.spacing.lg,
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        EmText(
                            text = getCategoryGroupTitle(isExpense),
                            modifier = Modifier.weight(1f),
                            role = EmTextRole.Overline,
                            color = EmTextColor.Supporting,
                        )
                        EmText(
                            text = pluralStringResource(
                                R.plurals.category_count,
                                categoryTransactions.size,
                                categoryTransactions.size,
                            ),
                            role = EmTextRole.Supporting,
                            color = EmTextColor.Supporting,
                        )
                    }
                }

                // ── Empty state ────────────────────────────────────
                if (categoryTransactions.isEmpty()) {
                    item(key = "empty") {
                        EmEmptyState(
                            illustration = painterResource(id = com.naveenapps.expensemanager.core.designsystem.R.drawable.ic_no_grouping_available),
                            title = stringResource(
                                id = R.string.no_transactions_available,
                            ),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    top = EmTheme.spacing.xxl,
                                    start = EmTheme.spacing.lg,
                                    end = EmTheme.spacing.lg,
                                ),
                        )
                    }
                } else {
                    // ── Category list — single grouped card ────────
                    categorySpendingItems(
                        categoryTransactions = categoryTransactions,
                        onItemClick = onItemClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun getCategoryGroupTitle(isExpense: Boolean): String = if (isExpense) {
    stringResource(R.string.expense)
} else {
    stringResource(R.string.income)
}

@Composable
private fun getCategoryGroupTitleInverted(isExpense: Boolean): String = getCategoryGroupTitle(isExpense.not())

// ═══════════════════════════════════════════════════════════════════════
//  Preview data
// ═══════════════════════════════════════════════════════════════════════

val getPieChartData = listOf(
    PieChartData("Chrome", 34.68f, "#43A546"),
    PieChartData("Firefox", 16.60F, "#F44336"),
    PieChartData("Safari", 16.15F, "#166EF7"),
    PieChartData("Internet Explorer", 15.62F, "#121212"),
)

fun getRandomCategoryTransactionData(): CategoryTransactionState = CategoryTransactionState(
    pieChartData = emptyList(),
    totalAmount = Amount(3000000.0, "3000000.00$"),
    categoryTransactions = buildList {
        repeat(15) {
            add(
                CategoryTransaction(
                    category = getCategoryData(it, CategoryType.EXPENSE),
                    amount = Amount(300.0, "300.00$"),
                    percent = Random(100).nextFloat(),
                    transaction = emptyList(),
                ),
            )
        }
    },
    categoryType = CategoryType.EXPENSE,
)

fun getUiState(): UiState<CategoryTransactionState> = UiState.Success(data = getRandomCategoryTransactionData())

@EmPreviews
@Composable
private fun CategoryTransactionTabScreenPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        CategoryTransactionTabScreenContent(
            state = getUiState(),
            onAction = {},
        )
    }
}
