package com.nkuppan.expensemanager.feature.category.create

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nkuppan.expensemanager.feature.category.R


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CategoryCreateScreen(
    navController: NavController
) {
    val viewModel: CategoryCreateViewModel = hiltViewModel()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    Icon(
                        painter = painterResource(id = com.nkuppan.expensemanager.core.ui.R.drawable.ic_arrow_back),
                        contentDescription = ""
                    )
                },
                title = {
                    Text(text = stringResource(R.string.category))
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {

            }) {
                Image(
                    painter = painterResource(id = com.nkuppan.expensemanager.core.ui.R.drawable.ic_done),
                    contentDescription = ""
                )
            }
        }
    ) {
        CategoryCreateScreen(
            modifier = Modifier.padding(top = it.calculateTopPadding())
        )
    }
}

@Composable
fun CategoryCreateScreen(
    modifier: Modifier = Modifier
) {
    var checked by remember { mutableStateOf(true) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
        ) {
            FilledTonalButton(
                onClick = {

                },
                shape = ButtonDefaults.filledTonalShape
            ) {
                Icon(
                    painter = painterResource(id = com.nkuppan.expensemanager.core.ui.R.drawable.ic_arrow_downward),
                    contentDescription = ""
                )
                Text(
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .align(Alignment.CenterVertically),
                    text = stringResource(id = R.string.income)
                )
            }
            FilledTonalButton(
                modifier = Modifier.padding(start = 8.dp),
                onClick = {

                },
                shape = ButtonDefaults.shape
            ) {
                Icon(
                    painter = painterResource(id = com.nkuppan.expensemanager.core.ui.R.drawable.ic_arrow_upward),
                    contentDescription = ""
                )
                Text(
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .align(Alignment.CenterVertically),
                    text = stringResource(id = R.string.expense)
                )
            }
        }
        OutlinedTextField(
            modifier = Modifier
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
            value = "",
            label = {
                Text(text = stringResource(id = R.string.category_name))
            },
            onValueChange = {

            }
        )

        Row(
            modifier = Modifier
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth()
                .clickable {
                    checked = !checked
                },
        ) {

            Text(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f),
                text = stringResource(id = R.string.favorite_category)
            )

            Switch(
                modifier = Modifier
                    .semantics { contentDescription = "Demo with icon" },
                checked = checked,
                onCheckedChange = { checked = it },
                thumbContent = if (checked) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                } else {
                    null
                }
            )
        }
    }
}

@Preview
@Composable
fun CategoryCreateStatePreview() {
    MaterialTheme {
        CategoryCreateScreen(
            modifier = Modifier.fillMaxSize()
        )
    }
}