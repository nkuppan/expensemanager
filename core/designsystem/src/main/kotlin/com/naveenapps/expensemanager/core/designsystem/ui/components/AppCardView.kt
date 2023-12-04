package com.naveenapps.expensemanager.core.designsystem.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val roundedCornerShape = RoundedCornerShape(8.dp)

@Composable
fun AppCardView(
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
    content: @Composable () -> Unit,
) {
    val backgroundBubbleColor = if (isPrimary) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Surface(
        modifier = modifier,
        color = backgroundBubbleColor,
        shape = roundedCornerShape,
        content = content,
    )
}
