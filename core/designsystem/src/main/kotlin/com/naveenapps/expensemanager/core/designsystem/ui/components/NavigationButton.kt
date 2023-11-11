package com.naveenapps.expensemanager.core.designsystem.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController

@Composable
fun NavigationButton(
    navController: NavController,
    navigationIcon: ImageVector = Icons.Default.ArrowBack,
    navigationContentDescription: String = ""
) {
    IconButton(
        onClick = {
            navController.popBackStack()
        }
    ) {
        Icon(
            imageVector = navigationIcon,
            contentDescription = navigationContentDescription
        )
    }
}