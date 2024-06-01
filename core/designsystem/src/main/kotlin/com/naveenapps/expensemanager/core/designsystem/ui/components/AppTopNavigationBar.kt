package com.naveenapps.expensemanager.core.designsystem.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopNavigationBar(
    title: String?,
    navigationIcon: ImageVector?,
    navigationBackClick: (() -> Unit) = {},
    actions: @Composable (RowScope.() -> Unit) = {},
    elevation: Dp = 0.dp,
    titleClick: (() -> Unit)? = null
) {
    Surface(shadowElevation = elevation) {
        TopAppBar(
            navigationIcon = if (navigationIcon != null) {
                {
                    IconButton(onClick = navigationBackClick) {
                        Icon(imageVector = navigationIcon, contentDescription = null)
                    }
                }
            } else {
                {}
            },
            title = {
                if (title?.isNotEmpty() == true) {
                    Text(
                        modifier = Modifier.then(
                            if (titleClick != null) {
                                Modifier.clickable {
                                    titleClick.invoke()
                                }
                            } else {
                                Modifier
                            }
                        ),
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            actions = actions
        )
    }
}

@Preview
@Composable
private fun AppTopPreview() {
    ExpenseManagerTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppTopNavigationBar(
                title = "Home",
                navigationIcon = null,
                actions = {
                    IconButton(onClick = {}) {
                        Icon(imageVector = Icons.Outlined.Settings, contentDescription = null)
                    }
                }
            )
            AppTopNavigationBar(
                title = "",
                navigationIcon = null,
                actions = {}
            )
        }
    }
}