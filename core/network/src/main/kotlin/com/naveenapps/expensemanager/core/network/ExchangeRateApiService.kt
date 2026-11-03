package com.naveenapps.expensemanager.core.network

import kotlinx.serialization.json.JsonObject
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.QueryMap
import retrofit2.http.Url

/**
 * A single Retrofit interface reused across every user-configured provider: the full request
 * URL, headers and query params are built per [com.naveenapps.expensemanager.core.model.CurrencyApiPreset]
 * (see [RatesResponseParser]) rather than fixed at the interface level, since the base URL and
 * auth style are user input, not known at compile time.
 */
internal interface ExchangeRateApiService {

    @GET
    suspend fun getLatestRates(
        @Url url: String,
        @HeaderMap headers: Map<String, String>,
        @QueryMap query: Map<String, String>,
    ): Response<JsonObject>
}
