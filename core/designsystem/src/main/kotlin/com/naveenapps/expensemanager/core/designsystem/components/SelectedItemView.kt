package com.naveenapps.expensemanager.core.designsystem.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp


@Composable
fun SelectedItemView(
    title: String,
    selectedCount: String,
    icon: Painter,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
    ) {
        Icon(
            painter = icon,
            contentDescription = null
        )

        Text(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp)
                .weight(1f)
                .align(Alignment.CenterVertically),
            text = title,
        )

        Text(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp)
                .align(Alignment.CenterVertically),
            text = selectedCount,
        )

        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null
        )
    }
}