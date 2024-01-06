package com.naveenapps.expensemanager.core.designsystem.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.naveenapps.expensemanager.core.designsystem.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.R
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigationBar(
    onClick: () -> Unit,
    title: String?,
    disableBackIcon: Boolean = false,
    navigationIcon: ImageVector = Icons.Filled.ArrowBack,
) {
    TopAppBar(
        navigationIcon = {
            if (disableBackIcon.not()) {
                NavigationButton(onClick, navigationIcon)
            }
        },
        title = {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        },
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TopNavigationBarWithDeleteAction(
    title: String,
    isDeleteEnabled: Boolean?,
    onNavigationIconClick: () -> Unit,
    onDeleteActionClick: () -> Unit,
) {
    TopAppBar(
        navigationIcon = {
            NavigationButton(
                onClick = onNavigationIconClick,
                navigationIcon = Icons.Default.Close,
            )
        },
        title = {
            Text(text = title)
        },
        actions = {
            if (isDeleteEnabled == true) {
                IconButton(onClick = onDeleteActionClick) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "",
                    )
                }
            }
        }
    )
}

@AppPreviewsLightAndDarkMode
@Composable
private fun TopNavigationBarWithDeleteActionPreview() {
    ExpenseManagerTheme {
        Column {
            TopNavigationBar(
                onClick = {},
                title = null,
            )
            TopNavigationBar(
                onClick = {},
                title = stringResource(id = R.string.page_name),
                disableBackIcon = true,
            )
            TopNavigationBar(
                onClick = {},
                title = stringResource(id = R.string.page_name),
            )
            TopNavigationBarWithDeleteAction(
                title = stringResource(id = R.string.page_name),
                isDeleteEnabled = null,
                onNavigationIconClick = {},
                onDeleteActionClick = {}
            )
            TopNavigationBarWithDeleteAction(
                title = stringResource(id = R.string.page_name),
                isDeleteEnabled = true,
                onNavigationIconClick = {},
                onDeleteActionClick = {}
            )
        }
    }
}
