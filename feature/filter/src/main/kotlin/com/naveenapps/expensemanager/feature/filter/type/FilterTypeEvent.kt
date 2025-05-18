package com.naveenapps.expensemanager.feature.filter.type

sealed class FilterTypeEvent {

    data object Saved : FilterTypeEvent()
}