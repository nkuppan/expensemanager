package com.naveenapps.expensemanager.feature.budget.list

import androidx.annotation.ColorRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.common.utils.toPercentString
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountSize
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountText
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmCardPosition
import com.naveenapps.expensemanager.core.designsystem.em.EmEmptyState
import com.naveenapps.expensemanager.core.designsystem.em.EmFab
import com.naveenapps.expensemanager.core.designsystem.em.EmLoading
import com.naveenapps.expensemanager.core.designsystem.em.EmProgress
import com.naveenapps.expensemanager.core.designsystem.em.EmScaffold
import com.naveenapps.expensemanager.core.designsystem.em.EmSnackbarHostState
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBar
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBarNavigation
import com.naveenapps.expensemanager.core.designsystem.em.emColorOf
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.theme.tint
import com.naveenapps.expensemanager.core.designsystem.ui.utils.ItemSpecModifier
import com.naveenapps.expensemanager.core.domain.usecase.budget.BudgetUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.BudgetTitle
import com.naveenapps.expensemanager.feature.budget.R
import com.naveenapps.expensemanager.feature.budget.text
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BudgetListScreen(
    viewModel: BudgetListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BudgetListScreenContent(
        state = state,
        onAction = viewModel::processAction,
    )
}

@Composable
private fun BudgetListScreenContent(
    state: BudgetState,
    onAction: (BudgetListAction) -> Unit,
) {
    val snackbarHostState = remember { EmSnackbarHostState() }

    EmScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            EmTopBar(
                navigation = EmTopBarNavigation.Back,
                onNavigationClick = {
                    onAction.invoke(BudgetListAction.ClosePage)
                },
                title = stringResource(R.string.budgets),
            )
        },
        floatingActionButton = {
            EmFab(
                icon = Icons.Default.Add,
                contentDescription = "",
                onClick = {
                    onAction.invoke(BudgetListAction.OpenBudgetCreate)
                },
            )
        },
    ) { innerPadding ->
        BudgetListScreenContent(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            state = state,
            onItemClick = {
                onAction.invoke(BudgetListAction.EditBudget(it.id))
            },
        )
    }
}

@Composable
private fun BudgetListScreenContent(
    state: BudgetState,
    onItemClick: ((BudgetUiModel) -> Unit),
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberLazyListState()

    if (state.isLoading) {
        EmLoading(modifier = modifier)
        return
    }

    if (state.budgets.isNotEmpty()) {
        LazyColumn(
            modifier = modifier,
            state = scrollState,
        ) {
            itemsIndexed(
                items = state.budgets,
                key = { _, item -> item.id },
            ) { index, budget ->
                BudgetItemWithCard(
                    name = budget.title.text(),
                    progressBarColor = budget.progressBarColor,
                    amount = budget.amount,
                    transactionAmount = budget.transactionAmount,
                    percentage = budget.percent,
                    otherCurrenciesExcluded = budget.otherCurrenciesExcluded,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.xxs),
                    position = EmCardPosition.of(index, state.budgets),
                    onItemClick = { onItemClick.invoke(budget) },
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
    } else {
        EmEmptyState(
            illustration = painterResource(id = com.naveenapps.expensemanager.core.designsystem.R.drawable.ic_no_budgets),
            title = stringResource(id = R.string.no_budget_available),
            modifier = modifier,
        )
    }
}

@Composable
fun BudgetItemWithCard(
    name: String,
    @ColorRes progressBarColor: Int,
    amount: Amount?,
    transactionAmount: Amount?,
    modifier: Modifier = Modifier,
    percentage: Float = 0.0f,
    position: EmCardPosition = EmCardPosition.Single,
    selected: Boolean = false,
    onItemClick: () -> Unit = {},
    otherCurrenciesExcluded: Boolean = false,
) {
    EmCard(
        position = position,
        selected = selected,
        modifier = modifier,
    ) {
        BudgetItem(
            name = name,
            progressBarColor = progressBarColor,
            amount = amount,
            transactionAmount = transactionAmount,
            percentage = percentage,
            otherCurrenciesExcluded = otherCurrenciesExcluded,
            modifier = Modifier
                .clickable {
                    onItemClick.invoke()
                }
                .then(ItemSpecModifier),
        )
    }
}

@Composable
fun BudgetItem(
    name: String,
    @ColorRes progressBarColor: Int,
    amount: Amount?,
    transactionAmount: Amount?,
    modifier: Modifier = Modifier,
    percentage: Float = 0.0f,
    otherCurrenciesExcluded: Boolean = false,
) {
    val barColor = emColorOf(progressBarColor)
    val isOverBudget = percentage > 100f
    val clampedProgress = (percentage / 100f).coerceIn(0f, 1f)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            // ── Row 1: Month label + Budget amount ─────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                EmText(
                    text = name,
                    modifier = Modifier.weight(1f),
                    role = EmTextRole.ItemTitle,
                    color = EmTextColor.Supporting,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (amount != null) {
                    EmAmountText(
                        amount = amount.amountString ?: "",
                    )
                }
            }

            Spacer(Modifier.height(EmTheme.spacing.sm))

            // ── Row 2: Progress bar ────────────────────────────────
            EmProgress(
                progress = clampedProgress,
                color = if (isOverBudget) {
                    EmTheme.colors.scheme.error
                } else {
                    barColor
                },
            )

            Spacer(Modifier.height(EmTheme.spacing.sm))

            // ── Row 3: Spent of Total + Percentage badge ───────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                EmText(
                    text = "${transactionAmount?.amountString ?: "—"} of ${amount?.amountString ?: "—"}",
                    role = EmTextRole.Supporting,
                    color = EmTheme.colors.scheme.onSurfaceVariant,
                )

                Spacer(Modifier.weight(1f))

                val badgeBg = if (isOverBudget) {
                    EmTheme.colors.scheme.errorContainer
                } else {
                    barColor.tint()
                }
                val badgeText = if (isOverBudget) {
                    EmTheme.colors.scheme.onErrorContainer
                } else {
                    barColor
                }

                Box(
                    modifier = Modifier
                        .clip(EmTheme.shapes.sm)
                        .background(badgeBg)
                        .padding(horizontal = EmTheme.spacing.sm, vertical = EmTheme.spacing.xxs),
                ) {
                    EmText(
                        text = percentage.toPercentString(),
                        role = EmTextRole.Supporting,
                        color = badgeText,
                    )
                }
            }

            if (otherCurrenciesExcluded) {
                Spacer(Modifier.height(4.dp))
                EmText(
                    text = stringResource(R.string.budget_other_currencies_excluded),
                    role = EmTextRole.Supporting,
                    color = EmTextColor.Supporting,
                )
            }
        }
    }
}

