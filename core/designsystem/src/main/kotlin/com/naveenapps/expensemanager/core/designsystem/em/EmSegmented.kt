package com.naveenapps.expensemanager.core.designsystem.em

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme

/**
 * The one segmented control: a pill with a thumb that slides to the chosen option (Monthly /
 * Annual, Income / Expense / Transfer). Each option gets an equal share of the width.
 * [icons] and [accents], when given, match [options] by index. With accents the thumb is filled
 * with the chosen option's colour (green income, red expense, blue transfer) and its label turns
 * light, so the choice reads at a glance; every accent keeps at least 4.5:1 contrast.
 */
@Composable
fun EmSegmented(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    icons: List<ImageVector>? = null,
    accents: List<Color>? = null,
) {
    if (options.isEmpty()) return
    val colors = EmTheme.colors.scheme
    val track = colors.surfaceContainerHigh
    val accent = accents?.getOrNull(selectedIndex)
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(HEIGHT.dp)
            .clip(EmTheme.shapes.pill)
            .background(track)
            .padding(EmTheme.spacing.xs),
    ) {
        val segment = maxWidth / options.size
        val thumbOffset by animateDpAsState(
            targetValue = segment * selectedIndex.coerceAtLeast(0),
            animationSpec = tween(EmTheme.motion.mediumMillis, easing = EmTheme.motion.easing),
            label = "segmented thumb",
        )
        val thumb by animateColorAsState(
            targetValue = accent ?: colors.surfaceContainerLowest,
            animationSpec = tween(EmTheme.motion.mediumMillis, easing = EmTheme.motion.easing),
            label = "segmented thumb colour",
        )
        // Text on a solid accent: white in light theme, the dark background in dark theme.
        val onAccent = if (EmTheme.colors.isDark) colors.background else colors.surfaceContainerLowest
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .width(segment)
                .fillMaxHeight()
                .shadow(EmTheme.elevation.raised, EmTheme.shapes.pill)
                .background(thumb, EmTheme.shapes.pill),
        )
        Row(modifier = Modifier.fillMaxSize().selectableGroup()) {
            options.forEachIndexed { index, option ->
                val isSelected = index == selectedIndex
                val content = when {
                    isSelected && accent != null -> onAccent
                    isSelected -> colors.onSurface
                    else -> colors.onSurfaceVariant
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(EmTheme.shapes.pill)
                        .selectable(selected = isSelected, role = Role.Tab, onClick = { onSelect(index) }),
                    horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.xs, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    icons?.getOrNull(index)?.let {
                        EmIcon(icon = it, contentDescription = null, size = EmTheme.icons.sm, tint = content)
                    }
                    EmFitText(text = option, role = EmTextRole.Label, color = content)
                }
            }
        }
    }
}

/** [EmSegmented] over any option type; [label] names each option. */
@Composable
fun <T> EmSegmented(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: @Composable (T) -> String,
    modifier: Modifier = Modifier,
) {
    EmSegmented(
        options = options.map { label(it) },
        selectedIndex = options.indexOf(selected),
        onSelect = { onSelect(options[it]) },
        modifier = modifier,
    )
}

private const val HEIGHT = 44
