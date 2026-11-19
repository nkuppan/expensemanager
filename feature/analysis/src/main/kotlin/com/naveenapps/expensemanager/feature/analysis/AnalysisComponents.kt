package com.naveenapps.expensemanager.feature.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.R as DesignSystemR
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountSize
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountText
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmIcon
import com.naveenapps.expensemanager.core.designsystem.em.EmSectionHeader
import com.naveenapps.expensemanager.core.designsystem.em.EmSegmented
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.tint
import com.naveenapps.expensemanager.core.designsystem.ui.utils.getExpenseColor
import com.naveenapps.expensemanager.core.designsystem.ui.utils.getIncomeColor
import com.naveenapps.expensemanager.core.model.ExpenseFlowState
import com.naveenapps.expensemanager.core.model.WholeAverageData
import kotlin.math.abs
import kotlin.math.roundToInt

/** Corner size shared by every card on the Analysis screen. */
internal val AnalysisCardCorner = 20.dp

/**
 * The top of Analysis: the balance for the period, income and expense with their change since
 * the previous period, and the income/expense chart with a legend, all in one card.
 */
@Composable
internal fun OverviewCard(
    flow: ExpenseFlowState,
    comparison: PeriodComparisonUi?,
    chart: AnalysisUiChartData?,
    isDarkTheme: Boolean,
    period: String,
    modifier: Modifier = Modifier,
) {
    val incomeColor = getIncomeColor()
    val expenseColor = getExpenseColor()

    EmCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(EmTheme.spacing.lg)) {
            EmText(
                text = stringResource(DesignSystemR.string.balance),
                role = EmTextRole.Overline,
                color = EmTextColor.Supporting,
            )
            EmAmountText(
                amount = flow.balance,
                size = EmAmountSize.Large,
            )
            EmText(
                text = comparison?.let { stringResource(it.titleResId) + " · " + it.periodsLabel } ?: period,
                role = EmTextRole.Supporting,
                color = EmTextColor.Supporting,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(EmTheme.spacing.md))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
            ) {
                FlowTile(
                    label = stringResource(DesignSystemR.string.income),
                    amount = flow.income,
                    color = incomeColor,
                    change = comparison?.income,
                    // More income is good news, more spending isn't.
                    upColor = incomeColor,
                    downColor = expenseColor,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
                FlowTile(
                    label = stringResource(DesignSystemR.string.expense),
                    amount = flow.expense,
                    color = expenseColor,
                    change = comparison?.expense,
                    upColor = expenseColor,
                    downColor = incomeColor,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }

            if (chart != null) {
                Spacer(modifier = Modifier.height(EmTheme.spacing.lg))
                Row(horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.lg)) {
                    LegendItem(stringResource(DesignSystemR.string.expense), expenseColor)
                    LegendItem(stringResource(DesignSystemR.string.income), incomeColor)
                }
                Spacer(modifier = Modifier.height(EmTheme.spacing.sm))
                ChartScreen(chart = chart, isDarkTheme = isDarkTheme)
            } else {
                Spacer(modifier = Modifier.height(EmTheme.spacing.lg))
                EmText(
                    text = stringResource(R.string.no_chart_available),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    role = EmTextRole.Supporting,
                    color = EmTextColor.Supporting,
                )
            }
        }
    }
}

/** Income or expense on a soft tint of its colour, with the change since the last period. */
@Composable
private fun FlowTile(
    label: String,
    amount: String,
    color: Color,
    change: ComparisonMetricUi?,
    upColor: Color,
    downColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(EmTheme.shapes.lg)
            .background(color.tint())
            .padding(EmTheme.spacing.md)
            .semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.xs),
    ) {
        LegendItem(label, color)
        EmAmountText(
            amount = amount,
        )
        if (change != null) {
            ChangeBadge(change = change.change, upColor = upColor, downColor = downColor)
            EmText(
                text = stringResource(R.string.compare_was, change.previous),
                role = EmTextRole.Supporting,
                color = EmTextColor.Supporting,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(EmTheme.shapes.pill)
                .background(color),
        )
        Spacer(modifier = Modifier.width(EmTheme.spacing.sm))
        EmText(
            text = label,
            role = EmTextRole.Supporting,
            color = EmTextColor.Supporting,
        )
    }
}

