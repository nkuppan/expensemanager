package com.naveenapps.expensemanager.feature.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.MoneyOff
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.components.DashboardWidgetTitle
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardViewDefaults
import com.naveenapps.expensemanager.core.designsystem.ui.components.IconAndBackgroundView
import com.naveenapps.expensemanager.core.designsystem.ui.utils.IconSpecModifier
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.model.TransactionUiItem
import com.naveenapps.expensemanager.feature.category.transaction.CategoryTransactionItem
import com.naveenapps.expensemanager.feature.transaction.list.TransactionItem
import com.naveenapps.expensemanager.feature.transaction.list.getTransactionItem
import java.time.DayOfWeek
import java.time.format.TextStyle as DayNameStyle
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import androidx.compose.ui.platform.LocalLocale

/**
 * Titles above the grouped-card lists. The fixed height is the height "View all" gives the title
 * when it's present, so the gap to the first card is the same with or without the button (and
 * matches the Transactions title on Home).
 */
private val ListTitleModifier = Modifier
    .heightIn(min = 48.dp)

// Category rows shown; the rest are one "View all" away on the Category screen.
private const val MAX_CATEGORY_ROWS = 6

/**
 * Insights under the Analysis chart, for the same filtered period:
 * plain-language highlights, where the money goes, weekday pattern and biggest expenses.
 *
 * Category and transaction rows reuse the shared app components so they read the same as the
 * Category and Transactions screens. The weekday chart is an emphasis chart: busiest day in the
 * brand colour, the rest neutral, values in text colours.
 */
@Composable
fun AnalysisInsightsSection(
    insights: AnalysisInsightsUi,
    modifier: Modifier = Modifier,
    onTransactionClick: (String) -> Unit = {},
    onCategoryClick: (String) -> Unit = {},
    onViewAllCategories: () -> Unit = {},
) {
    if (!insights.hasExpenses) return
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HighlightsCard(insights)
        CategoryBreakdownSection(insights.categories, onCategoryClick, onViewAllCategories)
        WeekdayCard(insights.weekdays, insights.busiestWeekday)
        if (insights.biggestExpenses.isNotEmpty()) {
            BiggestExpensesSection(insights.biggestExpenses, onTransactionClick)
        }
    }
}

/** Same title as the other Analysis / Home cards (e.g. "Average and projected"). */
@Composable
private fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    onViewAllClick: (() -> Unit)? = null,
) {
    DashboardWidgetTitle(title = text, modifier = modifier, onViewAllClick = onViewAllClick)
}

@Composable
private fun Category.displayName(): String = titleResId?.let { stringResource(it) } ?: name

private fun DayOfWeek.fullName(): String = getDisplayName(DayNameStyle.FULL, Locale.getDefault())
    .replaceFirstChar { it.titlecase(Locale.getDefault()) }

// region Highlights

/**
 * Three stat rows instead of sentences, so the specific numbers stand out: a tile, a small
 * label, the subject in bold (category / day) and a big percentage on the right. Savings uses the
 * app's income/expense colours; everything else stays in text colours plus the brand accent.
 */
