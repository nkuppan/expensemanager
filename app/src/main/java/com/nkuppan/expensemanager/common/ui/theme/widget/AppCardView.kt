package com.nkuppan.expensemanager.common.ui.theme.widget

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AppCardView(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp
        ),
        content = content
    )
}