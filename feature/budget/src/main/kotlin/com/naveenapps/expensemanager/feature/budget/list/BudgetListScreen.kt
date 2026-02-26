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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.expensemanager.core.common.utils.toPercentString
import com.naveenapps.expensemanager.core.designsystem.components.EmptyItem
import com.naveenapps.expensemanager.core.designsystem.components.LoadingItem
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.core.designsystem.ui.components.IconAndBackgroundView
import com.naveenapps.expensemanager.core.designsystem.ui.utils.ItemSpecModifier
import com.naveenapps.expensemanager.core.domain.usecase.budget.BudgetUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.feature.budget.R
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BudgetListScreen(
    viewModel: BudgetListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

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
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            ExpenseManagerTopAppBar(
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationBackClick = {
                    onAction.invoke(BudgetListAction.ClosePage)
                },
                title = stringResource(R.string.budgets),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onAction.invoke(BudgetListAction.OpenBudgetCreate)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "",
                )
            }
        },
    ) { innerPadding ->
        BudgetListScreenContent(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            state = state,
            onItemClick = {
                onAction.invoke(BudgetListAction.EditBudget(it.id))
            }
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
        LoadingItem(modifier)
        return
    }

    if (state.budgets.isNotEmpty()) {
        LazyColumn(
            modifier = modifier,
            state = scrollState
        ) {
            items(
                state.budgets,
                key = { it.id }
            ) { budget ->
                BudgetItem(
                    name = budget.name,
                    icon = budget.icon,
                    iconBackgroundColor = budget.iconBackgroundColor,
                    progressBarColor = budget.progressBarColor,
                    amount = budget.amount,
                    transactionAmount = budget.transactionAmount,
                    percentage = budget.percent,
                    modifier = Modifier
                        .clickable {
                            onItemClick.invoke(budget)
                        }
                        .then(ItemSpecModifier),
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
        EmptyItem(
            emptyItemText = stringResource(id = R.string.no_budget_available),
            icon = com.naveenapps.expensemanager.core.designsystem.R.drawable.ic_no_budgets,
            modifier = modifier
        )
    }
}

@Composable
fun BudgetItem(
    name: String,
    icon: String,
    iconBackgroundColor: String,
    @ColorRes progressBarColor: Int,
    amount: Amount?,
    transactionAmount: Amount?,
    modifier: Modifier = Modifier,
    percentage: Float = 0.0f,
) {
    val barColor = colorResource(id = progressBarColor)
    val isOverBudget = percentage > 100f
    val clampedProgress = (percentage / 100f).coerceIn(0f, 1f)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // ── Category icon ──────────────────────────────────────────
        IconAndBackgroundView(
            modifier = Modifier,
            icon = icon,
            iconBackgroundColor = iconBackgroundColor,
            name = name,
        )

        Spacer(Modifier.width(14.dp))

        // ── Content ────────────────────────────────────────────────
        Column(modifier = Modifier.weight(1f)) {

            // ── Row 1: Name + Budget amount ────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                if (amount != null) {
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = amount.amountString ?: "",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.3).sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Row 2: Progress bar ────────────────────────────────
            LinearProgressIndicator(
                progress = { clampedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isOverBudget) {
                    MaterialTheme.colorScheme.error
                } else {
                    barColor
                },
                trackColor = barColor.copy(alpha = 0.10f),
                strokeCap = StrokeCap.Round,
            )

            Spacer(Modifier.height(6.dp))

            // ── Row 3: Spent of Total + Percentage badge ───────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${transactionAmount?.amountString ?: "—"} of ${amount?.amountString ?: "—"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                        .copy(alpha = 0.6f),
                )

                Spacer(Modifier.weight(1f))

                // Percentage badge
                val badgeBg = if (isOverBudget) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    barColor.copy(alpha = 0.12f)
                }
                val badgeText = if (isOverBudget) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    barColor
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeBg)
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = percentage.toPercentString(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = badgeText,
                    )
                }
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
) {
    val barColor = colorResource(id = progressBarColor)
    val isOverBudget = percentage > 100f
    val clampedProgress = (percentage / 100f).coerceIn(0f, 1f)

    AppCardView(modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(horizontal = 18.dp, vertical = 16.dp)
                .width(260.dp),
        ) {
            // ── Budget name ────────────────────────────────────────
            Text(
                text = name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.2).sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(14.dp))

            // ── Spent vs Budget row ────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
            ) {
                // Spent amount — large and prominent
                Text(
                    text = transactionAmount ?: "—",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                    ),
                    color = if (isOverBudget) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )

                // "of $amount" — smaller, muted, baseline-aligned
                Text(
                    text = " of ${amount ?: "—"}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Normal,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                        .copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 2.dp),
                )

                Spacer(Modifier.weight(1f))

                // Percentage badge
                val badgeBg = if (isOverBudget) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    barColor.copy(alpha = 0.12f)
                }
                val badgeText = if (isOverBudget) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    barColor
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = percentage.toPercentString(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = badgeText,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Progress bar ───────────────────────────────────────
            LinearProgressIndicator(
                progress = { clampedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isOverBudget) {
                    MaterialTheme.colorScheme.error
                } else {
                    barColor
                },
                trackColor = barColor.copy(alpha = 0.10f),
                strokeCap = StrokeCap.Round,
            )
        }
    }
}

fun getRandomBudgetUiModel(size: Int): List<BudgetUiModel> {
    return buildList {
        repeat(size) {
            add(getBudgetUiModel(it.toString()))
        }
    }
}

private fun getBudgetUiModel(id: String) = BudgetUiModel(
    id = id,
    name = "Cash",
    icon = "account_balance",
    iconBackgroundColor = "#000000",
    amount = Amount(amount = 300.0, amountString = "300.00 ₹"),
    transactionAmount = Amount(amount = 300.0, amountString = "300.00 ₹"),
    progressBarColor = com.naveenapps.expensemanager.core.common.R.color.orange_500,
    percent = 0.9f,
)

@Preview
@Composable
private fun BudgetItemPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        BudgetItem(
            name = "Utilities",
            icon = "account_balance",
            iconBackgroundColor = "#000000",
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
    NaveenAppsPreviewTheme(padding = 0.dp) {
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
    NaveenAppsPreviewTheme(padding = 0.dp) {
        BudgetListScreenContent(
            state = BudgetState(isLoading = true, budgets = emptyList()),
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun BudgetListItemEmptyStatePreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        BudgetListScreenContent(
            state = BudgetState(isLoading = false, budgets = emptyList()),
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun BudgetListItemSuccessStatePreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        BudgetListScreenContent(
            state = BudgetState(isLoading = false, budgets = getRandomBudgetUiModel(5)),
            onAction = {},
        )
    }
}
