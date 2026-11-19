package com.naveenapps.expensemanager.feature.budget.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountSize
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountText
import com.naveenapps.expensemanager.core.designsystem.em.EmIcon
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.em.emColorOf
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.tint
import com.naveenapps.expensemanager.core.model.BudgetPeriod
import com.naveenapps.expensemanager.feature.budget.R
import com.naveenapps.expensemanager.feature.budget.text
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * The budget gauge: its name, a half-circle that fills as it's spent with what's left in the
 * middle, Spent and Limit tiles, and the daily allowance (or an over-budget warning).
 * Shown on Home and on the budget's details screen.
 */
@Composable
fun BudgetGauge(
    budget: BudgetProgress,
    modifier: Modifier = Modifier,
    showTitle: Boolean = true,
    onEdit: (() -> Unit)? = null,
) = Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
    val progressColor = if (budget.isOverBudget) {
        EmTheme.colors.scheme.error
    } else {
        emColorOf(budget.progressBarColor)
    }
    val tickColor = EmTheme.colors.scheme.outlineVariant
    val knobColor = EmTheme.colors.scheme.surfaceContainerLowest

    // Sweeps in from empty the first time, then follows changes.
    val progress = remember(budget.id) { Animatable(0f) }
    LaunchedEffect(budget.id, budget.progress) {
        progress.animateTo(budget.progress, tween(900, easing = FastOutSlowInEasing))
    }

    if (showTitle) {
        EmText(
            text = budget.title.text(),
            role = EmTextRole.SectionTitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(EmTheme.spacing.md))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = EmTheme.spacing.xl)
            .aspectRatio(2f),
    ) {
        val shadowColor = EmTheme.colors.scheme.scrim.tint()
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 20.dp.toPx()
            // A half circle whose flat side is the bottom edge of the box.
            val diameter = min(size.width, size.height * 2) - stroke
            val radius = diameter / 2
            val topLeft = Offset((size.width - diameter) / 2, stroke / 2)
            val center = Offset(topLeft.x + radius, topLeft.y + radius)
            val arcSize = Size(diameter, diameter)
            val style = Stroke(width = stroke, cap = StrokeCap.Round)

            // Track, tinted with the budget's colour.
            drawArc(progressColor.tint(), 180f, 180f, false, topLeft, arcSize, style = style)

            // Quarter marks inside the arc.
            val tickRadius = radius - stroke / 2 - 10.dp.toPx()
            for (step in 0..4) {
                val angle = Math.toRadians(180.0 + 45.0 * step)
                drawCircle(
                    color = tickColor,
                    radius = 2.dp.toPx(),
                    center = Offset(center.x + tickRadius * cos(angle).toFloat(), center.y + tickRadius * sin(angle).toFloat()),
                )
            }

            val value = progress.value
            if (value > 0f) {
                // Lighter at the start, full colour where spending has got to.
                val brush = Brush.sweepGradient(
                    0.5f to progressColor.copy(alpha = 0.45f),
                    (0.5f + value / 2f).coerceAtMost(1f) to progressColor,
                    center = center,
                )
                drawArc(brush, 180f, 180f * value, false, topLeft, arcSize, style = style)
            }

            // Knob: a soft shadow, a white ring and the budget colour inside.
            val angle = Math.toRadians(180.0 + 180.0 * value)
            val knob = Offset(center.x + radius * cos(angle).toFloat(), center.y + radius * sin(angle).toFloat())
            drawCircle(shadowColor, radius = stroke / 2 + 6.dp.toPx(), center = knob + Offset(0f, 2.dp.toPx()))
            drawCircle(knobColor, radius = stroke / 2 + 5.dp.toPx(), center = knob)
            drawCircle(progressColor, radius = stroke / 2 - 1.dp.toPx(), center = knob)
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            EmText(
                text = stringResource(
                    id = if (budget.isOverBudget) R.string.budget_over else R.string.budget_remaining,
                ),
                role = EmTextRole.Overline,
                color = if (budget.isOverBudget) EmTheme.colors.scheme.error else EmTheme.colors.scheme.onSurfaceVariant,
            )
            EmAmountText(
                amount = budget.remaining,
                color = if (budget.isOverBudget) EmTheme.colors.scheme.error else EmTheme.colors.scheme.onSurface,
                size = EmAmountSize.Large,
            )
            Spacer(modifier = Modifier.height(EmTheme.spacing.xs))
            EmText(
                text = stringResource(id = R.string.budget_percent_used, (budget.progress * 100).roundToInt()),
                modifier = Modifier
                    .clip(EmTheme.shapes.pill)
                    .background(progressColor.tint())
                    .padding(horizontal = EmTheme.spacing.sm, vertical = EmTheme.spacing.xs),
                role = EmTextRole.Label,
                color = progressColor,
            )
        }
    }

    Spacer(modifier = Modifier.height(EmTheme.spacing.lg))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
    ) {
        StatTile(
            label = stringResource(id = R.string.budget_spent),
            value = budget.spent,
            dotColor = progressColor,
            modifier = Modifier.weight(1f),
        )
        StatTile(
            label = stringResource(id = R.string.budget_limit),
            value = budget.limit,
            dotColor = progressColor.copy(alpha = 0.25f),
            modifier = Modifier.weight(1f),
            trailingIcon = if (onEdit != null) Icons.Outlined.Edit else null,
            trailingIconDescription = stringResource(id = R.string.a11y_edit_budget),
            onClick = onEdit,
        )
    }

    if (budget.isOverBudget) {
        Spacer(modifier = Modifier.height(EmTheme.spacing.sm))
        Hint(
            icon = Icons.Outlined.WarningAmber,
            tint = EmTheme.colors.scheme.error,
            text = buildAnnotatedString { append(stringResource(id = R.string.budget_over_hint)) },
        )
    } else {
        budget.safePerDay?.let { perDayAmount ->
            val perDay = stringResource(id = R.string.budget_per_day, perDayAmount)
            val full = stringResource(id = R.string.budget_safe_to_spend, perDay, periodName(budget))
            val start = full.indexOf(perDay)
            val highlight = EmTheme.colors.scheme.primary
            val emphasis = EmTheme.type.sectionTitle.fontWeight
            Spacer(modifier = Modifier.height(EmTheme.spacing.sm))
            Hint(
                icon = Icons.Outlined.Savings,
                tint = highlight,
                text = buildAnnotatedString {
                    append(full)
                    if (start >= 0) {
                        addStyle(SpanStyle(color = highlight, fontWeight = emphasis), start, start + perDay.length)
                    }
                },
            )
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    dotColor: Color,
    modifier: Modifier = Modifier,
    trailingIcon: ImageVector? = null,
    trailingIconDescription: String? = null,
    onClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .clip(EmTheme.shapes.lg)
            .background(EmTheme.colors.scheme.surfaceContainerHigh)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = EmTheme.spacing.md, vertical = EmTheme.spacing.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(EmTheme.shapes.pill)
                    .background(dotColor),
            )
            Spacer(modifier = Modifier.width(EmTheme.spacing.sm))
            EmText(
                text = label,
                modifier = Modifier.weight(1f),
                role = EmTextRole.Supporting,
                color = EmTextColor.Supporting,
            )
            if (trailingIcon != null) {
                EmIcon(
                    icon = trailingIcon,
                    contentDescription = trailingIconDescription,
                    size = EmTheme.icons.sm,
                    tint = EmTheme.colors.scheme.onSurfaceVariant,
                )
            }
        }
        Spacer(modifier = Modifier.height(EmTheme.spacing.xs))
        EmAmountText(
            amount = value,
        )
    }
}

/** A tinted strip with an icon: the daily allowance, or the over-budget warning. */
@Composable
private fun Hint(icon: ImageVector, tint: Color, text: androidx.compose.ui.text.AnnotatedString) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(EmTheme.shapes.lg)
            .background(tint.tint())
            .padding(horizontal = EmTheme.spacing.md, vertical = EmTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EmIcon(icon = icon, contentDescription = null, size = EmTheme.icons.md, tint = tint)
        Spacer(modifier = Modifier.width(EmTheme.spacing.sm))
        EmText(
            text = text,
            color = EmTextColor.Supporting,
        )
    }
}

/** "October" for a monthly budget, "2026" for a yearly one, in the user's language. */
@Composable
private fun periodName(budget: BudgetProgress): String {
    val locale = LocalConfiguration.current.locales[0]
    return remember(budget.periodStart, budget.periodType, locale) {
        when (budget.periodType) {
            BudgetPeriod.MONTHLY -> budget.periodStart.format(DateTimeFormatter.ofPattern("LLLL", locale))
            BudgetPeriod.YEARLY -> budget.periodStart.year.toString()
        }
    }
}
