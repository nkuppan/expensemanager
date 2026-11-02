package com.naveenapps.expensemanager.core.domain.usecase.settings.currencyapi

import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.FormatAmountInCurrencyUseCase
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.ExchangeRateSource
import com.naveenapps.expensemanager.core.model.RateLookup
import com.naveenapps.expensemanager.core.model.RatePurpose
import com.naveenapps.expensemanager.core.repository.ExchangeRateRepository

/**
 * Converts amounts between currencies for views (dashboard, analysis, budgets) under the user's
 * exchange method: the provider's *current* rate, their own fixed rate, or not at all. Views
 * show the present state, so old entries are converted at the rate in force now; stored
 * transfers carry their own snapshot and never go through here.
 *
 * A missing rate is never papered over: [invoke] returns null and [total] reports the missing
 * pairs, so callers show "rate unavailable" instead of an unconverted number. With conversion
 * disabled, [total] keeps one sum per currency and never adds them together.
 */
class ConvertAmountUseCase(
    private val repository: ExchangeRateRepository,
    private val formatAmountInCurrencyUseCase: FormatAmountInCurrencyUseCase,
) {
    /** [amount] in [toCode], or null when it isn't converted (no rate, or conversion disabled). */
    suspend operator fun invoke(amount: Double, fromCode: String, toCode: String): Double? {
        require(fromCode.isNotBlank() && toCode.isNotBlank()) { "Currency codes must be set" }
        if (fromCode == toCode) return amount
        val lookup = repository.getRate(fromCode, toCode, RatePurpose.VIEW)
        return (lookup as? RateLookup.Available)?.let { amount * it.rate }
    }

    /**
     * Sums (amount, currency) pairs in [toCode]. Amounts are summed per currency first, so the
     * rate is looked up once per currency rather than once per transaction.
     */
    suspend fun total(amounts: List<Pair<Double, String>>, toCode: String): ConvertedTotal {
        val sumsByCurrency = LinkedHashMap<String, Double>()
        // The display currency leads when totals are listed per currency.
        sumsByCurrency[toCode] = 0.0
        amounts.forEach { (amount, fromCode) ->
            sumsByCurrency[fromCode] = (sumsByCurrency[fromCode] ?: 0.0) + amount
        }
        val hasDisplayCurrencyAmounts = amounts.any { it.second == toCode }
        if (!hasDisplayCurrencyAmounts && sumsByCurrency.size > 1) sumsByCurrency.remove(toCode)

        var sum = 0.0
        val missing = linkedSetOf<String>()
        var disabled = false
        var usedFixedRates = false
        sumsByCurrency.forEach { (fromCode, currencySum) ->
            if (fromCode == toCode) {
                sum += currencySum
                return@forEach
            }
            when (val lookup = repository.getRate(fromCode, toCode, RatePurpose.VIEW)) {
                is RateLookup.Available -> {
                    sum += currencySum * lookup.rate
                    if (lookup.source == ExchangeRateSource.STATIC) usedFixedRates = true
                }

                RateLookup.Unavailable -> missing += "$fromCode→$toCode"

                RateLookup.Disabled -> disabled = true
            }
        }
        val converted = !disabled && missing.isEmpty()
        val involvesOtherCurrencies = sumsByCurrency.keys.any { it != toCode }
        return ConvertedTotal(
            value = if (converted) sum else null,
            missingPairs = if (disabled) emptyList() else missing.toList(),
            // Only a total that was actually produced counts as converted.
            usedConversion = converted && involvesOtherCurrencies,
            usedFixedRates = converted && usedFixedRates,
            conversionDisabled = disabled,
            parts = sumsByCurrency.map { (code, currencySum) ->
                Amount(currencySum, currency = formatAmountInCurrencyUseCase.currencyFor(code))
            },
        )
    }
}

/**
 * A total in one currency. [value] is null when it couldn't be produced: a rate was missing
 * ([missingPairs] lists which), or conversion is disabled ([conversionDisabled]) and more than
 * one currency is involved. [parts] always holds the per-currency sums the total was built
 * from; with conversion disabled they are what gets shown, side by side and never added up.
 * [usedConversion] tells views to note that amounts were converted, [usedFixedRates] that the
 * user's own fixed rates did it.
 */
data class ConvertedTotal(
    val value: Double?,
    val missingPairs: List<String> = emptyList(),
    val usedConversion: Boolean = false,
    val usedFixedRates: Boolean = false,
    val conversionDisabled: Boolean = false,
    val parts: List<Amount> = emptyList(),
) {
    /** The same total scaled by [factor] (e.g. a per-day average); unavailable stays unavailable. */
    fun times(factor: Double): ConvertedTotal = copy(
        value = value?.times(factor),
        parts = parts.map { it.copy(amount = it.amount * factor) },
    )

    /**
     * The sign to colour this total by. With conversion off there is no single value, so it
     * follows the per-currency parts: negative only if every part is, positive only if every
     * part is, and like zero when they are mixed.
     */
    fun colorSign(): Double {
        value?.let { return it }
        val signs = parts.map { it.amount }.filter { it != 0.0 }
        return when {
            signs.isEmpty() -> 0.0
            signs.all { it < 0.0 } -> -1.0
            signs.all { it > 0.0 } -> 1.0
            else -> 0.0
        }
    }

    operator fun plus(other: ConvertedTotal): ConvertedTotal = combine(other, sign = 1.0)

    operator fun minus(other: ConvertedTotal): ConvertedTotal = combine(other, sign = -1.0)

    private fun combine(other: ConvertedTotal, sign: Double): ConvertedTotal {
        val first = value
        val second = other.value
        val combinedValue = if (first != null && second != null) first + sign * second else null
        val merged = LinkedHashMap<String, Amount>()
        parts.forEach { merged[it.currency?.code.orEmpty()] = it }
        other.parts.forEach { part ->
            val code = part.currency?.code.orEmpty()
            val existing = merged[code]
            merged[code] = existing?.copy(amount = existing.amount + sign * part.amount)
                ?: part.copy(amount = sign * part.amount)
        }
        return ConvertedTotal(
            value = combinedValue,
            missingPairs = (missingPairs + other.missingPairs).distinct(),
            usedConversion = combinedValue != null && (usedConversion || other.usedConversion),
            usedFixedRates = combinedValue != null && (usedFixedRates || other.usedFixedRates),
            conversionDisabled = conversionDisabled || other.conversionDisabled,
            parts = merged.values.toList(),
        )
    }
}

/** Adds totals up; any part that couldn't be produced makes the sum unavailable too. */
fun Iterable<ConvertedTotal>.sum(): ConvertedTotal = fold(ConvertedTotal(value = 0.0)) { total, next -> total + next }
