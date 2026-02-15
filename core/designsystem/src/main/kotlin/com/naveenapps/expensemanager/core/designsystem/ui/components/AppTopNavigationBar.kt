package com.naveenapps.expensemanager.core.designsystem.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.components.AppTopAppBar
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme

@Composable
fun ExpenseManagerTopAppBar(
    title: String?,
    navigationIcon: ImageVector? = null,
    navigationBackClick: (() -> Unit) = {},
    actions: @Composable (RowScope.() -> Unit) = {},
    titleClick: (() -> Unit)? = null
) {
    AppTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background, // Match background
            scrolledContainerColor = MaterialTheme.colorScheme.background, // Match even when scrolled
            titleContentColor = MaterialTheme.colorScheme.onBackground,
        ),
        title = title,
        navigationIcon = navigationIcon,
        navigationBackClick = navigationBackClick,
        titleClick = titleClick,
        actions = actions,
    )
}

@Preview
@Composable
private fun AppTopPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        ExpenseManagerTopAppBar(
            title = "Home",
            navigationIcon = null,
            actions = {
                IconButton(onClick = {}) {
                    Icon(imageVector = Icons.Outlined.Settings, contentDescription = null)
                }
            }
        )
        ExpenseManagerTopAppBar(
            title = "",
            navigationIcon = null,
            actions = {}
        )
    }
}