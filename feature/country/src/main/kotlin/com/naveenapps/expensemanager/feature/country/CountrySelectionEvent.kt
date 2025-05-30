package com.naveenapps.expensemanager.feature.country

import com.naveenapps.expensemanager.core.model.Country

public sealed class CountrySelectionEvent {

    data class CountrySelected(val country: Country) : CountrySelectionEvent()

    data object Dismiss : CountrySelectionEvent()
}