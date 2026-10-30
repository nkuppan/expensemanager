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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.components.DashboardWidgetTitle
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.SmallIconAndBackgroundView
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

    AppCardView(modifier = modifier.fillMaxWidth(), cornerSize = AnalysisCardCorner) {
        Column(modifier = Modifier.padding(16.dp)) {
            DashboardWidgetTitle(title = stringResource(R.string.compare_biggest_changes))
            Text(
                text = stringResource(comparison.titleResId) + " · " + comparison.periodsLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            comparison.categoryChanges.forEachIndexed { index, change ->
                if (index > 0) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    )
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
    val name = change.category.titleResId?.let { stringResource(it) } ?: change.category.name
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp)
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
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(R.string.compare_category_detail, change.current, change.previous),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Row(
            modifier = Modifier
                .background(color.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (change.isIncrease) {
                    Icons.AutoMirrored.Filled.TrendingUp
                } else {
                    Icons.AutoMirrored.Filled.TrendingDown
                },
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = change.difference,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = color,
            )
        }
    }
}

@AppPreviewsLightAndDarkMode
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
