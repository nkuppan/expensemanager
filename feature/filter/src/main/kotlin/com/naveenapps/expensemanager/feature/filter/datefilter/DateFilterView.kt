package com.naveenapps.expensemanager.feature.filter.datefilter

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
import com.naveenapps.expensemanager.core.designsystem.utils.ObserveAsEvents
import com.naveenapps.expensemanager.core.model.DateRangeModel
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.TextFieldValue
import com.naveenapps.expensemanager.feature.filter.R
import java.util.Date

@Composable
fun DateFilterSelectionView(
    onComplete: () -> Unit,
    viewModel: DateFilterViewModel = hiltViewModel()
) {

    ObserveAsEvents(viewModel.event) {
        when (it) {
            DateFilterEvent.Saved -> onComplete.invoke()
        }
    }

    val state by viewModel.state.collectAsState()

    if (state.showDateFilter) {
        AppDatePickerDialog(
            selectedDate = if (state.dateFilterType == DateFilterType.FROM_DATE) {
                state.fromDate.value
            } else {
                state.toDate.value
            },
            onDateSelected = { date ->
                if (state.dateFilterType == DateFilterType.FROM_DATE) {
                    viewModel.processAction(DateFilterAction.SaveFromDate(date))
                } else {
                    viewModel.processAction(DateFilterAction.SaveToDate(date))
                }
            },
        ) {
            viewModel.processAction(DateFilterAction.DismissDateSelection)
        }
    }

    FilterTypesAndViewContent(
        state = state,
        onAction = viewModel::processAction,
        modifier = Modifier
            .wrapContentSize()
            .padding(16.dp)
    )
}

@Composable
private fun FilterTypesAndViewContent(
    state: DateFilterState,
    onAction: (DateFilterAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        item {
            Text(
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                text = stringResource(id = R.string.date_filter).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
            )
        }
        items(state.dateRangeTypeList) { filter ->
            val isSelected = state.dateRangeType.value == filter.type
            Row(
                modifier = Modifier
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
                    .clickable {
                        state.dateRangeType.onValueChange?.invoke(filter.type)
                    }
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
            if (state.showCustomRangeSelection) {
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
                            value = state.fromDate.value.toCompleteDateWithDate(),
                            label = R.string.from_date,
                            leadingIcon = Icons.Default.EditCalendar,
                            onClick = {
                                onAction.invoke(DateFilterAction.ShowFromDateSelection)
                            },
                        )
                        ClickableTextField(
                            modifier = Modifier.weight(1f),
                            value = state.toDate.value.toCompleteDateWithDate(),
                            label = R.string.to_date,
                            leadingIcon = Icons.Default.EditCalendar,
                            onClick = {
                                onAction.invoke(DateFilterAction.ShowToDateSelection)
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
                    TextButton(onClick = { onAction.invoke(DateFilterAction.Save) }) {
                        Text(text = stringResource(id = R.string.cancel).uppercase())
                    }
                    TextButton(onClick = { onAction.invoke(DateFilterAction.Save) }) {
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
        FilterTypesAndViewContent(
            state = DateFilterState(
                dateFilterType = DateFilterType.FROM_DATE,
                showCustomRangeSelection = false,
                showDateFilter = false,
                dateRangeTypeList = DateRangeType.entries.map {
                    DateRangeModel(
                        name = it.toCapitalize(),
                        description = "Sample",
                        type = it,
                        listOf(Date().time, Date().time)
                    )
                },
                dateRangeType = dateRange,
                fromDate = dateFilter,
                toDate = dateFilter
            ),
            onAction = {}
        )
    }
}

