package com.naveenapps.expensemanager.feature.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.MoneyOff
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountSize
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountText
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmCardPosition
import com.naveenapps.expensemanager.core.designsystem.em.EmIcon
import com.naveenapps.expensemanager.core.designsystem.em.EmPreviews
import com.naveenapps.expensemanager.core.designsystem.em.EmSectionHeader
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.theme.tint
import com.naveenapps.expensemanager.core.designsystem.ui.components.IconAndBackgroundView
import com.naveenapps.expensemanager.core.designsystem.ui.components.TransactionItem
import com.naveenapps.expensemanager.core.designsystem.ui.utils.IconSpecModifier
import com.naveenapps.expensemanager.core.designsystem.ui.utils.categoryDisplayName
import com.naveenapps.expensemanager.core.designsystem.ui.utils.displayName
import com.naveenapps.expensemanager.core.designsystem.ui.utils.getExpenseColor
import com.naveenapps.expensemanager.core.designsystem.ui.utils.getIncomeColor
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.model.TransactionUiItem
import com.naveenapps.expensemanager.feature.category.transaction.CategoryTransactionItem
import com.naveenapps.expensemanager.feature.transaction.list.getTransactionItem
import java.time.format.TextStyle as DayNameStyle
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.isoDayNumber

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
        verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.lg),
    ) {
        HighlightsCard(insights)
        CategoryBreakdownSection(insights.categories, onCategoryClick, onViewAllCategories)
        WeekdayCard(insights.weekdays, insights.busiestWeekday)
        if (insights.biggestExpenses.isNotEmpty()) {
            BiggestExpensesSection(insights.biggestExpenses, onTransactionClick)
        }
    }
}

/** Same title as the other Analysis / Home cards (e.g. "Average and projected").
 * For the platform's localized day names (Android-only display text). */
private fun DayOfWeek.javaDay(): java.time.DayOfWeek = java.time.DayOfWeek.of(isoDayNumber)

private fun DayOfWeek.fullName(): String = javaDay().getDisplayName(DayNameStyle.FULL, Locale.getDefault())
    .replaceFirstChar { it.titlecase(Locale.getDefault()) }

// region Highlights

/**
 * Up to three tiles side by side: top category, busiest weekday and savings rate, each with a
 * big percentage so the key numbers can be read at a glance.
 */
