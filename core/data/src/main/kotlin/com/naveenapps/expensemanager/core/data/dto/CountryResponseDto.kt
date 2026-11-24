package com.naveenapps.expensemanager.core.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CountriesResponseDto(
    @SerialName("countries")
    var counties: List<CountryResponseDto>,
)

@Serializable
data class CountryResponseDto(
    @SerialName("name")
    var name: String? = null,
    @SerialName("code")
    var countryCode: String? = null,
    @SerialName("dial_code")
    var dialCode: String? = null,
    @SerialName("continent")
    var continent: String? = null,
    @SerialName("group")
    var countryGroup: String? = null,
    @SerialName("states")
    var countryStates: String? = null,
    @SerialName("currency_code")
    var currencyCode: String? = null,
    @SerialName("currency")
    var currencyResponseDto: CurrencyResponseDto? = null,
)

@Serializable
data class CurrencyResponseDto(
    @SerialName("symbol")
    var symbol: String? = null,
    @SerialName("name")
    var name: String? = null,
    @SerialName("symbol_native")
    var nativeSymbol: String? = null,
    @SerialName("decimal_digits")
    var decimalDigits: Int = 0,
    @SerialName("rounding")
    var rounding: Float = 0f,
    @SerialName("name_plural")
    var namePlural: String? = null,
    @SerialName("code")
    var code: String? = null,
)
