package com.naveenapps.expensemanager.core.model

data class Country(
    var name: String,
    var countryCode: String,
    var currencyCode: String,
    var currency: Currency?,
)
