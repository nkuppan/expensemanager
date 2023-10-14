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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.common.ui.theme.widget.AppDatePickerDialog
import com.nkuppan.expensemanager.common.ui.theme.widget.ClickableTextField
import com.nkuppan.expensemanager.data.utils.toTransactionDate


enum class DateTypeSelection {
    FROM_DATE,
    TO_DATE
}


@Composable
fun DateFilterView(
    complete: () -> Unit
) {

    val focusManager = LocalFocusManager.current

    val viewModel: DateFilterViewModel = hiltViewModel()
    val selectedFilterType by viewModel.filterType.collectAsState()
    val showCustomRangeSelection by viewModel.showCustomRangeSelection.collectAsState()
    val filterTypes by viewModel.filterTypes.collectAsState()

    val fromDate by viewModel.fromDate.collectAsState()
    val toDate by viewModel.toDate.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }

    var dateTypeSelection by remember { mutableStateOf(DateTypeSelection.FROM_DATE) }

    if (showDatePicker) {
        AppDatePickerDialog(
            modifier = Modifier
                .wrapContentSize()
                .padding(16.dp)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(8.dp)
                ),
            selectedDate = if (dateTypeSelection == DateTypeSelection.FROM_DATE) {
                fromDate
            } else {
                toDate
            },
            onDateSelected = { date ->
                showDatePicker = false
                if (dateTypeSelection == DateTypeSelection.FROM_DATE) {
                    viewModel.setFromDate(date)
                } else {
                    viewModel.setToDate(date)
                }
            },
            onDismiss = {
                showDatePicker = false
            },
        )
    }


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
                if (showCustomRangeSelection) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(top = 8.dp, start = 16.dp, bottom = 8.dp)
                                .fillMaxWidth()
                        ) {
                            ClickableTextField(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                value = fromDate.toTransactionDate(),
                                label = R.string.from_date,
                                leadingIcon = R.drawable.ic_calendar,
                                onClick = {
                                    focusManager.clearFocus()
                                    dateTypeSelection = DateTypeSelection.FROM_DATE
                                    showDatePicker = true
                                }
                            )
                            ClickableTextField(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp),
                                value = toDate.toTransactionDate(),
                                label = R.string.to_date,
                                leadingIcon = R.drawable.ic_calendar,
                                onClick = {
                                    focusManager.clearFocus()
                                    dateTypeSelection = DateTypeSelection.TO_DATE
                                    showDatePicker = true
                                }
                            )
                        }
                    }
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.End)
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