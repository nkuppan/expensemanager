package com.naveenapps.expensemanager.core.model

/**
 * A rate from the configured provider: 1 [baseCode] = [rate] [targetCode], fetched from
 * [provider] at [fetchedAt] (epoch millis). This is the temporary, cached value; when a
 * transaction uses it, it is copied into an [ExchangeRateSnapshot].
 */
data class ExchangeRate(
    val provider: String,
    val baseCode: String,
    val targetCode: String,
    val rate: Double,
    val fetchedAt: Long,
)