@Composable
private fun HighlightsCard(insights: AnalysisInsightsUi) {
    val top = insights.categories.firstOrNull()
    val busiest = insights.busiestWeekday
    val busiestAmount = insights.weekdays.firstOrNull { it.dayOfWeek == busiest }?.amount
    val savingsRate = insights.savingsRate
    if (top == null && busiest == null && savingsRate == null) return

    val incomeColor = colorResource(id = com.naveenapps.expensemanager.core.common.R.color.green_500)
    val expenseColor = colorResource(id = com.naveenapps.expensemanager.core.common.R.color.red_500)
    val accent = MaterialTheme.colorScheme.primary

    AppCardView(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionTitle(stringResource(R.string.insights_title))
            Spacer(modifier = Modifier.height(8.dp))

            val rows = buildList<@Composable () -> Unit> {
                if (top != null) {
                    add {
                        HighlightRow(
                            icon = {
                                IconAndBackgroundView(
                                    icon = top.category.storedIcon.name,
                                    iconBackgroundColor = top.category.storedIcon.backgroundColor,
                                    name = top.category.displayName(),
                                    customImagePath = top.category.storedIcon.customImagePath,
                                )
                            },
                            label = stringResource(R.string.highlight_top_category),
                            value = top.category.displayName(),
                            detail = top.amount,
                            percent = (top.share * 100).roundToInt(),
                            percentCaption = stringResource(R.string.highlight_of_spending),
                            percentColor = accent,
                        )
                    }
                }
                if (busiest != null) {
                    add {
                        HighlightRow(
                            icon = { HighlightIconTile(Icons.Outlined.CalendarMonth, accent) },
                            label = stringResource(R.string.highlight_busiest_day),
                            value = busiest.fullName(),
                            detail = busiestAmount,
                            percent = (insights.busiestWeekdayShare * 100).roundToInt(),
                            percentCaption = stringResource(R.string.highlight_of_spending),
                            percentColor = accent,
                        )
                    }
                }
                if (savingsRate != null) {
                    val saved = savingsRate >= 0
                    val color = if (saved) incomeColor else expenseColor
                    add {
                        HighlightRow(
                            icon = {
                                HighlightIconTile(
                                    if (saved) Icons.Outlined.Savings else Icons.Outlined.MoneyOff,
                                    color,
                                )
                            },
                            label = stringResource(R.string.highlight_savings),
                            value = stringResource(
                                if (saved) R.string.highlight_saved_value else R.string.highlight_overspent_value,
                            ),
                            detail = null,
                            percent = (abs(savingsRate) * 100).roundToInt(),
                            percentCaption = stringResource(R.string.highlight_of_income),
                            percentColor = color,
                        )
                    }
                }
            }
            rows.forEachIndexed { index, row ->
                row()
                if (index < rows.lastIndex) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    )
                }
            }
        }
    }
}

@Composable
private fun HighlightRow(
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    detail: String?,
    percent: Int,
    percentCaption: String,
    percentColor: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon()
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (detail != null) {
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.titleLarge.copy(letterSpacing = (-0.3).sp),
                fontWeight = FontWeight.SemiBold,
                color = percentColor,
            )
            Text(
                text = percentCaption,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Tinted round tile, same size and shape as the category icon so all three rows line up. */
@Composable
private fun HighlightIconTile(icon: ImageVector, tint: Color) {
    Box(
        modifier = IconSpecModifier
            .background(tint.copy(alpha = 0.14f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp),
        )
    }
}

// endregion

// region Where your money goes

/**
 * Same rows and grouped cards as the Category screen ([CategoryTransactionItem]). A tap opens
 * that category's details; "View all" opens the full category list for the same filter.
 */
@Composable
private fun CategoryBreakdownSection(
    categories: List<CategoryRowUi>,
    onCategoryClick: (String) -> Unit,
    onViewAllCategories: () -> Unit,
) {
    if (categories.isEmpty()) return
    val shown = categories.take(MAX_CATEGORY_ROWS)

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        SectionTitle(
            text = stringResource(R.string.where_money_goes),
            modifier = ListTitleModifier,
            onViewAllClick = if (categories.size > MAX_CATEGORY_ROWS) onViewAllCategories else null,
        )
        shown.forEachIndexed { index, row ->
            AppCardView(
                modifier = Modifier.fillMaxWidth(),
                shape = AppCardViewDefaults.cardShape(index, shown),
            ) {
                CategoryTransactionItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCategoryClick(row.category.id) },
                    name = row.category.displayName(),
                    icon = row.category.storedIcon.name,
                    iconBackgroundColor = row.category.storedIcon.backgroundColor,
                    customImagePath = row.category.storedIcon.customImagePath,
                    amount = row.amount,
                    // CategoryTransactionItem takes 0..100.
                    percentage = row.share * 100f,
                )
            }
        }
    }
}

// endregion

// region Weekday pattern

