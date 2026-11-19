package com.naveenapps.expensemanager.feature.category.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.common.utils.toPercentString
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountSize
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountText
import com.naveenapps.expensemanager.core.designsystem.em.EmPreviews
import com.naveenapps.expensemanager.core.designsystem.em.EmProgress
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.IconAndBackgroundView
import com.naveenapps.expensemanager.core.designsystem.ui.components.SmallIconAndBackgroundView
import com.naveenapps.expensemanager.core.designsystem.ui.extensions.toColor

@Composable
fun CategoryTransactionItem(
    modifier: Modifier = Modifier,
    name: String,
    icon: String,
    iconBackgroundColor: String,
    amount: String,
    percentage: Float,
    customImagePath: String? = null,
) {
    val categoryColor = iconBackgroundColor.toColor()

    Row(
        modifier = modifier
            .padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconAndBackgroundView(
            modifier = Modifier,
            icon = icon,
            iconBackgroundColor = iconBackgroundColor,
            name = name,
            customImagePath = customImagePath,
        )

        Spacer(Modifier.width(EmTheme.spacing.md))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
        ) {
            // ── Name + Amount ──────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                EmText(
                    text = name,
                    modifier = Modifier.weight(1f),
                    role = EmTextRole.ItemTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.width(EmTheme.spacing.md))

                EmAmountText(
                    amount = amount,
                )
            }

            // ── Progress bar + Percentage ──────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                EmProgress(
                    progress = (percentage / 100f).coerceIn(0f, 1f),
                    modifier = Modifier
                        .weight(1f),
                    color = categoryColor,
                )

                Spacer(Modifier.width(EmTheme.spacing.sm))

                EmText(
                    text = percentage.toPercentString(),
                    role = EmTextRole.Supporting,
                    color = categoryColor,
                )
            }
        }
    }
}

@Composable
fun CategoryTransactionSmallItem(
    name: String,
    icon: String,
    iconBackgroundColor: String,
    amount: String,
    modifier: Modifier = Modifier,
    customImagePath: String? = null,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SmallIconAndBackgroundView(
            icon = icon,
            iconBackgroundColor = iconBackgroundColor,
            modifier = Modifier,
            name = name,
            iconSize = 12.dp,
            customImagePath = customImagePath,
        )

        Spacer(Modifier.width(EmTheme.spacing.sm))

        EmText(
            text = name,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(Modifier.width(EmTheme.spacing.sm))

        EmAmountText(
            amount = amount,
            color = EmTheme.colors.scheme.onSurfaceVariant,
            size = EmAmountSize.Small,
        )
    }
}

@EmPreviews
@Composable
private fun CategoryTransactionSmallItemPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        CategoryTransactionSmallItem(
            modifier = Modifier
                .fillMaxWidth(),
            name = "Utilities",
            icon = "account_balance",
            iconBackgroundColor = "#000000",
            amount = "$100.00",
        )
    }
}

@EmPreviews
@Composable
private fun CategoryTransactionItemPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        CategoryTransactionItem(
            modifier = Modifier
                .fillMaxWidth(),
            name = "Utilities",
            icon = "account_balance",
            iconBackgroundColor = "#000000",
            amount = "$100.00",
            percentage = 0.5f,
        )
    }
}
