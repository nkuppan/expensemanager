package com.naveenapps.expensemanager.core.designsystem.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AppCardView(
    modifier: Modifier = Modifier,
    elevation: Dp = 0.dp,
    cornerSize: Dp = 12.dp,
    border: BorderStroke = CardDefaults.outlinedCardBorder(),
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(cornerSize),
    content: @Composable ColumnScope.() -> Unit,
) {
    if (onClick != null) {
        OutlinedCard(
            modifier = modifier,
            onClick = onClick,
            shape = shape,
            border = border,
            content = content,
            elevation = CardDefaults.outlinedCardElevation(elevation),
        )
    } else {
        OutlinedCard(
            modifier = modifier,
            shape = shape,
            border = border,
            content = content,
            elevation = CardDefaults.outlinedCardElevation(elevation),
        )
    }
}


object AppCardViewDefaults {

    @Composable
    fun cardShape(
        index: Int,
        items: List<*>
    ): CornerBasedShape = when {
        items.size == 1 -> RoundedCornerShape(16.dp)
        index == 0 -> RoundedCornerShape(
            topStart = 16.dp, topEnd = 16.dp,
            bottomStart = 4.dp, bottomEnd = 4.dp,
        )

        index == items.lastIndex -> RoundedCornerShape(
            topStart = 4.dp, topEnd = 4.dp,
            bottomStart = 16.dp, bottomEnd = 16.dp,
        )

        else -> RoundedCornerShape(4.dp)
    }
}