@Composable
private fun WeekdayCard(weekdays: List<WeekdayBarUi>, busiest: DayOfWeek?) {
    // Selected column (tap to inspect); defaults to the busiest day.
    var selected by remember(busiest) { mutableStateOf(busiest) }
    val selectedBar = weekdays.firstOrNull { it.dayOfWeek == selected }

    AppCardView(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionTitle(stringResource(R.string.spending_by_weekday))
            if (selectedBar != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${selectedBar.dayOfWeek.fullName()} · ${selectedBar.amount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                weekdays.forEach { bar ->
                    val isBusiest = bar.dayOfWeek == busiest
                    val isSelected = bar.dayOfWeek == selected
                    val dayName = bar.dayOfWeek.getDisplayName(DayNameStyle.SHORT, LocalLocale.current.platformLocale)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            // Whole column is the touch target, much larger than the bar.
                            .clickable { selected = bar.dayOfWeek }
                            .semantics { contentDescription = "${bar.dayOfWeek.fullName()}, ${bar.amount}" },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.BottomCenter,
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.72f)
                                    .fillMaxHeight(bar.relative.coerceIn(0.02f, 1f))
                                    .background(
                                        color = when {
                                            isBusiest -> MaterialTheme.colorScheme.primary
                                            isSelected -> MaterialTheme.colorScheme.outline
                                            else -> MaterialTheme.colorScheme.surfaceContainerHigh
                                        },
                                        // Rounded data end only; flat on the baseline.
                                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
                                    ),
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = dayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

// endregion

// region Biggest expenses

/**
 * Same title + row treatment as the Transactions list on Home: the shared [TransactionItem] in
 * grouped cards, and a tap opens the transaction for editing.
 */
@Composable
private fun BiggestExpensesSection(
    expenses: List<TransactionUiItem>,
    onTransactionClick: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        SectionTitle(
            text = stringResource(R.string.biggest_expenses),
            modifier = ListTitleModifier,
        )
        expenses.forEachIndexed { index, transaction ->
            AppCardView(
                modifier = Modifier.fillMaxWidth(),
                shape = AppCardViewDefaults.cardShape(index, expenses),
            ) {
                TransactionItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTransactionClick(transaction.id) },
                    categoryName = transaction.categoryTitleResId?.let { stringResource(it) }
                        ?: transaction.categoryName,
                    categoryColor = transaction.categoryIcon.backgroundColor,
                    categoryIcon = transaction.categoryIcon.name,
                    amount = transaction.amount,
                    date = transaction.date,
                    notes = transaction.notes,
                    transactionType = transaction.transactionType,
                    fromAccountName = transaction.fromAccountName,
                    fromAccountIcon = transaction.fromAccountIcon.name,
                    fromAccountColor = transaction.fromAccountIcon.backgroundColor,
                    toAccountName = transaction.toAccountName,
                    toAccountIcon = transaction.toAccountIcon?.name,
                    toAccountColor = transaction.toAccountIcon?.backgroundColor,
                )
            }
        }
    }
}

// endregion

@AppPreviewsLightAndDarkMode
@Composable
private fun AnalysisInsightsSectionPreview() {
    fun category(id: String, name: String, color: String) = Category(
        id = id,
        name = name,
        type = CategoryType.EXPENSE,
        storedIcon = StoredIcon(name = "restaurant", backgroundColor = color),
        createdOn = Date(),
        updatedOn = Date(),
    )
    val food = category("1", "Food", "#F44336")
    val travel = category("2", "Travel", "#3F51B5")
    val bars = listOf(0.3f, 0.2f, 0.25f, 0.4f, 0.6f, 1f, 0.5f)
    ExpenseManagerPreviewTheme(padding = 16.dp) {
        AnalysisInsightsSection(
            insights = AnalysisInsightsUi(
                hasExpenses = true,
                savingsRate = 0.22f,
                categories = listOf(
                    CategoryRowUi(food, "₹4,200", 0.42f, 18),
                    CategoryRowUi(travel, "₹2,100", 0.21f, 4),
                ),
                weekdays = DayOfWeek.entries.mapIndexed { i, d -> WeekdayBarUi(d, "₹${(bars[i] * 900).toInt()}", bars[i]) },
                busiestWeekday = DayOfWeek.SATURDAY,
                busiestWeekdayShare = 0.28f,
                biggestExpenses = listOf(getTransactionItem("1"), getTransactionItem("2")),
            ),
        )
    }
}
