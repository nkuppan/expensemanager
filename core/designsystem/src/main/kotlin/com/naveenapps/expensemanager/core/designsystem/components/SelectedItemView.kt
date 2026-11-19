package com.naveenapps.expensemanager.core.designsystem.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.naveenapps.expensemanager.core.designsystem.em.EmIcon
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme

@Composable
fun SelectedItemView(
    title: String,
    selectedCount: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
    ) {
        EmIcon(
            icon = icon,
            contentDescription = null,
        )

        EmText(
            text = title,
            modifier = Modifier
                .padding(start = EmTheme.spacing.lg, end = EmTheme.spacing.lg)
                .weight(1f)
                .align(Alignment.CenterVertically),
        )

        EmText(
            text = selectedCount,
            modifier = Modifier
                .padding(start = EmTheme.spacing.lg, end = EmTheme.spacing.lg)
                .align(Alignment.CenterVertically),
        )

        EmIcon(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
        )
    }
}
