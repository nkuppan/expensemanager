package com.naveenapps.expensemanager.feature.filter.datefilter

import kotlin.time.Instant

sealed class DateFilterAction {

    data object Save : DateFilterAction()

    data object Cancel : DateFilterAction()

    data object ShowFromDateSelection : DateFilterAction()

    data object ShowToDateSelection : DateFilterAction()

    data class SaveFromDate(val date: Instant) : DateFilterAction()

    data class SaveToDate(val date: Instant) : DateFilterAction()

    data object DismissDateSelection : DateFilterAction()
}
