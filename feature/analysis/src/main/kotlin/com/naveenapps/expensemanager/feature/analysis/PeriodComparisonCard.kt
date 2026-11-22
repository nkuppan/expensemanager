package com.naveenapps.expensemanager.feature.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountText
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmDivider
import com.naveenapps.expensemanager.core.designsystem.em.EmIcon
import com.naveenapps.expensemanager.core.designsystem.em.EmPreviews
import com.naveenapps.expensemanager.core.designsystem.em.EmSectionHeader
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.theme.tint
import com.naveenapps.expensemanager.core.designsystem.ui.components.SmallIconAndBackgroundView
import com.naveenapps.expensemanager.core.designsystem.ui.utils.displayName
import com.naveenapps.expensemanager.core.designsystem.ui.utils.getExpenseColor
import com.naveenapps.expensemanager.core.designsystem.ui.utils.getIncomeColor
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.StoredIcon
import java.util.Date

/**
 * The categories whose spending moved most since the previous period (income and expense
 * totals with their change are on the overview card). More spending is red, less is green.
 */
@Composable
fun PeriodComparisonCard(
    comparison: PeriodComparisonUi,
    modifier: Modifier = Modifier,
    onCategoryClick: (String) -> Unit = {},
) {
    val good = getIncomeColor()
    val bad = getExpenseColor()

    EmCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(EmTheme.spacing.lg)) {
            EmSectionHeader(title = stringResource(R.string.compare_biggest_changes))
            EmText(
                text = stringResource(comparison.titleResId) + " · " + comparison.periodsLabel,
                role = EmTextRole.Supporting,
                color = EmTextColor.Supporting,
            )
            Spacer(modifier = Modifier.height(EmTheme.spacing.sm))
            comparison.categoryChanges.forEachIndexed { index, change ->
                if (index > 0) {
                    EmDivider()
                }
                CategoryChangeRow(
                    change = change,
                    color = if (change.isIncrease) bad else good,
                    onClick = { onCategoryClick(change.category.id) },
                )
            }
        }
    }
}

@Composable
private fun CategoryChangeRow(
    change: CategoryChangeUi,
    color: Color,
    onClick: () -> Unit,
) {
    val name = change.category.displayName()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = EmTheme.spacing.sm)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SmallIconAndBackgroundView(
            icon = change.category.storedIcon.name,
            iconBackgroundColor = change.category.storedIcon.backgroundColor,
            name = name,
            iconSize = 12.dp,
            customImagePath = change.category.storedIcon.customImagePath,
        )
        Spacer(modifier = Modifier.width(EmTheme.spacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            EmText(
                text = name,
                role = EmTextRole.ItemTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            EmText(
                text = stringResource(R.string.compare_category_detail, change.current, change.previous),
                role = EmTextRole.Supporting,
                color = EmTextColor.Supporting,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.width(EmTheme.spacing.md))
        Row(
            modifier = Modifier
                .background(color.tint(), EmTheme.shapes.sm)
                .padding(horizontal = EmTheme.spacing.sm, vertical = EmTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EmIcon(
                icon = if (change.isIncrease) {
                    Icons.AutoMirrored.Filled.TrendingUp
                } else {
                    Icons.AutoMirrored.Filled.TrendingDown
                },
                contentDescription = null,
                size = EmTheme.icons.sm,
                tint = color,
            )
            Spacer(modifier = Modifier.width(EmTheme.spacing.xs))
            EmAmountText(
                amount = change.difference,
                color = color,
            )
        }
    }
}

@EmPreviews
@Composable
private fun PeriodComparisonCardPreview() {
    val food = Category(
        id = "1",
        name = "Food",
        type = CategoryType.EXPENSE,
        storedIcon = StoredIcon(name = "restaurant", backgroundColor = "#F44336"),
        createdOn = Date(),
        updatedOn = Date(),
    )
    ExpenseManagerPreviewTheme(padding = 16.dp) {
        PeriodComparisonCard(
            comparison = PeriodComparisonUi(
                titleResId = R.string.compare_title_month,
                periodsLabel = "1–3 Oct vs 1–3 Sep",
                expense = ComparisonMetricUi("₹4,200", "₹3,750", 0.12),
                income = ComparisonMetricUi("₹25,000", "₹25,000", 0.0),
                categoryChanges = listOf(
                    CategoryChangeUi(food, "₹1,800", "₹1,200", "+₹600", isIncrease = true),
                    CategoryChangeUi(food.copy(id = "2", name = "Travel"), "₹200", "₹950", "−₹750", isIncrease = false),
                ),
            ),
        )
    }
}
