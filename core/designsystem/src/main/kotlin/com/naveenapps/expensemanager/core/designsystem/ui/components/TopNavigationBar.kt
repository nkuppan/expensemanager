package com.naveenapps.expensemanager.core.designsystem.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.naveenapps.expensemanager.core.designsystem.R
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigationBar(
    navController: NavController,
    title: String?,
    disableBackIcon: Boolean = false
) {
    TopAppBar(
        navigationIcon = {
            if (disableBackIcon.not()) {
                NavigationButton(navController)
            }
        },
        title = {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    )
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TopNavigationBarWithDeleteAction(
    navController: NavController,
    title: String,
    actionId: String?,
    onClick: () -> Unit,
) {
    TopAppBar(navigationIcon = {
        NavigationButton(
            navController,
            navigationIcon = Icons.Default.Close
        )
    }, title = {
        Row {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically),
                text = title
            )
            if (actionId?.isNotBlank() == true) {
                IconButton(onClick = onClick) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = ""
                    )
                }
            }
        }
    })
}

@Preview
@Composable
private fun TopNavigationBarWithDeleteAction() {
    ExpenseManagerTheme {
        Column {
            TopNavigationBar(
                navController = rememberNavController(),
                title = null,
            )
            TopNavigationBar(
                navController = rememberNavController(),
                title = stringResource(id = R.string.page_name),
                disableBackIcon = true
            )
            TopNavigationBar(
                navController = rememberNavController(),
                title = stringResource(id = R.string.page_name),
            )
            TopNavigationBarWithDeleteAction(
                navController = rememberNavController(),
                title = stringResource(id = R.string.page_name),
                actionId = null
            ) {

            }
            TopNavigationBarWithDeleteAction(
                navController = rememberNavController(),
                title = stringResource(id = R.string.page_name),
                actionId = "null"
            ) {

            }
        }
    }
}
