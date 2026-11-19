package com.naveenapps.expensemanager.core.designsystem.em

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.tint

@Composable
fun EmSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Switch(checked = checked, onCheckedChange = onCheckedChange, modifier = modifier, enabled = enabled)
}

@Composable
fun EmCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Checkbox(checked = checked, onCheckedChange = onCheckedChange, modifier = modifier, enabled = enabled)
}

@Composable
fun EmRadio(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    RadioButton(selected = selected, onClick = onClick, modifier = modifier, enabled = enabled)
}

enum class EmLoadingVariant {
    /** Whole screen or section is loading: centred, large. */
    Screen,

    /** Inside a row or next to text: small. */
    Inline,
}

@Composable
fun EmLoading(
    modifier: Modifier = Modifier,
    variant: EmLoadingVariant = EmLoadingVariant.Screen,
) {
    when (variant) {
        EmLoadingVariant.Screen -> Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = EmTheme.spacing.xxxl),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(EmTheme.spacing.xxxl),
                strokeWidth = 3.dp,
                strokeCap = StrokeCap.Round,
                color = EmTheme.colors.scheme.primary,
                trackColor = EmTheme.colors.scheme.surfaceContainerHighest,
            )
        }

        EmLoadingVariant.Inline -> CircularProgressIndicator(
            modifier = modifier.size(EmTheme.icons.md),
            strokeWidth = 2.dp,
            color = EmTheme.colors.scheme.primary,
        )
    }
}

/** How full a progress bar is, by meaning; budget bars use budgetProgressColor's buckets. */
enum class EmProgressStatus { Default, Safe, Watch, Risk, Over }

/**
 * Linear progress. [progress] null = indeterminate (a running backup); 0..1 otherwise.
 */
@Composable
fun EmProgress(
    progress: Float?,
    modifier: Modifier = Modifier,
    status: EmProgressStatus = EmProgressStatus.Default,
    /** A colour that carries data (a category's own colour); overrides [status]. */
    color: Color = Color.Unspecified,
) {
    val colors = EmTheme.colors
    val statusColor = when (status) {
        EmProgressStatus.Default -> colors.scheme.primary
        EmProgressStatus.Safe -> colors.budgetSafe
        EmProgressStatus.Watch -> colors.budgetWatch
        EmProgressStatus.Risk -> colors.budgetRisk
        EmProgressStatus.Over -> colors.budgetOver
    }
    val barColor = color.takeOrElse { statusColor }
    val track = if (color.isSpecified) color.tint() else colors.scheme.surfaceContainerHighest
    val sized = modifier.fillMaxWidth()
    if (progress == null) {
        LinearProgressIndicator(modifier = sized, color = barColor, trackColor = track, strokeCap = StrokeCap.Round)
    } else {
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = sized,
            color = barColor,
            trackColor = track,
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp,
            drawStopIndicator = {},
        )
    }
}
