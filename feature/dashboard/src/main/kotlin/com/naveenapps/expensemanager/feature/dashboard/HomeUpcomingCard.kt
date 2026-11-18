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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.Repeat
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naveenapps.expensemanager.core.common.R as CommonR
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.feature.transaction.recurring.labelResId
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date

/**
 * Home's "Upcoming" card, styled like the budget card. On top, two tinted tiles with what the
 * recurring rules will spend and bring in over the next 30 days. Then the next few rules, soonest
 * first: a calendar tile with the day it's added, the name with its frequency, and the amount with
 * a "Tomorrow" / "In 3 days" pill (highlighted when it's today or tomorrow).
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
                }
                upcoming.items.forEachIndexed { index, item ->
                    if (index > 0) {
                        HorizontalDivider(
                            // Starts after the date tile, like a list divider.
                            modifier = Modifier.padding(start = 76.dp, end = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        )
                    }
                    UpcomingRow(item = item, onClick = { onAction(DashboardAction.OpenRecurringEdit(item.rule.id)) })
                }
            }
        }
    }
}

@Composable
private fun UpcomingTotals(upcoming: UpcomingUi) {
    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)) {
        Text(
            text = pluralStringResource(R.plurals.upcoming_window, upcoming.windowDays, upcoming.windowDays),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            upcoming.expenseDue?.let {
                UpcomingTotalTile(
                    label = stringResource(R.string.upcoming_spend),
                    amount = it,
                    color = colorResource(CommonR.color.red_500),
                    modifier = Modifier.weight(1f),
                )
            }
            upcoming.incomeDue?.let {
                UpcomingTotalTile(
                    label = stringResource(R.string.upcoming_income),
                    amount = it,
                    color = colorResource(CommonR.color.green_500),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** A soft tile tinted with the type's colour: the amount, and what it is underneath. */
@Composable
private fun UpcomingTotalTile(label: String, amount: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(
            text = amount,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
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
    // The notes name the payment when there are any ("Rent"), with the category beside the
    // frequency; otherwise the category is the name.
    val title = rule.notes.ifBlank { categoryName }
    val detail = listOfNotNull(
        stringResource(rule.frequency.labelResId),
        categoryName.takeIf { rule.notes.isNotBlank() && it.isNotBlank() },
    ).joinToString(" · ")
    val (prefix, amountColor) = when (rule.type) {
        TransactionType.EXPENSE -> "−" to colorResource(CommonR.color.red_500)
        TransactionType.INCOME -> "+" to colorResource(CommonR.color.green_500)
        TransactionType.TRANSFER -> "" to MaterialTheme.colorScheme.onSurface
    }
    val daysAway = remember(rule.nextDueDate) { daysUntil(rule.nextDueDate) }
    val isSoon = daysAway <= 1

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DateTile(date = rule.nextDueDate, highlighted = isSoon)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Repeat,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(12.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = prefix + item.amount,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = amountColor,
            )
            Spacer(modifier = Modifier.height(4.dp))
            DuePill(daysAway = daysAway, highlighted = isSoon)
        }
    }
}

/** Day of the month over a short month name, like a calendar page. */
@Composable
private fun DateTile(date: Date, highlighted: Boolean) {
    val locale = LocalConfiguration.current.locales[0]
    val local = remember(date) { date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() }
    val month = remember(local, locale) { local.format(DateTimeFormatter.ofPattern("MMM", locale)) }
    val container = if (highlighted) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val content = if (highlighted) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Column(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(container),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = local.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = content,
            lineHeight = 18.sp,
        )
        Text(
            text = month.uppercase(locale),
            style = MaterialTheme.typography.labelSmall,
            color = content.copy(alpha = 0.75f),
            lineHeight = 12.sp,
        )
    }
}

/** "Today", "Tomorrow" or "In 3 days", as a small pill; tinted when it's today or tomorrow. */
@Composable
private fun DuePill(daysAway: Long, highlighted: Boolean) {
    val text = when {
        // Due but not logged yet (the app was closed): it's added on the next start, so "Today".
        daysAway <= 0 -> stringResource(R.string.upcoming_today)

        daysAway == 1L -> stringResource(R.string.upcoming_tomorrow)

        else -> pluralStringResource(R.plurals.upcoming_in_days, daysAway.toInt(), daysAway.toInt())
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Medium,
        color = if (highlighted) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                if (highlighted) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                },
            )
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

private fun daysUntil(date: Date): Long {
    val zone = ZoneId.systemDefault()
    return ChronoUnit.DAYS.between(LocalDate.now(zone), date.toInstant().atZone(zone).toLocalDate())
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
