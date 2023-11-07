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
import androidx.compose.material3.Surface
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.data.utils.toCompleteDate
import com.nkuppan.expensemanager.domain.model.DateRangeModel
import com.nkuppan.expensemanager.domain.model.DateRangeType
import com.nkuppan.expensemanager.ui.components.AppDatePickerDialog
import com.nkuppan.expensemanager.ui.components.ClickableTextField
import java.util.Date


enum class DateTypeSelection {
    FROM_DATE,
    TO_DATE
}


@Composable
fun DateFilterSelectionView(complete: () -> Unit) {

    val viewModel: DateFilterViewModel = hiltViewModel()
    val selectedFilterType by viewModel.dateRangeFilterType.collectAsState()
    val showCustomRangeSelection by viewModel.showCustomRangeSelection.collectAsState()
    val filterTypes by viewModel.dateRangeFilterTypes.collectAsState(emptyList())

    val fromDate by viewModel.fromDate.collectAsState()
    val toDate by viewModel.toDate.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }

    var dateTypeSelection by remember { mutableStateOf(DateTypeSelection.FROM_DATE) }

    if (showDatePicker) {
        AppDatePickerDialog(
            modifier = Modifier
                .wrapContentSize()
                .padding(16.dp),
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

    FilterTypesAndView(
        filterTypes,
        selectedFilterType,
        viewModel,
        showCustomRangeSelection,
        fromDate,
        toDate,
        complete
    ) {
        dateTypeSelection = it
        showDatePicker = true
    }
}

@Composable
private fun FilterTypesAndView(
    filterTypes: List<DateRangeModel>,
    selectedFilterType: DateRangeType,
    viewModel: DateFilterViewModel,
    showCustomRangeSelection: Boolean,
    fromDate: Date,
    toDate: Date,
    complete: () -> Unit,
    onDateSelection: (DateTypeSelection) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    Surface(
        modifier = Modifier.padding(16.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        LazyColumn(
            modifier = Modifier.wrapContentSize()
        ) {
            item {
                Text(
                    modifier = Modifier.padding(top = 12.dp, start = 12.dp, bottom = 8.dp),
                    text = stringResource(id = R.string.date_filter).uppercase(),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            items(filterTypes) { filter ->
                val isSelected = selectedFilterType == filter.type
                Row(
                    modifier = Modifier
                        .clickable {
                            viewModel.setFilterType(filter.type)
                        }
                        .fillMaxWidth()
                        .then(
                            if (isSelected) {
                                Modifier
                                    .padding(4.dp)
                                    .background(
                                        color = colorResource(id = R.color.green_500).copy(alpha = .1f),
                                        shape = RoundedCornerShape(size = 12.dp)
                                    )
                            } else {
                                Modifier
                                    .padding(4.dp)
                            }
                        )
                        .padding(8.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                    ) {
                        Text(
                            text = filter.name,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (filter.description.isNotBlank()) {
                            Text(
                                text = filter.description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    if (isSelected) {
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
                                .padding(top = 8.dp, start = 16.dp, bottom = 8.dp, end = 16.dp)
                                .fillMaxWidth()
                        ) {
                            ClickableTextField(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                value = fromDate.toCompleteDate(),
                                label = R.string.from_date,
                                leadingIcon = R.drawable.ic_calendar,
                                onClick = {
                                    focusManager.clearFocus()
                                    onDateSelection.invoke(DateTypeSelection.FROM_DATE)
                                }
                            )
                            ClickableTextField(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp),
                                value = toDate.toCompleteDate(),
                                label = R.string.to_date,
                                leadingIcon = R.drawable.ic_calendar,
                                onClick = {
                                    focusManager.clearFocus()
                                    onDateSelection.invoke(DateTypeSelection.TO_DATE)
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