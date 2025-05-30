package com.naveenapps.expensemanager.feature.country

import androidx.compose.runtime.Immutable
import com.naveenapps.expensemanager.core.model.Country
import com.naveenapps.expensemanager.core.model.TextFieldValue

@Immutable
data class CountryState(
    val countries: List<Country>,
    val searchText: TextFieldValue<String>,
    val showClearButton: Boolean
)