@Composable
internal fun HighlightsCard(insights: AnalysisInsightsUi) {
    val top = insights.categories.firstOrNull()
    val busiest = insights.busiestWeekday
    val savingsRate = insights.savingsRate
    if (top == null && busiest == null && savingsRate == null) return

    val incomeColor = getIncomeColor()
    val expenseColor = getExpenseColor()
    val accent = EmTheme.colors.scheme.primary

    Column {
        EmSectionHeader(stringResource(R.string.insights_title), modifier = ListTitleModifier)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
        ) {
            if (top != null) {
                HighlightTile(
                    icon = {
                        IconAndBackgroundView(
                            icon = top.category.storedIcon.name,
                            iconBackgroundColor = top.category.storedIcon.backgroundColor,
                            name = top.category.displayName(),
                            customImagePath = top.category.storedIcon.customImagePath,
                        )
                    },
                    percent = (top.share * 100).roundToInt(),
                    color = accent,
                    label = stringResource(R.string.highlight_top_category),
                    value = top.category.displayName(),
                    modifier = Modifier.weight(1f),
                )
            }
            if (busiest != null) {
                HighlightTile(
                    icon = { HighlightIconTile(Icons.Outlined.CalendarMonth, accent) },
                    percent = (insights.busiestWeekdayShare * 100).roundToInt(),
                    color = accent,
                    label = stringResource(R.string.highlight_busiest_day),
                    value = busiest.fullName(),
                    modifier = Modifier.weight(1f),
                )
            }
            if (savingsRate != null) {
                val saved = savingsRate >= 0
                val color = if (saved) incomeColor else expenseColor
                HighlightTile(
                    icon = {
                        HighlightIconTile(if (saved) Icons.Outlined.Savings else Icons.Outlined.MoneyOff, color)
                    },
                    percent = (abs(savingsRate) * 100).roundToInt(),
                    color = color,
                    label = stringResource(R.string.highlight_savings),
                    value = stringResource(
                        if (saved) R.string.highlight_saved_value else R.string.highlight_overspent_value,
                    ),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun HighlightTile(
    icon: @Composable () -> Unit,
    percent: Int,
    color: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    EmCard(modifier = modifier.fillMaxHeight()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(EmTheme.spacing.md)
                .semantics(mergeDescendants = true) {},
            verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
        ) {
            icon()
            EmAmountText(
                amount = "$percent%",
                color = color,
            )
            EmText(
                text = label,
                role = EmTextRole.Supporting,
                color = EmTextColor.Supporting,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            EmText(
                text = value,
                role = EmTextRole.Supporting,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Tinted round tile, same size and shape as the category icon so all tiles line up. */
@Composable
private fun HighlightIconTile(icon: ImageVector, tint: Color) {
    Box(
        modifier = IconSpecModifier
            .background(tint.tint(), EmTheme.shapes.pill),
        contentAlignment = Alignment.Center,
    ) {
        EmIcon(
            icon = icon,
            contentDescription = null,
            size = EmTheme.icons.md,
            tint = tint,
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
internal fun CategoryBreakdownSection(
    categories: List<CategoryRowUi>,
    onCategoryClick: (String) -> Unit,
    onViewAllCategories: () -> Unit,
) {
    if (categories.isEmpty()) return
    val shown = categories.take(MAX_CATEGORY_ROWS)

    Column(verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.xxs)) {
        EmSectionHeader(
            title = stringResource(R.string.where_money_goes),
            modifier = ListTitleModifier,
            onViewAllClick = if (categories.size > MAX_CATEGORY_ROWS) onViewAllCategories else null,
        )
        shown.forEachIndexed { index, row ->
            EmCard(
                modifier = Modifier.fillMaxWidth(),
                position = EmCardPosition.of(index, shown),
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
internal fun WeekdayCard(weekdays: List<WeekdayBarUi>, busiest: DayOfWeek?) {
    // Selected column (tap to inspect); defaults to the busiest day.
    var selected by remember(busiest) { mutableStateOf(busiest) }
    val selectedBar = weekdays.firstOrNull { it.dayOfWeek == selected }
    val accent = EmTheme.colors.scheme.primary

    EmCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(EmTheme.spacing.lg)) {
            EmSectionHeader(stringResource(R.string.spending_by_weekday))
            if (selectedBar != null) {
                Spacer(modifier = Modifier.height(EmTheme.spacing.xxs))
                Row(verticalAlignment = Alignment.Bottom) {
                    EmAmountText(
                        amount = selectedBar.amount,
                        size = EmAmountSize.Large,
                    )
                    Spacer(modifier = Modifier.width(EmTheme.spacing.sm))
                    EmText(
                        text = selectedBar.dayOfWeek.fullName(),
                        modifier = Modifier.padding(bottom = EmTheme.spacing.xxs),
                        color = EmTextColor.Supporting,
                    )
                }
            }
            Spacer(modifier = Modifier.height(EmTheme.spacing.lg))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp),
                horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
                verticalAlignment = Alignment.Bottom,
            ) {
                weekdays.forEach { bar ->
                    val isBusiest = bar.dayOfWeek == busiest
                    val isSelected = bar.dayOfWeek == selected
                    val dayName = bar.dayOfWeek.javaDay().getDisplayName(DayNameStyle.SHORT, LocalLocale.current.platformLocale)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(EmTheme.shapes.sm)
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
                                    .fillMaxWidth(0.7f)
                                    .fillMaxHeight(bar.relative.coerceIn(0.04f, 1f))
                                    .background(
                                        brush = Brush.verticalGradient(
                                            listOf(
                                                accent.copy(alpha = if (isSelected || isBusiest) 1f else 0.35f),
                                                accent.copy(alpha = if (isSelected || isBusiest) 0.6f else 0.18f),
                                            ),
                                        ),
                                        shape = EmTheme.shapes.sm,
                                    ),
                            )
                        }
                        Spacer(modifier = Modifier.height(EmTheme.spacing.sm))
                        EmText(
                            text = dayName,
                            role = EmTextRole.Supporting,
                            color = if (isSelected) accent else EmTheme.colors.scheme.onSurfaceVariant,
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
internal fun BiggestExpensesSection(
    expenses: List<TransactionUiItem>,
    onTransactionClick: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.xxs)) {
        EmSectionHeader(
            title = stringResource(R.string.biggest_expenses),
            modifier = ListTitleModifier,
        )
        expenses.forEachIndexed { index, transaction ->
            EmCard(
                modifier = Modifier.fillMaxWidth(),
                position = EmCardPosition.of(index, expenses),
            ) {
                TransactionItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTransactionClick(transaction.id) },
                    categoryName = transaction.categoryDisplayName(),
                    categoryColor = transaction.categoryIcon.backgroundColor,
                    categoryIcon = transaction.categoryIcon.name,
                    amount = transaction.amount,
                    date = transaction.date,
                    notes = transaction.notes,
                    transactionType = transaction.transactionType,
                    isRecurring = transaction.isRecurring,
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

@EmPreviews
@Composable
private fun AnalysisInsightsSectionPreview() {
    fun category(id: String, name: String, color: String) = Category(
        id = id,
        name = name,
        type = CategoryType.EXPENSE,
        storedIcon = StoredIcon(name = "restaurant", backgroundColor = color),
        createdOn = Clock.System.now(),
        updatedOn = Clock.System.now(),
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
