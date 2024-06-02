package com.naveenapps.expensemanager.core.data.dto

import com.google.gson.annotations.SerializedName

data class CountriesResponseDto(
    @SerializedName("countries")
    var counties: List<CountryResponseDto>,
)

data class CountryResponseDto(
    @SerializedName("name")
    var name: String? = null,
    @SerializedName("code")
    var countryCode: String? = null,
    @SerializedName("dial_code")
    var dialCode: String? = null,
    @SerializedName("continent")
    var continent: String? = null,
    @SerializedName("group")
    var countryGroup: String? = null,
    @SerializedName("states")
    var countryStates: String? = null,
    @SerializedName("currency_code")
    var currencyCode: String? = null,
    @SerializedName("currency")
    var currencyResponseDto: CurrencyResponseDto? = null,
)

data class CurrencyResponseDto(
    @SerializedName("symbol")
    var symbol: String? = null,
    @SerializedName("name")
    var name: String? = null,
    @SerializedName("symbol_native")
    var nativeSymbol: String? = null,
    @SerializedName("decimal_digits")
    var decimalDigits: Int = 0,
    @SerializedName("rounding")
    var rounding: Float = 0f,
    @SerializedName("name_plural")
    var namePlural: String? = null,
    @SerializedName("code")
    var code: String? = null,
)