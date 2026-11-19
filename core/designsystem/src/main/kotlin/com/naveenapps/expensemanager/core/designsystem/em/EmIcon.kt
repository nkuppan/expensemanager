package com.naveenapps.expensemanager.core.designsystem.em

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme

/**
 * An icon at a token size (EmTheme.icons: sm 16, md 20, lg 24). [tint] defaults to the container's
 * content colour. [contentDescription] is null only when the icon is decoration next to text.
 */
@Composable
fun EmIcon(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = EmTheme.icons.lg,
    tint: Color = LocalContentColor.current,
) {
    Icon(imageVector = icon, contentDescription = contentDescription, modifier = modifier.size(size), tint = tint)
}

/** [EmIcon] for drawable resources (category and account icons). */
@Composable
fun EmIcon(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = EmTheme.icons.lg,
    tint: Color = LocalContentColor.current,
) {
    Icon(painter = painter, contentDescription = contentDescription, modifier = modifier.size(size), tint = tint)
}
