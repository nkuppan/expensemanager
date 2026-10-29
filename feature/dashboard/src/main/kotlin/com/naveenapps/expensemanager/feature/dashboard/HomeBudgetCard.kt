package com.naveenapps.expensemanager.feature.dashboard

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedContent
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
import com.naveenapps.expensemanager.core.designsystem.ui.components.SegmentedSwitch
import com.naveenapps.expensemanager.core.model.BudgetPeriod
import com.naveenapps.expensemanager.feature.budget.components.BudgetGauge
import com.naveenapps.expensemanager.feature.budget.label
import java.text.SimpleDateFormat

/**
 * Home's budget card: a Monthly / Annual switch, a half-circle gauge with what's left in the
 * middle, spent and limit tiles, and how much can be spent per day for the rest of the period.
 * One budget per period (see [pickHomeBudgets]); "View all" lists the rest.
 */
@Composable
internal fun HomeBudgetCard(
    budgets: HomeBudgets,
    onAction: (DashboardAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var period by rememberSaveable { mutableStateOf(BudgetPeriod.MONTHLY) }
    val budget = if (period == BudgetPeriod.MONTHLY) budgets.monthly else budgets.yearly

    AppCardView(
        modifier = modifier.fillMaxWidth(),
        cornerSize = 24.dp,
        onClick = budget?.let { { onAction(DashboardAction.OpenBudgetDetails(it.id)) } },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
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
                targetState = period to budget,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(120)) },
                contentKey = { (p, b) -> p to b?.id },
                label = "home budget",
            ) { (shownPeriod, shownBudget) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (shownBudget != null) {
                        BudgetGauge(
                            budget = shownBudget,
                            onEdit = { onAction(DashboardAction.EditBudget(shownBudget.id)) },
                        )
                    } else {
                        NoBudget(
                            periodName = if (shownPeriod == BudgetPeriod.MONTHLY) monthAndYear(budgets) else budgets.year,
                            onCreate = { onAction(DashboardAction.OpenBudgetCreate(shownPeriod)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoBudget(periodName: String, onCreate: () -> Unit) {
    Column(
        modifier = Modifier
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
