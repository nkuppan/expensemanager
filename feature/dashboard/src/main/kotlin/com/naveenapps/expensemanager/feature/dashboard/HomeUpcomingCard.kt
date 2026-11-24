package com.naveenapps.expensemanager.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountSize
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountText
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmDivider
import com.naveenapps.expensemanager.core.designsystem.em.EmEmptyAction
import com.naveenapps.expensemanager.core.designsystem.em.EmEmptyState
import com.naveenapps.expensemanager.core.designsystem.em.EmIcon
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.tint
import com.naveenapps.expensemanager.core.designsystem.ui.utils.displayName
import com.naveenapps.expensemanager.core.foundation.toLocalDateIn
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.feature.transaction.recurring.labelResId
import java.time.format.DateTimeFormatter
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toJavaLocalDate

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
    EmCard(
        modifier = modifier.fillMaxWidth(),
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
                        EmDivider()
                    }
                    UpcomingRow(item = item, onClick = { onAction(DashboardAction.OpenRecurringEdit(item.rule.id)) })
                }
            }
        }
    }
}

@Composable
private fun UpcomingTotals(upcoming: UpcomingUi) {
    Column(
        modifier = Modifier.padding(
            start = EmTheme.spacing.lg,
            end = EmTheme.spacing.lg,
            top = EmTheme.spacing.lg,
            bottom = EmTheme.spacing.xs,
        ),
    ) {
        EmText(
            text = pluralStringResource(R.plurals.upcoming_window, upcoming.windowDays, upcoming.windowDays),
            role = EmTextRole.Supporting,
            color = EmTextColor.Supporting,
        )
        Spacer(modifier = Modifier.height(EmTheme.spacing.sm))
        Row(horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.md)) {
            upcoming.expenseDue?.let {
                UpcomingTotalTile(
                    label = stringResource(R.string.upcoming_spend),
                    amount = it,
                    color = EmTheme.colors.expense,
                    modifier = Modifier.weight(1f),
                )
            }
            upcoming.incomeDue?.let {
                UpcomingTotalTile(
                    label = stringResource(R.string.upcoming_income),
                    amount = it,
                    color = EmTheme.colors.income,
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
            .clip(EmTheme.shapes.md)
            .background(color.tint())
            .padding(horizontal = EmTheme.spacing.md, vertical = EmTheme.spacing.sm),
    ) {
        EmAmountText(
            amount = amount,
            color = color,
        )
        EmText(
            text = label,
            role = EmTextRole.Supporting,
            color = EmTextColor.Supporting,
        )
    }
}

@Composable
private fun UpcomingRow(item: UpcomingItemUi, onClick: () -> Unit) {
    val rule = item.rule
    val categoryName = rule.category?.let { category ->
        category.displayName()
    }.orEmpty()
    // The notes name the payment when there are any ("Rent"), with the category beside the
    // frequency; otherwise the category is the name.
    val title = rule.notes.ifBlank { categoryName }
    val detail = listOfNotNull(
        stringResource(rule.frequency.labelResId),
        categoryName.takeIf { rule.notes.isNotBlank() && it.isNotBlank() },
    ).joinToString(" · ")
    val (prefix, amountColor) = when (rule.type) {
        TransactionType.EXPENSE -> "−" to EmTheme.colors.expense
        TransactionType.INCOME -> "+" to EmTheme.colors.income
        TransactionType.TRANSFER -> "" to EmTheme.colors.scheme.onSurface
    }
    val daysAway = remember(rule.nextDueDate) { daysUntil(rule.nextDueDate) }
    val isSoon = daysAway <= 1

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DateTile(date = rule.nextDueDate, highlighted = isSoon)
        Spacer(modifier = Modifier.width(EmTheme.spacing.lg))
        Column(modifier = Modifier.weight(1f)) {
            EmText(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(EmTheme.spacing.xxs))
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmIcon(
                    icon = Icons.Outlined.Repeat,
                    contentDescription = null,
                    size = EmTheme.icons.sm,
                    tint = EmTheme.colors.scheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(EmTheme.spacing.xs))
                EmText(
                    text = detail,
                    role = EmTextRole.Supporting,
                    color = EmTextColor.Supporting,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(modifier = Modifier.width(EmTheme.spacing.sm))
        Column(horizontalAlignment = Alignment.End) {
            EmAmountText(
                amount = prefix + item.amount,
                color = amountColor,
                size = EmAmountSize.Small,
            )
            Spacer(modifier = Modifier.height(EmTheme.spacing.xs))
            DuePill(daysAway = daysAway, highlighted = isSoon)
        }
    }
}

/** Day of the month over a short month name, like a calendar page. */
@Composable
private fun DateTile(date: Instant, highlighted: Boolean) {
    val locale = LocalConfiguration.current.locales[0]
    val local = remember(date) { date.toLocalDateIn() }
    val month = remember(local, locale) { local.toJavaLocalDate().format(DateTimeFormatter.ofPattern("MMM", locale)) }
    val container = if (highlighted) {
        EmTheme.colors.scheme.primaryContainer
    } else {
        EmTheme.colors.scheme.surfaceContainerHigh
    }
    val content = if (highlighted) {
        EmTheme.colors.scheme.onPrimaryContainer
    } else {
        EmTheme.colors.scheme.onSurface
    }
    Column(
        modifier = Modifier
            .size(44.dp)
            .clip(EmTheme.shapes.md)
            .background(container),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        EmText(text = local.dayOfMonth.toString(), role = EmTextRole.SectionTitle, color = content)
        EmText(text = month.uppercase(locale), role = EmTextRole.Overline, color = content)
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
    EmText(
        text = text,
        modifier = Modifier
            .clip(EmTheme.shapes.pill)
            .background(
                if (highlighted) {
                    EmTheme.colors.scheme.primaryContainer
                } else {
                    EmTheme.colors.scheme.surfaceContainerHigh
                },
            )
            .padding(horizontal = EmTheme.spacing.sm, vertical = EmTheme.spacing.xxs),
        role = EmTextRole.Supporting,
        color = if (highlighted) {
            EmTheme.colors.scheme.onPrimaryContainer
        } else {
            EmTheme.colors.scheme.onSurfaceVariant
        },
    )
}

private fun daysUntil(date: Instant): Long = Clock.System.now().toLocalDateIn().daysUntil(date.toLocalDateIn()).toLong()

@Composable
private fun UpcomingEmpty(onPlan: () -> Unit) {
    EmEmptyState(
        icon = Icons.Outlined.EventRepeat,
        title = stringResource(R.string.upcoming_empty_title),
        body = stringResource(R.string.upcoming_empty_description),
        action = EmEmptyAction(stringResource(R.string.upcoming_plan), onPlan),
        compact = true,
    )
}
