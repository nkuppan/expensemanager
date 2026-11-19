package com.naveenapps.expensemanager.core.designsystem.em

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.naveenapps.expensemanager.core.designsystem.R
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme

enum class EmChipVariant {
    /** Toggles a filter or option on and off; shows a check when selected. */
    Filter,

    /** An active value that can be removed (an applied filter); shows a close icon. */
    Input,

    /** A one-tap suggestion (quick pick, receipt amount). */
    Suggestion,
}

/**
 * The one chip. [onRemove] is used by Input chips only; its close icon reads "Remove <label>".
 */
@Composable
fun EmChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: EmChipVariant = EmChipVariant.Filter,
    selected: Boolean = false,
    leadingIcon: ImageVector? = null,
    leadingPainter: Painter? = null,
    onRemove: (() -> Unit)? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val shape = EmTheme.shapes.sm
    val text: @Composable () -> Unit = {
        Text(text = label, style = EmTheme.type.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
    val leading: (@Composable () -> Unit)? = when {
        loading -> ({ EmLoading(variant = EmLoadingVariant.Inline, modifier = Modifier.size(EmTheme.icons.sm)) })
        leadingIcon != null -> ({ EmIcon(icon = leadingIcon, contentDescription = null, size = EmTheme.icons.sm) })
        leadingPainter != null -> ({ EmIcon(painter = leadingPainter, contentDescription = null, size = EmTheme.icons.sm) })
        else -> null
    }
    when (variant) {
        EmChipVariant.Filter -> FilterChip(
            selected = selected,
            onClick = onClick,
            label = text,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            leadingIcon = leading ?: if (selected) {
                { EmIcon(icon = Icons.Default.Check, contentDescription = null, size = EmTheme.icons.sm) }
            } else {
                null
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = EmTheme.colors.scheme.primaryContainer,
                selectedLabelColor = EmTheme.colors.scheme.onPrimaryContainer,
                selectedLeadingIconColor = EmTheme.colors.scheme.onPrimaryContainer,
            ),
        )

        EmChipVariant.Input -> InputChip(
            selected = true,
            onClick = onClick,
            label = text,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            leadingIcon = leading,
            trailingIcon = onRemove?.let { remove ->
                {
                    EmIconButtonSmall(
                        icon = Icons.Default.Close,
                        contentDescription = stringResource(R.string.a11y_remove, label),
                        onClick = remove,
                    )
                }
            },
            colors = InputChipDefaults.inputChipColors(
                selectedContainerColor = EmTheme.colors.scheme.secondaryContainer,
                selectedLabelColor = EmTheme.colors.scheme.onSecondaryContainer,
            ),
        )

        EmChipVariant.Suggestion -> AssistChip(
            onClick = onClick,
            label = text,
            modifier = modifier,
            enabled = enabled && !loading,
            shape = shape,
            leadingIcon = leading,
            colors = AssistChipDefaults.assistChipColors(
                leadingIconContentColor = EmTheme.colors.scheme.primary,
            ),
        )
    }
}

/** A horizontally scrolling row of chips with the standard gap. */
@Composable
fun EmChipRow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
    ) {
        content()
    }
}

/** Small tappable icon inside a chip (Input chip remove). The chip provides the touch area. */
@Composable
private fun EmIconButtonSmall(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    EmIcon(
        icon = icon,
        contentDescription = contentDescription,
        size = EmTheme.icons.sm,
        modifier = Modifier.clickable(role = Role.Button, onClick = onClick),
    )
}