@Composable
fun DashBoardBudgetItem(
    name: String,
    @ColorRes progressBarColor: Int,
    amount: String?,
    transactionAmount: String?,
    modifier: Modifier = Modifier,
    percentage: Float = 0.0f,
    onItemClick: () -> Unit = {},
) {
    val barColor = emColorOf(progressBarColor)
    val isOverBudget = percentage > 100f
    val clampedProgress = (percentage / 100f).coerceIn(0f, 1f)

    EmCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .clickable {
                    onItemClick.invoke()
                }
                .padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.lg)
                .fillMaxWidth(),
        ) {
            // ── Budget name ────────────────────────────────────────
            EmText(
                text = name,
                role = EmTextRole.ItemTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(EmTheme.spacing.md))

            // ── Spent vs Budget row ────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
            ) {
                // Spent amount — large and prominent
                EmAmountText(
                    amount = transactionAmount ?: "—",
                    color = if (isOverBudget) {
                        EmTheme.colors.scheme.error
                    } else {
                        EmTheme.colors.scheme.onSurface
                    },
                    size = EmAmountSize.Large,
                )

                // "of $amount" — smaller, muted, baseline-aligned
                EmText(
                    text = " of ${amount ?: "—"}",
                    modifier = Modifier.padding(bottom = EmTheme.spacing.xxs),
                    role = EmTextRole.Supporting,
                    color = EmTheme.colors.scheme.onSurfaceVariant,
                )

                Spacer(Modifier.weight(1f))

                // Percentage badge
                val badgeBg = if (isOverBudget) {
                    EmTheme.colors.scheme.errorContainer
                } else {
                    barColor.tint()
                }
                val badgeText = if (isOverBudget) {
                    EmTheme.colors.scheme.onErrorContainer
                } else {
                    barColor
                }

                Box(
                    modifier = Modifier
                        .clip(EmTheme.shapes.sm)
                        .background(badgeBg)
                        .padding(horizontal = EmTheme.spacing.sm, vertical = EmTheme.spacing.xs),
                ) {
                    EmText(
                        text = percentage.toPercentString(),
                        role = EmTextRole.Label,
                        color = badgeText,
                    )
                }
            }

            Spacer(Modifier.height(EmTheme.spacing.md))

            // ── Progress bar ───────────────────────────────────────
            EmProgress(
                progress = clampedProgress,
                color = if (isOverBudget) {
                    EmTheme.colors.scheme.error
                } else {
                    barColor
                },
            )
        }
    }
}

fun getRandomBudgetUiModel(size: Int): List<BudgetUiModel> = buildList {
    repeat(size) {
        add(getBudgetUiModel(it.toString()))
    }
}

private fun getBudgetUiModel(id: String) = BudgetUiModel(
    id = id,
    title = BudgetTitle.ThisMonth,
    selectedMonth = "June 2026",
    amount = Amount(amount = 300.0, amountString = "300.00 ₹"),
    transactionAmount = Amount(amount = 300.0, amountString = "300.00 ₹"),
    progressBarColor = com.naveenapps.expensemanager.core.common.R.color.orange_500,
    percent = 0.9f,
)

@Preview
@Composable
private fun BudgetItemPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        BudgetItem(
            name = "Jun 2026 Budget",
            amount = Amount(amount = 300.0, amountString = "300.00 ₹"),
            transactionAmount = Amount(amount = 300.0, amountString = "300.00 ₹"),
            modifier = ItemSpecModifier,
            progressBarColor = com.naveenapps.expensemanager.core.common.R.color.orange_500,
            percentage = 78.8f,
        )
    }
}

@Preview
@Composable
private fun DashboardBudgetItemPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        DashBoardBudgetItem(
            name = "This Month Budget",
            progressBarColor = com.naveenapps.expensemanager.core.common.R.color.orange_500,
            amount = "$100.00",
            transactionAmount = "$78.00",
            modifier = ItemSpecModifier,
            percentage = 78.8f,
        )
    }
}

@Preview
@Composable
private fun BudgetListItemLoadingStatePreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        BudgetListScreenContent(
            state = BudgetState(isLoading = true, budgets = emptyList()),
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun BudgetListItemEmptyStatePreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        BudgetListScreenContent(
            state = BudgetState(isLoading = false, budgets = emptyList()),
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun BudgetListItemSuccessStatePreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        BudgetListScreenContent(
            state = BudgetState(isLoading = false, budgets = getRandomBudgetUiModel(5)),
            onAction = {},
        )
    }
}
