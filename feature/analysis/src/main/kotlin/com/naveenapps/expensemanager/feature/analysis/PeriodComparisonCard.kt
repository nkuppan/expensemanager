package com.naveenapps.expensemanager.feature.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.components.DashboardWidgetTitle
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.SmallIconAndBackgroundView
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.StoredIcon
import java.util.Date
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * The selected period against the previous one: spending and income side by side with a change
 * badge, then the categories that moved most. Colours follow the app's meaning, not the
 * direction: more spending is red, more income is green.
 */
@Composable
fun PeriodComparisonCard(
    comparison: PeriodComparisonUi,
    modifier: Modifier = Modifier,
    onCategoryClick: (String) -> Unit = {},
) {
    val good = colorResource(id = com.naveenapps.expensemanager.core.common.R.color.green_500)
    val bad = colorResource(id = com.naveenapps.expensemanager.core.common.R.color.red_500)

    AppCardView(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            DashboardWidgetTitle(title = stringResource(comparison.titleResId))
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = comparison.periodsLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                ComparisonMetric(
                    label = stringResource(com.naveenapps.expensemanager.core.designsystem.R.string.expense),
                    metric = comparison.expense,
                    upColor = bad,
                    downColor = good,
                    modifier = Modifier.weight(1f),
                )
                VerticalDivider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 12.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                ComparisonMetric(
                    label = stringResource(com.naveenapps.expensemanager.core.designsystem.R.string.income),
                    metric = comparison.income,
                    upColor = good,
                    downColor = bad,
                    modifier = Modifier.weight(1f),
                )
            }

            if (comparison.categoryChanges.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.compare_biggest_changes),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                comparison.categoryChanges.forEach { change ->
                    CategoryChangeRow(
                        change = change,
                        color = if (change.isIncrease) bad else good,
                        onClick = { onCategoryClick(change.category.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ComparisonMetric(
    label: String,
    metric: ComparisonMetricUi,
    upColor: Color,
    downColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = metric.current,
            style = MaterialTheme.typography.titleMedium.copy(letterSpacing = (-0.3).sp),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        ChangeBadge(change = metric.change, upColor = upColor, downColor = downColor)
        Text(
            text = stringResource(R.string.compare_was, metric.previous),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** "↑ 12%" / "↓ 8%" in a tinted pill, "Same" when flat, or "New" when there was nothing before. */
@Composable
private fun ChangeBadge(change: Double?, upColor: Color, downColor: Color) {
    val percent = change?.let { (abs(it) * 100).roundToInt() }
    val (text, color, icon) = when {
        change == null -> Triple(stringResource(R.string.compare_new), MaterialTheme.colorScheme.primary, null)
        percent == 0 -> Triple(stringResource(R.string.compare_same), MaterialTheme.colorScheme.onSurfaceVariant, null)
        change > 0 -> Triple("$percent%", upColor, Icons.AutoMirrored.Filled.TrendingUp)
        else -> Triple("$percent%", downColor, Icons.AutoMirrored.Filled.TrendingDown)
    }
    Row(
        modifier = Modifier
            .padding(vertical = 2.dp)
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
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
            .padding(vertical = 8.dp)
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
        Text(
            text = change.difference,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
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
