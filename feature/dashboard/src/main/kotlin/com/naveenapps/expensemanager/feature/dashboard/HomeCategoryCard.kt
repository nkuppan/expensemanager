package com.naveenapps.expensemanager.feature.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.model.CategoryTransaction
import com.naveenapps.expensemanager.core.model.CategoryTransactionState
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.feature.category.transaction.getRandomCategoryTransactionData
import java.text.NumberFormat

/** Categories listed beside the donut; anything after them is grouped into one "other" slice. */
private const val TOP_CATEGORIES = 4

/**
 * Home's categories card, in the budget card's style but compact: a donut on the left with the
 * period's total in the middle, and the top categories on the right, each with its share as a bar.
 * Tapping the donut switches between expense and income; the centre names the side shown, in its
 * colour, with a swap icon. With nothing logged for that side, it offers to add a transaction.
 */
@Composable
internal fun HomeCategoryCard(
    expense: CategoryTransactionState,
    income: CategoryTransactionState,
    onAddTransaction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var type by rememberSaveable { mutableStateOf(CategoryType.EXPENSE) }
    val shown = if (type == CategoryType.EXPENSE) expense else income

    AppCardView(
        modifier = modifier.fillMaxWidth(),
        cornerSize = 16.dp,
    ) {
        AnimatedContent(
            targetState = type to shown,
            transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(120)) },
            // Animate when switching sides or when a side becomes empty / non-empty, not on every new figure.
            contentKey = { (shownType, state) -> shownType to state.categoryTransactions.isEmpty() },
            label = "home categories",
        ) { (shownType, state) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CategoryDonut(
                    type = shownType,
                    state = state,
                    onToggle = {
                        type = if (type == CategoryType.EXPENSE) CategoryType.INCOME else CategoryType.EXPENSE
                    },
                )

                Spacer(modifier = Modifier.width(16.dp))

                if (state.categoryTransactions.isEmpty()) {
                    NoCategorySpending(
                        type = shownType,
                        onAddTransaction = onAddTransaction,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    CategoryLegend(state = state, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Full-circle counterpart of the budget gauge (same tinted track and grow-in animation), with
 * square-ended slices so small categories keep their true size. Sized for a side-by-side layout;
 * tapping it switches between expense and income.
 */
@Composable
private fun CategoryDonut(
    type: CategoryType,
    state: CategoryTransactionState,
    onToggle: () -> Unit,
) {
    val typeColor = type.color()
    val trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)
    val slices = slicesOf(state)
    val reveal = remember { Animatable(0f) }
    LaunchedEffect(slices) {
        reveal.snapTo(0f)
        reveal.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
    }

    Box(
        modifier = Modifier
            .size(124.dp)
            .clip(CircleShape)
            .clickable(
                role = Role.Button,
                onClickLabel = stringResource(
                    id = if (type == CategoryType.EXPENSE) {
                        R.string.home_categories_show_income
                    } else {
                        R.string.home_categories_show_expense
                    },
                ),
                onClick = onToggle,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 14.dp.toPx()
            val diameter = size.minDimension - stroke
            val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
            val arcSize = Size(diameter, diameter)
            val style = Stroke(width = stroke)

            drawArc(trackColor, 0f, 360f, false, topLeft, arcSize, style = style)

            // Square-ended slices with a thin, even gap between neighbours, so each slice keeps its
            // true size. (Rounded ends overhang the slice, and trimming them to fit made small
            // categories into blobs and visibly distorted the proportions on a ring this size.)
            // A slice narrower than two gaps keeps at least half its width, so 1% still shows.
            val gapDegrees = if (slices.size > 1) 2.5f else 0f
            var start = -90f
            slices.forEach { (color, fraction) ->
                val sweep = 360f * fraction * reveal.value
                val drawn = (sweep - gapDegrees).coerceAtLeast(sweep / 2)
                drawArc(color, start + (sweep - drawn) / 2, drawn, false, topLeft, arcSize, style = style)
                start += sweep
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BasicText(
                    text = stringResource(id = if (type == CategoryType.EXPENSE) R.string.expense else R.string.income),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = typeColor,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    maxLines = 1,
                    autoSize = TextAutoSize.StepBased(minFontSize = 8.sp, maxFontSize = 12.sp),
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Outlined.SwapHoriz,
                    contentDescription = null,
                    tint = typeColor,
                    modifier = Modifier.size(14.dp),
                )
            }
            // Large totals shrink to fit the ring instead of being cut off.
            BasicText(
                text = state.totalAmount.amountString.orEmpty(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                ),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(minFontSize = 10.sp, maxFontSize = 18.sp),
            )
        }
    }
}

@Composable
private fun CategoryLegend(state: CategoryTransactionState, modifier: Modifier = Modifier) {
    val fallback = MaterialTheme.colorScheme.primary
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        state.categoryTransactions.take(TOP_CATEGORIES).forEach { item ->
            LegendRow(item = item, color = item.color(fallback))
        }
    }
}

/** Colour dot, name and amount, with the category's share of the total as a thin bar below. */
@Composable
private fun LegendRow(item: CategoryTransaction, color: Color) {
    val name = item.category.titleResId?.let { stringResource(it) } ?: item.category.name
    val share = item.percent / 100f
    val shareText = remember(share) { NumberFormat.getPercentInstance().format(share.toDouble()) }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = item.amount.amountString.orEmpty(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(share.coerceIn(0f, 1f))
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(color),
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = shareText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Nothing logged for this side: say so, and offer to add a transaction. The donut stays tappable. */
@Composable
private fun NoCategorySpending(type: CategoryType, onAddTransaction: () -> Unit, modifier: Modifier = Modifier) {
    val message = if (type == CategoryType.EXPENSE) {
        R.string.home_categories_none_expense
    } else {
        R.string.home_categories_none_income
    }
    Column(modifier = modifier) {
        Text(
            text = stringResource(id = message),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(10.dp))
        FilledTonalButton(
            onClick = onAddTransaction,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Icon(imageVector = Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = stringResource(id = R.string.home_categories_add), maxLines = 1)
        }
    }
}

/** Top categories as slices, plus one grey slice for the rest so the ring always adds up to the total. */
@Composable
private fun slicesOf(state: CategoryTransactionState): List<Pair<Color, Float>> {
    val fallback = MaterialTheme.colorScheme.primary
    val otherColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
    return remember(state.categoryTransactions, fallback, otherColor) {
        val top = state.categoryTransactions.take(TOP_CATEGORIES).map { it.color(fallback) to it.percent / 100f }
        val rest = 1f - top.sumOf { it.second.toDouble() }.toFloat()
        if (state.categoryTransactions.size > TOP_CATEGORIES && rest > 0.005f) top + (otherColor to rest) else top
    }
}

/** Expense in the theme's error red, income in its primary colour. */
@Composable
private fun CategoryType.color(): Color =
    if (this == CategoryType.EXPENSE) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

/** The category's own colour, as used for its icon; [fallback] if it isn't a valid colour. */
private fun CategoryTransaction.color(fallback: Color): Color =
    runCatching { Color(category.storedIcon.backgroundColor.toColorInt()) }.getOrDefault(fallback)

@AppPreviewsLightAndDarkMode
@Composable
private fun HomeCategoryCardPreview() {
    ExpenseManagerPreviewTheme(padding = 16.dp) {
        HomeCategoryCard(
            expense = getRandomCategoryTransactionData(),
            income = getRandomCategoryTransactionData(),
            onAddTransaction = {},
        )
    }
}

@AppPreviewsLightAndDarkMode
@Composable
private fun HomeCategoryCardEmptyPreview() {
    ExpenseManagerPreviewTheme(padding = 16.dp) {
        HomeCategoryCard(
            expense = getRandomCategoryTransactionData().copy(categoryTransactions = emptyList()),
            income = getRandomCategoryTransactionData().copy(categoryTransactions = emptyList()),
            onAddTransaction = {},
        )
    }
}
