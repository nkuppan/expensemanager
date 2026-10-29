package com.naveenapps.expensemanager.core.designsystem.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme

/**
 * A pill-shaped switch between a few options with a thumb that slides to the selected one,
 * e.g. Monthly / Annual. Fills the width it's given; each option gets an equal share.
 */
@Composable
fun <T> SegmentedSwitch(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: @Composable (T) -> String,
    modifier: Modifier = Modifier,
) {
    if (options.isEmpty()) return
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(4.dp),
    ) {
        val segment = maxWidth / options.size
        val thumbOffset by animateDpAsState(
            targetValue = segment * options.indexOf(selected).coerceAtLeast(0),
            animationSpec = tween(250, easing = FastOutSlowInEasing),
            label = "segmented switch thumb",
        )
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .width(segment)
                .fillMaxHeight()
                .shadow(2.dp, CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest, CircleShape),
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .selectableGroup(),
        ) {
            options.forEach { option ->
                val isSelected = option == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .selectable(selected = isSelected, role = Role.Tab, onClick = { onSelect(option) }),
                    contentAlignment = Alignment.Center,
                ) {
                    SingleLineText(
                        text = label(option),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        ),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        },
                    )
                }
            }
        }
    }
}

@AppPreviewsLightAndDarkMode
@Composable
private fun SegmentedSwitchPreview() {
    ExpenseManagerPreviewTheme {
        SegmentedSwitch(
            options = listOf("Monthly", "Annual"),
            selected = "Monthly",
            onSelect = {},
            label = { it },
        )
    }
}
