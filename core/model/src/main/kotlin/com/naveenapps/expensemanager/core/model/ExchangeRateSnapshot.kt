package com.naveenapps.expensemanager.core.model

/** Where a stored exchange rate came from. Persisted by ordinal: only append new values. */
enum class ExchangeRateSource {
    /** Copied from the configured provider's fetched rate. */
    FETCHED,

    /** Entered by the user (e.g. the rate their exchange office actually gave them). */
    USER,

    /** Copied from the user's fixed rate for the pair ([StaticExchangeRate]). */
    STATIC,
}

/**
 * The exact rate a cross-currency transaction used: 1 [baseCode] = [rate] [quoteCode].
 * Same shape whether fetched or user-entered; [provider] is set for fetched rates only.
 */
data class ExchangeRateSnapshot(
    val id: String,
    val source: ExchangeRateSource,
    val provider: String?,
    val baseCode: String,
    val quoteCode: String,
    val rate: Double,
    val capturedAt: Long,
)