/** "↑ 12%" / "↓ 8%" in a tinted pill, "Same" when flat, or "New" when there was nothing before. */
@Composable
internal fun ChangeBadge(change: Double?, upColor: Color, downColor: Color) {
    val percent = change?.let { (abs(it) * 100).roundToInt() }
    val (text, color, icon) = when {
        change == null -> Triple(stringResource(R.string.compare_new), EmTheme.colors.scheme.primary, null)
        percent == 0 -> Triple(stringResource(R.string.compare_same), EmTheme.colors.scheme.onSurfaceVariant, null)
        change > 0 -> Triple("$percent%", upColor, Icons.AutoMirrored.Filled.TrendingUp)
        else -> Triple("$percent%", downColor, Icons.AutoMirrored.Filled.TrendingDown)
    }
    Row(
        modifier = Modifier
            .background(color.tint(), EmTheme.shapes.sm)
            .padding(horizontal = EmTheme.spacing.sm, vertical = EmTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            EmIcon(icon = icon, contentDescription = null, size = EmTheme.icons.sm, tint = color)
            Spacer(modifier = Modifier.width(EmTheme.spacing.xs))
        }
        EmText(
            text = text,
            role = EmTextRole.Label,
            color = color,
        )
    }
}

private enum class AveragePeriod { DAY, WEEK, MONTH }

/**
 * Average (and projected) income and spending per day, week or month: one switch and two
 * numbers instead of a 3×2 table.
 */
@Composable
internal fun AveragesCard(
    averageData: WholeAverageData,
    modifier: Modifier = Modifier,
) {
    var period by rememberSaveable { mutableStateOf(AveragePeriod.DAY) }
    val (income, expense) = when (period) {
        AveragePeriod.DAY -> averageData.incomeAverageData.perDay to averageData.expenseAverageData.perDay
        AveragePeriod.WEEK -> averageData.incomeAverageData.perWeek to averageData.expenseAverageData.perWeek
        AveragePeriod.MONTH -> averageData.incomeAverageData.perMonth to averageData.expenseAverageData.perMonth
    }
    val incomeColor = getIncomeColor()
    val expenseColor = getExpenseColor()

    EmCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(EmTheme.spacing.lg)) {
            EmSectionHeader(title = stringResource(R.string.average_and_projected))
            Spacer(modifier = Modifier.height(EmTheme.spacing.md))
            EmSegmented(
                options = AveragePeriod.entries,
                selected = period,
                onSelect = { period = it },
                label = {
                    stringResource(
                        when (it) {
                            AveragePeriod.DAY -> R.string.day
                            AveragePeriod.WEEK -> R.string.week
                            AveragePeriod.MONTH -> R.string.month
                        },
                    )
                },
            )
            Spacer(modifier = Modifier.height(EmTheme.spacing.md))
            Row(horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm)) {
                AverageTile(stringResource(DesignSystemR.string.income), income, incomeColor, Modifier.weight(1f))
                AverageTile(stringResource(DesignSystemR.string.expense), expense, expenseColor, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AverageTile(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(EmTheme.shapes.lg)
            .background(EmTheme.colors.scheme.surfaceContainerHigh)
            .padding(horizontal = EmTheme.spacing.md, vertical = EmTheme.spacing.md)
            .semantics(mergeDescendants = true) {},
    ) {
        LegendItem(label, color)
        Spacer(modifier = Modifier.height(EmTheme.spacing.xs))
        EmAmountText(
            amount = value,
            color = color,
        )
    }
}
