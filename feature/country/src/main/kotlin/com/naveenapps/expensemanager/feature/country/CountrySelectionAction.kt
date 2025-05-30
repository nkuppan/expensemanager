package com.naveenapps.expensemanager.feature.country

import com.naveenapps.expensemanager.core.model.Country

sealed class CountrySelectionAction {

    data class SelectCountry(val country: Country) : CountrySelectionAction()

    data object ClearText : CountrySelectionAction()

    data object ClosePage : CountrySelectionAction()
}