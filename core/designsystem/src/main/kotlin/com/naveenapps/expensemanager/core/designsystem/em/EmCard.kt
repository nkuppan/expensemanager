package com.naveenapps.expensemanager.core.designsystem.em

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme

enum class EmCardVariant {
    /** Default: tonal surface, no border. */
    Filled,

    /** Card on a surface of the same tone (lists on the background). */
    Outlined,

    /** Draws attention: reminder / what's-new banners, the selected option. */
    Highlight,

    /** Something is wrong and needs the user (backup failing): error container colours. */
    Error,
}

/** Card borders: the hairline outline, or the 2 dp primary outline that marks the selected card. */
object EmCardDefaults {
    @Composable
    fun border(selected: Boolean = false): BorderStroke = if (selected) {
        BorderStroke(2.dp, EmTheme.colors.scheme.primary)
    } else {
        CardDefaults.outlinedCardBorder()
    }
}

/**
 * Where a card sits in a stack of cards: alone (16 dp corners all round) or as item [index] of
 * [count] in a grouped list (round outside, tight between items).
 */
@Immutable
data class EmCardPosition(val index: Int, val count: Int) {
    companion object {
        val Single = EmCardPosition(0, 1)

        fun of(index: Int, items: List<*>) = EmCardPosition(index, items.size)
    }
}

/**
 * The one card. Its corners come from [position]; [selected] draws the 2 dp primary outline used
 * by pickers.
 */
@Composable
fun EmCard(
    modifier: Modifier = Modifier,
    variant: EmCardVariant = EmCardVariant.Outlined,
    onClick: (() -> Unit)? = null,
    position: EmCardPosition = EmCardPosition.Single,
    selected: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    EmCardSurface(
        modifier = modifier,
        variant = variant,
        shape = emGroupedShape(position.index, position.count),
        onClick = onClick,
        border = if (selected) EmCardDefaults.border(selected = true) else null,
        content = content,
    )
}

/**
 * Card drawing shared by [EmCard], [EmGroupedList] and the legacy AppCardView. Internal: features
 * can't choose a shape or border.
 */
@Composable
internal fun EmCardSurface(
    modifier: Modifier,
    variant: EmCardVariant,
    shape: Shape,
    onClick: (() -> Unit)?,
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scheme = EmTheme.colors.scheme
    val elevation = CardDefaults.cardElevation(defaultElevation = EmTheme.elevation.none)
    when (variant) {
        EmCardVariant.Outlined -> {
            val colors = CardDefaults.outlinedCardColors(containerColor = scheme.surfaceContainerLowest)
            val stroke = border ?: CardDefaults.outlinedCardBorder()
            if (onClick != null) {
                OutlinedCard(onClick, modifier, shape = shape, colors = colors, border = stroke, content = content)
            } else {
                OutlinedCard(modifier, shape = shape, colors = colors, border = stroke, content = content)
            }
        }

        EmCardVariant.Filled, EmCardVariant.Highlight, EmCardVariant.Error -> {
            val colors = when (variant) {
                EmCardVariant.Filled -> CardDefaults.cardColors(
                    containerColor = scheme.surfaceContainerLow,
                    contentColor = scheme.onSurface,
                )

                EmCardVariant.Highlight -> CardDefaults.cardColors(
                    containerColor = scheme.primaryContainer,
                    contentColor = scheme.onPrimaryContainer,
                )

                else -> CardDefaults.cardColors(
                    containerColor = scheme.errorContainer,
                    contentColor = scheme.onErrorContainer,
                )
            }
            if (onClick != null) {
                Card(onClick, modifier, shape = shape, colors = colors, elevation = elevation, border = border, content = content)
            } else {
                Card(modifier, shape = shape, colors = colors, elevation = elevation, border = border, content = content)
            }
        }
    }
}

private val GroupOuter = 16.dp
private val GroupInner = 4.dp

/** Shape of item [index] of [count] in a grouped list: round outside, tight between items. */
fun emGroupedShape(index: Int, count: Int): CornerBasedShape = when {
    count <= 1 -> RoundedCornerShape(GroupOuter)
    index == 0 -> RoundedCornerShape(GroupOuter, GroupOuter, GroupInner, GroupInner)
    index == count - 1 -> RoundedCornerShape(GroupInner, GroupInner, GroupOuter, GroupOuter)
    else -> RoundedCornerShape(GroupInner)
}

/**
 * Rows grouped into one block of cards with 2 dp gaps (the Settings look). Use for short lists in a
 * Column; in a LazyColumn use [emGroupedItems].
 */
@Composable
fun <T> EmGroupedList(
    items: List<T>,
    modifier: Modifier = Modifier,
    onItemClick: ((T) -> Unit)? = null,
    itemContent: @Composable (T) -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.xxs)) {
        items.forEachIndexed { index, item ->
            EmCardSurface(
                modifier = Modifier,
                variant = EmCardVariant.Outlined,
                shape = emGroupedShape(index, items.size),
                onClick = onItemClick?.let { { it(item) } },
            ) {
                itemContent(item)
            }
        }
    }
}

/** [EmGroupedList] for a LazyColumn. Put spacedBy(EmTheme.spacing.xxs) on the LazyColumn. */
fun <T> LazyListScope.emGroupedItems(
    items: List<T>,
    key: ((T) -> Any)? = null,
    onItemClick: ((T) -> Unit)? = null,
    itemContent: @Composable (T) -> Unit,
) {
    val indexedKey: ((Int, T) -> Any)? = key?.let { k -> { _: Int, item: T -> k(item) } }
    itemsIndexed(items, key = indexedKey) { index, item ->
        EmCardSurface(
            modifier = Modifier,
            variant = EmCardVariant.Outlined,
            shape = emGroupedShape(index, items.size),
            onClick = onItemClick?.let { { it(item) } },
        ) {
            itemContent(item)
        }
    }
}
