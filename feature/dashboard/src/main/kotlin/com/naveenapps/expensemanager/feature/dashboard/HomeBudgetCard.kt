package com.naveenapps.expensemanager.feature.dashboard

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.PagerIndicator
import com.naveenapps.expensemanager.core.designsystem.ui.components.SegmentedSwitch
import com.naveenapps.expensemanager.core.model.BudgetPeriod
import com.naveenapps.expensemanager.feature.budget.components.BudgetGauge
import com.naveenapps.expensemanager.feature.budget.components.BudgetProgress
import com.naveenapps.expensemanager.feature.budget.label
import java.text.SimpleDateFormat

/**
 * Home's budget card: everything in one card. A Monthly / Annual switch at the top, then the
 * budgets for that period as a half-circle gauge (what's left in the middle, spent and limit tiles,
 * and how much can be spent per day for the rest of the period).
 *
 * - One budget: just its gauge, nothing to swipe.
 * - Several (e.g. an overall budget plus category ones): swipe between their gauges inside the
 *   card, largest limit first (see [pickHomeBudgets]), with page dots at the bottom of the card.
 * - None: an offer to create one for that period.
 *
 * Tapping the card opens the budget currently shown. Each period keeps its own page, so switching
 * Monthly / Annual and back returns to the same budget.
 */
@Composable
internal fun HomeBudgetCard(
    budgets: HomeBudgets,
    onAction: (DashboardAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var period by rememberSaveable { mutableStateOf(BudgetPeriod.MONTHLY) }

    // The pagers read their page count through these, so adding or deleting a budget updates them.
    val monthlyCount by rememberUpdatedState(budgets.monthly.size)
    val yearlyCount by rememberUpdatedState(budgets.yearly.size)
    val monthlyPager = rememberPagerState(pageCount = { monthlyCount })
    val yearlyPager = rememberPagerState(pageCount = { yearlyCount })
    fun pagerFor(p: BudgetPeriod) = if (p == BudgetPeriod.MONTHLY) monthlyPager else yearlyPager

    val periodBudgets = if (period == BudgetPeriod.MONTHLY) budgets.monthly else budgets.yearly
    val shownBudget = periodBudgets.getOrNull(pagerFor(period).currentPage.coerceAtMost(periodBudgets.lastIndex))

    AppCardView(
        modifier = modifier.fillMaxWidth(),
        cornerSize = 16.dp,
        onClick = shownBudget?.let { budget -> { onAction(DashboardAction.OpenBudgetDetails(budget.id)) } },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SegmentedSwitch(
                options = BudgetPeriod.entries,
                selected = period,
                onSelect = { period = it },
                label = { it.label() },
                modifier = Modifier.fillMaxWidth(0.72f),
            )

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedContent(
                targetState = period to periodBudgets,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(120)) },
                // Animate only when the period or the layout (none / one / several) changes. Adding a
                // third budget to two just adds a page, and a pager's state is never shown twice.
                contentKey = { (shownPeriod, shownBudgets) -> shownPeriod to shownBudgets.size.coerceAtMost(2) },
                label = "home budget",
            ) { (shownPeriod, shownBudgets) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    when (shownBudgets.size) {
                        0 -> NoBudget(
                            periodName = if (shownPeriod == BudgetPeriod.MONTHLY) {
                                monthAndYear(budgets)
                            } else {
                                budgets.year
                            },
                            onCreate = { onAction(DashboardAction.OpenBudgetCreate(shownPeriod)) },
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        1 -> HomeBudgetGauge(budget = shownBudgets.single(), onAction = onAction)

                        else -> HomeBudgetPages(
                            budgets = shownBudgets,
                            pagerState = pagerFor(shownPeriod),
                            onAction = onAction,
                        )
                    }
                }
            }
        }
    }
}

/** Several budgets for one period: swipe between them inside the card; the dots show where you are. */
@Composable
private fun HomeBudgetPages(
    budgets: List<BudgetProgress>,
    pagerState: PagerState,
    onAction: (DashboardAction) -> Unit,
) {
    HorizontalPager(
        state = pagerState,
        // Pages slide edge to edge of the card; each gauge keeps the card's inner padding. Gauges can
        // differ in height (an over-budget one has an extra line), so ease the change.
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        pageSpacing = 16.dp,
        verticalAlignment = Alignment.Top,
        key = { page -> budgets[page].id },
    ) { page ->
        HomeBudgetGauge(budget = budgets[page], onAction = onAction)
    }

    Spacer(modifier = Modifier.height(16.dp))

    val currentPage = pagerState.currentPage.coerceIn(0, budgets.lastIndex)
    PagerIndicator(
        pageCount = budgets.size,
        currentPage = currentPage,
        description = stringResource(id = R.string.home_budget_page, currentPage + 1, budgets.size),
    )
}

@Composable
private fun HomeBudgetGauge(budget: BudgetProgress, onAction: (DashboardAction) -> Unit) {
    BudgetGauge(
        budget = budget,
        onEdit = { onAction(DashboardAction.EditBudget(budget.id)) },
        modifier = Modifier.padding(horizontal = 16.dp),
    )
}

@Composable
private fun NoBudget(periodName: String, onCreate: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Savings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(30.dp),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(id = R.string.home_budget_none, periodName),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(id = R.string.tap_to_set_spending_limit),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        FilledTonalButton(onClick = onCreate) {
            Icon(imageVector = Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = stringResource(id = R.string.home_budget_create))
        }
    }
}

/** "October 2026" for the month being viewed, in the user's language. */
@Composable
private fun monthAndYear(budgets: HomeBudgets): String {
    val locale = LocalConfiguration.current.locales[0]
    val month = budgets.month ?: return budgets.year
    return remember(month, locale) {
        SimpleDateFormat(DateFormat.getBestDateTimePattern(locale, "MMMMyyyy"), locale).format(month)
    }
}
