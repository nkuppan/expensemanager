package com.naveenapps.expensemanager.feature.country

import com.naveenapps.expensemanager.core.model.Country

sealed class CountrySelectionEvent {

    data class CountrySelected(val country: Country) : CountrySelectionEvent()

    data object Dismiss : CountrySelectionEvent()
}