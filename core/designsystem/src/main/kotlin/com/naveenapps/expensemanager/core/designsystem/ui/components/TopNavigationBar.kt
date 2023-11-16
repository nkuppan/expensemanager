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
import com.naveenapps.expensemanager.core.designsystem.R
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigationBar(
    onClick: () -> Unit,
    title: String?,
    disableBackIcon: Boolean = false
) {
    TopAppBar(
        navigationIcon = {
            if (disableBackIcon.not()) {
                NavigationButton(onClick)
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
    title: String,
    showDelete: Boolean?,
    onClick: (Int) -> Unit,
) {
    TopAppBar(navigationIcon = {
        NavigationButton(
            onClick = {
                onClick.invoke(1)
            },
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
            if (showDelete == true) {
                IconButton(onClick = {
                    onClick.invoke(2)
                }) {
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
                onClick = {},
                title = null,
            )
            TopNavigationBar(
                onClick = {},
                title = stringResource(id = R.string.page_name),
                disableBackIcon = true
            )
            TopNavigationBar(
                onClick = {},
                title = stringResource(id = R.string.page_name),
            )
            TopNavigationBarWithDeleteAction(
                title = stringResource(id = R.string.page_name),
                showDelete = null
            ) {

            }
            TopNavigationBarWithDeleteAction(
                title = stringResource(id = R.string.page_name),
                showDelete = true
            ) {

            }
        }
    }
}
