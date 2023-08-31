package com.nkuppan.expensemanager.feature.category.create

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nkuppan.expensemanager.feature.category.R

@Composable
fun CategoryCreateScreen(
    modifier: Modifier = Modifier
) {

    Column(modifier = modifier) {
        OutlinedTextField(
            modifier = Modifier
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
            value = "",
            label = {
                    Text(text = stringResource(id = R.string.name))
            },
            onValueChange = {

            }
        )
        OutlinedTextField(
            modifier = Modifier
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
            value = "",
            onValueChange = {

            }
        )
        OutlinedTextField(
            modifier = Modifier
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
            value = "",
            onValueChange = {

            }
        )
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