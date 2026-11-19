package com.naveenapps.expensemanager.feature.category.selection

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.naveenapps.expensemanager.core.designsystem.em.EmIcon
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme

object CategoryItemDefaults {

    @Composable
    fun MultiCheckedTrailing(isSelected: Boolean) {
        if (isSelected) {
            EmIcon(
                icon = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = EmTheme.colors.scheme.primary,
            )
        } else {
            EmIcon(
                icon = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = EmTheme.colors.scheme.surfaceVariant,
            )
        }
    }

    @Composable
    fun SingleCheckedTrailing(isSelected: Boolean) {
        if (isSelected) {
            EmIcon(
                icon = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = EmTheme.colors.scheme.primary,
            )
        }
    }

    @Composable
    fun ChevronTrailing() {
        EmIcon(
            icon = Icons.Default.ChevronRight,
            contentDescription = null,
            size = EmTheme.icons.md,
            tint = EmTheme.colors.scheme.onSurfaceVariant,
        )
    }

    @Composable
    fun IconTrailing(icon: ImageVector) {
        EmIcon(
            icon = icon,
            contentDescription = null,
            size = EmTheme.icons.md,
            tint = EmTheme.colors.scheme.onSurfaceVariant,
        )
    }
}
