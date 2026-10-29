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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        MaterialTheme.colorScheme.error
    } else {
        colorResource(id = budget.progressBarColor)
    }
    val tickColor = MaterialTheme.colorScheme.outlineVariant
    val knobColor = MaterialTheme.colorScheme.surfaceContainerLowest

    // Sweeps in from empty the first time, then follows changes.
    val progress = remember(budget.id) { Animatable(0f) }
    LaunchedEffect(budget.id, budget.progress) {
        progress.animateTo(budget.progress, tween(900, easing = FastOutSlowInEasing))
    }

    if (showTitle) {
        Text(
            text = budget.title.text(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(12.dp))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .aspectRatio(2f),
    ) {
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
            drawArc(progressColor.copy(alpha = 0.12f), 180f, 180f, false, topLeft, arcSize, style = style)

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
            drawCircle(Color.Black.copy(alpha = 0.12f), radius = stroke / 2 + 6.dp.toPx(), center = knob + Offset(0f, 2.dp.toPx()))
            drawCircle(knobColor, radius = stroke / 2 + 5.dp.toPx(), center = knob)
            drawCircle(progressColor, radius = stroke / 2 - 1.dp.toPx(), center = knob)
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(
                    id = if (budget.isOverBudget) R.string.budget_over else R.string.budget_remaining,
                ).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp,
                color = if (budget.isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = budget.remaining,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
                color = if (budget.isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = R.string.budget_percent_used, (budget.progress * 100).roundToInt()),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = progressColor,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(progressColor.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 3.dp),
            )
        }
    }

    Spacer(modifier = Modifier.height(18.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
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
        Spacer(modifier = Modifier.height(10.dp))
        Hint(
            icon = Icons.Outlined.WarningAmber,
            tint = MaterialTheme.colorScheme.error,
            text = buildAnnotatedString { append(stringResource(id = R.string.budget_over_hint)) },
        )
    } else {
        budget.safePerDay?.let { perDayAmount ->
            val perDay = stringResource(id = R.string.budget_per_day, perDayAmount)
            val full = stringResource(id = R.string.budget_safe_to_spend, perDay, periodName(budget))
            val start = full.indexOf(perDay)
            val highlight = MaterialTheme.colorScheme.primary
            Spacer(modifier = Modifier.height(10.dp))
            Hint(
                icon = Icons.Outlined.Savings,
                tint = highlight,
                text = buildAnnotatedString {
                    append(full)
                    if (start >= 0) {
                        addStyle(SpanStyle(color = highlight, fontWeight = FontWeight.Bold), start, start + perDay.length)
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
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f))
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            if (trailingIcon != null) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = trailingIconDescription,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** A tinted strip with an icon: the daily allowance, or the over-budget warning. */
@Composable
private fun Hint(icon: ImageVector, tint: Color, text: androidx.compose.ui.text.AnnotatedString) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(tint.copy(alpha = 0.08f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
