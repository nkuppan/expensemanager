package com.naveenapps.expensemanager.core.designsystem.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.naveenapps.expensemanager.core.designsystem.em.EmIconButton

@Composable
fun NavigationButton(
    onClick: () -> Unit,
    navigationIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    navigationContentDescription: String = "",
) {
    EmIconButton(icon = navigationIcon, contentDescription = navigationContentDescription, onClick = onClick)
}
