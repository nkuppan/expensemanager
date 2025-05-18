package com.naveenapps.expensemanager.feature.filter.datefilter

import java.util.Date

sealed class DateFilterAction {

    data object Save : DateFilterAction()

    data object ShowFromDateSelection : DateFilterAction()

    data object ShowToDateSelection : DateFilterAction()

    data class SaveFromDate(val date:Date) : DateFilterAction()

    data class SaveToDate(val date:Date) : DateFilterAction()

    data object DismissDateSelection : DateFilterAction()
}