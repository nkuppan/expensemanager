package com.nkuppan.expensemanager.presentation.settings.datefilter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.nkuppan.expensemanager.R


@Composable
fun DateFilterView(
    complete: () -> Unit
) {

    val viewModel: DateFilterViewModel = hiltViewModel()
    val selectedFilterType by viewModel.filterType.collectAsState()
    val filterTypes by viewModel.filterTypes.collectAsState()
    Dialog(
        onDismissRequest = complete,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
    ) {
        LazyColumn(
            modifier = Modifier
                .wrapContentSize()
                .padding(16.dp)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            item {
                Text(
                    modifier = Modifier.padding(top = 16.dp, start = 16.dp, bottom = 8.dp),
                    text = stringResource(id = R.string.date_filter).uppercase(),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            items(filterTypes) { filter ->
                val isSelectedCurrency = selectedFilterType == filter.filterType
                Row(
                    modifier = Modifier
                        .clickable {
                            viewModel.setFilterType(filter.filterType)
                        }
                        .fillMaxWidth()
                        .then(
                            if (isSelectedCurrency) {
                                Modifier
                                    .padding(4.dp)
                                    .background(
                                        color = colorResource(id = R.color.green_100),
                                        shape = RoundedCornerShape(size = 12.dp)
                                    )
                            } else {
                                Modifier
                                    .padding(4.dp)
                            }
                        )
                        .padding(12.dp),
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = filter.name
                    )
                    if (isSelectedCurrency) {
                        Icon(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            painter = painterResource(id = R.drawable.ic_done),
                            contentDescription = null
                        )
                    }
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        TextButton(onClick = complete) {
                            Text(text = stringResource(id = R.string.cancel).uppercase())
                        }
                        TextButton(onClick = {
                            viewModel.save()
                            complete.invoke()
                        }) {
                            Text(text = stringResource(id = R.string.select).uppercase())
                        }
                    }
                }
            }
        }
    }
}