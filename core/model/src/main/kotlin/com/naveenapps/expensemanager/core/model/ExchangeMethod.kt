package com.naveenapps.expensemanager.core.model

/** Where every conversion rate in the app comes from. Persisted by name. */
enum class ExchangeMethod {
    /** Rates are fetched from the configured exchange-rate API. */
    API,

    /** Rates are the user's own fixed buy/sell values, one record per currency pair. */
    STATIC,

    /** Nothing is converted: totals stay per currency and are never added together. */
    DISABLED,
}

/**
 * The user's fixed rate for one currency pair, valid for all dates until they change it:
 * 1 [baseCode] is bought for [buy] and sold for [sell] [quoteCode]. A pair has one record,
 * whichever way round it was entered (USD/SYP and SYP/USD are the same pair).
 */
data class StaticExchangeRate(
    val baseCode: String,
    val quoteCode: String,
    val buy: Double,
    val sell: Double,
    val updatedOn: Long = 0L,
) {
    val mid: Double get() = (buy + sell) / 2

    fun isSamePair(firstCode: String, secondCode: String): Boolean = (baseCode == firstCode && quoteCode == secondCode) ||
        (baseCode == secondCode && quoteCode == firstCode)
}

/** What a rate is needed for; a static pair answers each with a different value. */
enum class RatePurpose {
    /** Totals, charts and budgets: the midpoint of buy and sell. */
    VIEW,

    /** Money actually changing currency: buy when handing over the base, sell when buying it. */
    TRANSFER,
}

/** The answer to "what is the rate from A to B" under the current [ExchangeMethod]. */
sealed interface RateLookup {
    /** 1 from-currency = [rate] to-currency. [provider] is set for fetched rates only. */
    data class Available(
        val rate: Double,
        val source: ExchangeRateSource,
        val provider: String? = null,
        val capturedAt: Long = 0L,
    ) : RateLookup

    /** A rate was expected but there is none (offline, pair not offered, pair not entered). */
    data object Unavailable : RateLookup

    /** The exchange method is [ExchangeMethod.DISABLED]: no conversion is to happen. */
    data object Disabled : RateLookup
}
