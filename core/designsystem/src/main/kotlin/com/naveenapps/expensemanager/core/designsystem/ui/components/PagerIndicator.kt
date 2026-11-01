package com.naveenapps.expensemanager.core.designsystem.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme

/**
 * Dots under a horizontal pager: the current page is a wider pill in the primary colour, the others
 * small dots. Shows nothing for a single page. Screen readers get one [description] for the whole
 * row (e.g. "Budget 2 of 3") instead of an unlabelled dot per page.
 */
@Composable
fun PagerIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    if (pageCount <= 1) return
    val selectedPage = currentPage.coerceIn(0, pageCount - 1)

    Row(
        modifier = modifier.clearAndSetSemantics {
            if (description != null) contentDescription = description
        },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == selectedPage
            val width by animateDpAsState(
                targetValue = if (isSelected) 18.dp else 6.dp,
                label = "pager indicator width",
            )
            val color by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
                label = "pager indicator color",
            )
            Box(
                modifier = Modifier
                    .size(width = width, height = 6.dp)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}

@AppPreviewsLightAndDarkMode
@Composable
private fun PagerIndicatorPreview() {
    ExpenseManagerPreviewTheme {
        PagerIndicator(pageCount = 3, currentPage = 1, description = "Budget 2 of 3")
    }
}
