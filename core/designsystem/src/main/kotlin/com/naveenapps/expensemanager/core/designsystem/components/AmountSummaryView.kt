package com.naveenapps.expensemanager.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.R
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountSize
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountText
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmFitText
import com.naveenapps.expensemanager.core.designsystem.em.EmIcon
import com.naveenapps.expensemanager.core.designsystem.em.EmPreviews
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.theme.tint

@Composable
fun WidgetHeader(
    title: String,
    subTitle: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EmText(
            text = title,
            role = EmTextRole.SectionTitle,
        )
        Spacer(Modifier.width(EmTheme.spacing.sm))
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(EmTheme.shapes.pill)
                .background(
                    EmTheme.colors.scheme.onSurfaceVariant,
                ),
        )
        Spacer(Modifier.width(EmTheme.spacing.sm))
        EmText(
            text = subTitle,
            modifier = Modifier.weight(1f),
            role = EmTextRole.Supporting,
            color = EmTextColor.Supporting,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun AmountInfoWidget(
    expenseAmount: String,
    incomeAmount: String,
    balanceAmount: String,
    transactionPeriod: String,
    modifier: Modifier = Modifier,
) {
    val incomeColor = EmTheme.colors.income
    val expenseColor = EmTheme.colors.expense

    EmCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(EmTheme.spacing.lg),
        ) {
            WidgetHeader(
                title = stringResource(id = R.string.transaction_summary),
                subTitle = transactionPeriod,
            )

            Spacer(Modifier.height(EmTheme.spacing.lg))

            // ── Income & Expense — side by side ────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.md),
            ) {
                SummaryCard(
                    label = stringResource(id = R.string.income),
                    amount = incomeAmount,
                    icon = Icons.Default.ArrowDownward,
                    tintColor = incomeColor,
                    modifier = Modifier.weight(1f),
                )
                SummaryCard(
                    label = stringResource(id = R.string.expense),
                    amount = expenseAmount,
                    icon = Icons.Default.ArrowUpward,
                    tintColor = expenseColor,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(EmTheme.spacing.md))

            // ── Balance row ────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(EmTheme.shapes.sm)
                    .background(
                        EmTheme.colors.scheme.surfaceContainerHighest
                            .copy(alpha = 0.5f),
                    )
                    .padding(horizontal = EmTheme.spacing.md, vertical = EmTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                EmText(
                    text = stringResource(id = R.string.total),
                    color = EmTheme.colors.scheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                EmAmountText(
                    amount = balanceAmount,
                    size = EmAmountSize.Large,
                )
            }
        }
    }
}

@Composable
fun SummaryCard(
    label: String,
    amount: String,
    icon: ImageVector,
    tintColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(EmTheme.shapes.sm)
            .background(tintColor.tint())
            .padding(EmTheme.spacing.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(EmTheme.shapes.sm)
                    .background(tintColor.tint()),
                contentAlignment = Alignment.Center,
            ) {
                EmIcon(
                    icon = icon,
                    contentDescription = null,
                    size = EmTheme.icons.sm,
                    tint = tintColor,
                )
            }
            Spacer(Modifier.width(EmTheme.spacing.sm))
            EmFitText(
                text = label,
                role = EmTextRole.Supporting,
                color = EmTheme.colors.scheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(EmTheme.spacing.sm))

        EmAmountText(
            amount = amount,
            color = tintColor,
        )
    }
}

@Composable
fun AmountInfoWidgetCompact(
    expenseAmount: String,
    incomeAmount: String,
    balanceAmount: String,
    transactionPeriod: String,
    modifier: Modifier = Modifier,
) {
    val incomeColor = EmTheme.colors.income
    val expenseColor = EmTheme.colors.expense

    EmCard(modifier = modifier) {
        Column(modifier = Modifier.padding(EmTheme.spacing.md)) {
            // ── Header ─────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                EmText(
                    text = stringResource(id = R.string.transaction_summary),
                    role = EmTextRole.Label,
                    color = EmTheme.colors.scheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(EmTheme.spacing.xs))
                Box(
                    Modifier
                        .size(3.dp)
                        .clip(EmTheme.shapes.pill)
                        .background(
                            EmTheme.colors.scheme.onSurfaceVariant,
                        ),
                )
                Spacer(Modifier.width(EmTheme.spacing.xs))
                EmText(
                    text = transactionPeriod,
                    modifier = Modifier.weight(1f),
                    role = EmTextRole.Supporting,
                    color = EmTheme.colors.scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.height(EmTheme.spacing.sm))

            // ── Income / Expense / Balance in one row ──────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
            ) {
                CompactAmountCell(
                    label = stringResource(id = R.string.income),
                    amount = incomeAmount,
                    icon = Icons.Default.ArrowDownward,
                    tintColor = incomeColor,
                    modifier = Modifier.weight(1f),
                )
                CompactAmountCell(
                    label = stringResource(id = R.string.expense),
                    amount = expenseAmount,
                    icon = Icons.Default.ArrowUpward,
                    tintColor = expenseColor,
                    modifier = Modifier.weight(1f),
                )
                CompactBalanceCell(
                    amount = balanceAmount,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun CompactAmountCell(
    label: String,
    amount: String,
    icon: ImageVector,
    tintColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(EmTheme.shapes.sm)
            .background(tintColor.tint())
            .padding(EmTheme.spacing.sm),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            EmIcon(
                icon = icon,
                contentDescription = null,
                size = EmTheme.icons.sm,
                tint = tintColor,
            )
            Spacer(Modifier.width(EmTheme.spacing.xs))
            EmFitText(
                text = label,
                role = EmTextRole.Supporting,
                color = EmTheme.colors.scheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(EmTheme.spacing.xs))
        EmAmountText(
            amount = amount,
            color = tintColor,
            size = EmAmountSize.Small,
        )
    }
}

@Composable
private fun CompactBalanceCell(
    amount: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(EmTheme.shapes.sm)
            .background(
                EmTheme.colors.scheme.surfaceContainerHighest
                    .copy(alpha = 0.45f),
            )
            .padding(EmTheme.spacing.sm),
    ) {
        EmText(
            text = stringResource(id = R.string.total),
            role = EmTextRole.Supporting,
            color = EmTheme.colors.scheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(EmTheme.spacing.xs))
        EmAmountText(
            amount = amount,
            size = EmAmountSize.Small,
        )
    }
}

@EmPreviews
@Composable
private fun AmountViewPreview() {
    val amount = "100000.0$"
    ExpenseManagerPreviewTheme {
        Column(
            modifier = Modifier.padding(EmTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.lg),
        ) {
            AmountInfoWidget(
                expenseAmount = amount,
                incomeAmount = amount,
                balanceAmount = amount,
                transactionPeriod = "This Month(Oct 2023)",
            )
            AmountInfoWidgetCompact(
                expenseAmount = amount,
                incomeAmount = amount,
                balanceAmount = amount,
                transactionPeriod = "This Month(Oct 2023)",
            )
        }
    }
}
