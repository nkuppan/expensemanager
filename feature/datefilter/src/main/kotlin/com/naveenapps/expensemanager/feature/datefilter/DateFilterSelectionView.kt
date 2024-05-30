package com.naveenapps.expensemanager.feature.datefilter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.EditCalendar
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naveenapps.expensemanager.core.common.utils.toCapitalize
import com.naveenapps.expensemanager.core.common.utils.toCompleteDateWithDate
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppDatePickerDialog
import com.naveenapps.expensemanager.core.designsystem.ui.components.ClickableTextField
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.designsystem.ui.utils.getSelectedBGColor
import com.naveenapps.expensemanager.core.model.DateRangeModel
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.TextFieldValue
import java.util.Date

enum class DateTypeSelection {
    FROM_DATE,
    TO_DATE,
}

@Composable
fun DateFilterSelectionView(
    onComplete: () -> Unit,
    viewModel: DateFilterViewModel = hiltViewModel()
) {

    var showDatePicker by remember { mutableStateOf(false) }
    var dateTypeSelection by remember { mutableStateOf(DateTypeSelection.FROM_DATE) }

    val fromDate by viewModel.fromDate.collectAsState()
    val toDate by viewModel.toDate.collectAsState()
    val dateRangeFilterTypes by viewModel.dateRangeFilterTypes.collectAsState()
    val dateRangeType by viewModel.dateRangeType.collectAsState()
    val showCustomRangeSelection by viewModel.showCustomRangeSelection.collectAsState()

    if (showDatePicker) {
        AppDatePickerDialog(
            selectedDate = if (dateTypeSelection == DateTypeSelection.FROM_DATE) {
                fromDate.value
            } else {
                toDate.value
            },
            onDateSelected = { date ->
                showDatePicker = false
                if (dateTypeSelection == DateTypeSelection.FROM_DATE) {
                    viewModel.setFromDate(date)
                } else {
                    viewModel.setToDate(date)
                }
            },
        ) {
            showDatePicker = false
        }
    }

    FilterTypesAndView(
        filterTypes = dateRangeFilterTypes,
        selectedFilterType = dateRangeType,
        fromDate = fromDate,
        toDate = toDate,
        showCustomRangeSelection = showCustomRangeSelection,
        complete = {
            viewModel.save()
            onComplete.invoke()
        },
        onDateSelection = {
            dateTypeSelection = it
            showDatePicker = true
        },
        modifier = Modifier
            .wrapContentSize()
            .padding(16.dp)
    )
}

@Composable
private fun FilterTypesAndView(
    filterTypes: List<DateRangeModel>,
    selectedFilterType: TextFieldValue<DateRangeType>,
    fromDate: TextFieldValue<Date>,
    toDate: TextFieldValue<Date>,
    showCustomRangeSelection: Boolean,
    complete: () -> Unit,
    onDateSelection: (DateTypeSelection) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        item {
            Text(
                modifier = Modifier.padding(top = 12.dp, start = 16.dp, bottom = 8.dp),
                text = stringResource(id = R.string.date_filter).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
            )
        }
        items(filterTypes) { filter ->
            val isSelected = selectedFilterType.value == filter.type
            Row(
                modifier = Modifier
                    .clickable {
                        selectedFilterType.onValueChange?.invoke(filter.type)
                    }
                    .fillMaxWidth()
                    .then(
                        if (isSelected) {
                            Modifier
                                .padding(4.dp)
                                .background(
                                    color = getSelectedBGColor(),
                                    shape = RoundedCornerShape(size = 12.dp),
                                )
                        } else {
                            Modifier
                                .padding(4.dp)
                        },
                    )
                    .padding(12.dp),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                ) {
                    Text(
                        text = filter.name,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    if (filter.description.isNotBlank()) {
                        Text(
                            text = filter.description,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                if (isSelected) {
                    Icon(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        imageVector = Icons.Default.Done,
                        contentDescription = null,
                    )
                }
            }
        }
        item {
            if (showCustomRangeSelection) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .padding(top = 8.dp, start = 16.dp, bottom = 8.dp, end = 16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ClickableTextField(
                            modifier = Modifier.weight(1f),
                            value = fromDate.value.toCompleteDateWithDate(),
                            label = R.string.from_date,
                            leadingIcon = Icons.Default.EditCalendar,
                            onClick = {
                                onDateSelection.invoke(DateTypeSelection.FROM_DATE)
                            },
                        )
                        ClickableTextField(
                            modifier = Modifier.weight(1f),
                            value = toDate.value.toCompleteDateWithDate(),
                            label = R.string.to_date,
                            leadingIcon = Icons.Default.EditCalendar,
                            onClick = {
                                onDateSelection.invoke(DateTypeSelection.TO_DATE)
                            },
                        )
                    }
                }
            }
        }
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = modifier.align(Alignment.End),
                ) {
                    TextButton(onClick = complete) {
                        Text(text = stringResource(id = R.string.cancel).uppercase())
                    }
                    TextButton(onClick = { complete.invoke() }) {
                        Text(text = stringResource(id = R.string.select).uppercase())
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun FilterNormalViewPreview() {
    ExpenseManagerTheme {
        val dateRange = TextFieldValue(DateRangeType.THIS_MONTH, false, {})
        val dateFilter = TextFieldValue(Date(), false, {})
        FilterTypesAndView(
            filterTypes = DateRangeType.entries.map {
                DateRangeModel(
                    name = it.toCapitalize(),
                    description = "Sample",
                    type = it,
                    listOf(Date().time, Date().time)
                )
            },
            selectedFilterType = dateRange,
            fromDate = dateFilter,
            toDate = dateFilter,
            showCustomRangeSelection = true,
            complete = { },
            onDateSelection = {}
        )
    }
}

