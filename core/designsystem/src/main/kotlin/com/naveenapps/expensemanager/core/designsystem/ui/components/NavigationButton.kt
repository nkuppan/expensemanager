package com.naveenapps.expensemanager.core.designsystem.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun NavigationButton(
    onClick: () -> Unit,
    navigationIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    navigationContentDescription: String = "",
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = navigationIcon,
            contentDescription = navigationContentDescription,
        )
    }
}
