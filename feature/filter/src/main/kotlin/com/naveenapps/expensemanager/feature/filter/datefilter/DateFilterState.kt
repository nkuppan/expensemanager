package com.naveenapps.expensemanager.feature.filter.datefilter

import com.naveenapps.expensemanager.core.model.DateRangeModel
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.TextFieldValue
import java.util.Date

data class DateFilterState(
    val dateRangeType: TextFieldValue<DateRangeType>,
    val fromDate: TextFieldValue<Date>,
    val toDate: TextFieldValue<Date>,
    val dateRangeTypeList: List<DateRangeModel>,
    val showCustomRangeSelection: Boolean,
    val showDateFilter: Boolean,
    val dateFilterType: DateFilterType
)