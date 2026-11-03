package com.naveenapps.expensemanager.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.common.R as CommonR
import com.naveenapps.expensemanager.core.common.utils.toCompleteDateWithDate
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.IconAndBackgroundView
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.feature.transaction.recurring.labelResId
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date

/**
 * Home's "Upcoming" card, styled like the budget card: what the recurring rules will add over
 * the next 30 days, then the next few rules, soonest first, each with when it's added.
 * With no rules yet, it invites the user to plan one.
 */
@Composable
internal fun HomeUpcomingCard(
    upcoming: UpcomingUi,
    onAction: (DashboardAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCardView(
        modifier = modifier.fillMaxWidth(),
        cornerSize = 16.dp,
    ) {
        if (upcoming.items.isEmpty()) {
            UpcomingEmpty(onPlan = { onAction(DashboardAction.PlanRecurring) })
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (upcoming.expenseDue != null || upcoming.incomeDue != null) {
                    UpcomingTotals(upcoming)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
                upcoming.items.forEach { item ->
                    UpcomingRow(item = item, onClick = { onAction(DashboardAction.OpenRecurringEdit(item.rule.id)) })
                }
            }
        }
    }
}

@Composable
private fun UpcomingTotals(upcoming: UpcomingUi) {
    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)) {
        Text(
            text = pluralStringResource(R.plurals.upcoming_window, upcoming.windowDays, upcoming.windowDays),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            upcoming.expenseDue?.let {
                UpcomingTotal(
                    label = stringResource(R.string.upcoming_spend),
                    amount = it,
                    color = colorResource(CommonR.color.red_500),
                )
            }
            upcoming.incomeDue?.let {
                UpcomingTotal(
                    label = stringResource(R.string.upcoming_income),
                    amount = it,
                    color = colorResource(CommonR.color.green_500),
                )
            }
        }
    }
}

@Composable
private fun UpcomingTotal(label: String, amount: String, color: Color) {
    Column {
        Text(
            text = amount,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun UpcomingRow(item: UpcomingItemUi, onClick: () -> Unit) {
    val rule = item.rule
    val categoryName = rule.category?.let { category ->
        category.titleResId?.let { stringResource(it) } ?: category.name
    }.orEmpty()
    val amountColor = when (rule.type) {
        TransactionType.EXPENSE -> colorResource(CommonR.color.red_500)
        TransactionType.INCOME -> colorResource(CommonR.color.green_500)
        TransactionType.TRANSFER -> MaterialTheme.colorScheme.onSurface
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        rule.category?.let {
            IconAndBackgroundView(
                icon = it.storedIcon.name,
                iconBackgroundColor = it.storedIcon.backgroundColor,
                name = categoryName,
                customImagePath = it.storedIcon.customImagePath,
            )
            Spacer(modifier = Modifier.width(14.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = rule.notes.ifBlank { categoryName },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(
                    R.string.upcoming_schedule,
                    stringResource(rule.frequency.labelResId),
                    whenDue(rule.nextDueDate),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = item.amount,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = amountColor,
        )
    }
}

/** "Today", "Tomorrow", "In 3 days · 6 Nov 2026" or just the date when it's a week or more away. */
@Composable
private fun whenDue(date: Date): String {
    val days = remember(date) {
        val zone = ZoneId.systemDefault()
        ChronoUnit.DAYS.between(LocalDate.now(zone), date.toInstant().atZone(zone).toLocalDate())
    }
    val formatted = remember(date) { date.toCompleteDateWithDate() }
    return when {
        // Due but not logged yet (the app was closed): it's added on the next start, so "Today".
        days <= 0 -> stringResource(R.string.upcoming_today)
        days == 1L -> stringResource(R.string.upcoming_tomorrow)
        days < 7 -> pluralStringResource(R.plurals.upcoming_in_days, days.toInt(), days.toInt()) + " · " + formatted
        else -> formatted
    }
}

@Composable
private fun UpcomingEmpty(onPlan: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.EventRepeat,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.upcoming_empty_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.upcoming_empty_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        FilledTonalButton(onClick = onPlan) {
            Icon(imageVector = Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = stringResource(R.string.upcoming_plan))
        }
    }
}
