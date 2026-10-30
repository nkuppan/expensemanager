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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naveenapps.expensemanager.core.designsystem.R as DesignSystemR
import com.naveenapps.expensemanager.core.designsystem.components.DashboardWidgetTitle
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.SegmentedSwitch
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

    AppCardView(modifier = modifier.fillMaxWidth(), cornerSize = AnalysisCardCorner) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(DesignSystemR.string.balance).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = flow.balance,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = comparison?.let { stringResource(it.titleResId) + " · " + it.periodsLabel } ?: period,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
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
                Spacer(modifier = Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LegendItem(stringResource(DesignSystemR.string.expense), expenseColor)
                    LegendItem(stringResource(DesignSystemR.string.income), incomeColor)
                }
                Spacer(modifier = Modifier.height(8.dp))
                ChartScreen(chart = chart, isDarkTheme = isDarkTheme)
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.no_chart_available),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
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
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(12.dp)
            .semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        LegendItem(label, color)
        Text(
            text = amount,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (change != null) {
            ChangeBadge(change = change.change, upColor = upColor, downColor = downColor)
            Text(
                text = stringResource(R.string.compare_was, change.previous),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                .clip(CircleShape)
                .background(color),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** "↑ 12%" / "↓ 8%" in a tinted pill, "Same" when flat, or "New" when there was nothing before. */
@Composable
internal fun ChangeBadge(change: Double?, upColor: Color, downColor: Color) {
    val percent = change?.let { (abs(it) * 100).roundToInt() }
    val (text, color, icon) = when {
        change == null -> Triple(stringResource(R.string.compare_new), MaterialTheme.colorScheme.primary, null)
        percent == 0 -> Triple(stringResource(R.string.compare_same), MaterialTheme.colorScheme.onSurfaceVariant, null)
        change > 0 -> Triple("$percent%", upColor, Icons.AutoMirrored.Filled.TrendingUp)
        else -> Triple("$percent%", downColor, Icons.AutoMirrored.Filled.TrendingDown)
    }
    Row(
        modifier = Modifier
            .background(color.copy(alpha = 0.14f), RoundedCornerShape(8.dp))
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

    AppCardView(modifier = modifier.fillMaxWidth(), cornerSize = AnalysisCardCorner) {
        Column(modifier = Modifier.padding(16.dp)) {
            DashboardWidgetTitle(title = stringResource(R.string.average_and_projected))
            Spacer(modifier = Modifier.height(12.dp))
            SegmentedSwitch(
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
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f))
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .semantics(mergeDescendants = true) {},
    ) {
        LegendItem(label, color)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
