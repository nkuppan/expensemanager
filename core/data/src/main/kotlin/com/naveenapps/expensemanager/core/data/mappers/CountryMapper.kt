package com.naveenapps.expensemanager.core.data.mappers

import com.naveenapps.expensemanager.core.data.dto.CountryResponseDto
import com.naveenapps.expensemanager.core.data.dto.CurrencyResponseDto
import com.naveenapps.expensemanager.core.model.Country
import com.naveenapps.expensemanager.core.model.Currency


fun CountryResponseDto.toDomainModel(): Country? {
    val currency = currencyResponseDto?.toDomainModel()

    currency ?: return null

    return Country(
        name = name ?: "",
        countryCode = countryCode ?: "",
        currencyCode = currencyCode ?: "",
        currency = currency
    )
}

fun CurrencyResponseDto.toDomainModel(): Currency {
    return Currency(
        name = name ?: "",
        symbol = symbol ?: ""
    )
}
