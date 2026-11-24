package com.naveenapps.expensemanager.core.model

import androidx.compose.runtime.Stable

@Stable
data class Country(
    var name: String,
    var countryCode: String,
    var currencyCode: String,
    var currency: Currency,
)
