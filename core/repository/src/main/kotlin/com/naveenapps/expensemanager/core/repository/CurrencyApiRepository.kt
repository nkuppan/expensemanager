package com.naveenapps.expensemanager.core.repository

import com.naveenapps.expensemanager.core.model.CurrencyApiProfile
import com.naveenapps.expensemanager.core.model.ExchangeRate
import kotlinx.coroutines.flow.Flow

interface CurrencyApiRepository {

    fun getProfile(): Flow<CurrencyApiProfile?>

    suspend fun saveProfile(profile: CurrencyApiProfile)

    /**
     * Fetches latest rates from [baseCurrencyCode] using the saved profile. Returns failure when
     * no profile is configured, or the request/parse fails — callers fall back to cached rates.
     */
    suspend fun fetchLatestRates(baseCurrencyCode: String): Result<Map<String, Double>>

    /**
     * The current rate from [fromCode] to [toCode] from the configured provider: a fresh cached
     * value if there is one, otherwise refreshed from the API, otherwise (refresh failed) the
     * last cached value. Null when no rate is available: no profile, never fetched while
     * offline, or the provider doesn't offer the pair.
     */
    suspend fun getRate(fromCode: String, toCode: String): ExchangeRate?
}
