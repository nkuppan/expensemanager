package com.naveenapps.expensemanager.feature.country

import com.naveenapps.expensemanager.core.model.Country
import com.naveenapps.expensemanager.core.model.TextFieldValue

data class CountryState(
    val countries: List<Country>,
    val searchText: TextFieldValue<String>
)