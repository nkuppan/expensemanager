package com.naveenapps.expensemanager.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.naveenapps.expensemanager.R
import com.naveenapps.expensemanager.ui.theme.ExpenseManagerTheme


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
            navigationIcon = R.drawable.ic_close
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
                        painter = painterResource(id = R.drawable.ic_delete),
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
                title = stringResource(id = R.string.account),
                disableBackIcon = true
            )
            TopNavigationBar(
                navController = rememberNavController(),
                title = stringResource(id = R.string.account),
            )
            TopNavigationBarWithDeleteAction(
                navController = rememberNavController(),
                title = stringResource(id = R.string.account),
                actionId = null
            ) {

            }
            TopNavigationBarWithDeleteAction(
                navController = rememberNavController(),
                title = stringResource(id = R.string.account),
                actionId = "null"
            ) {

            }
        }
    }
}
