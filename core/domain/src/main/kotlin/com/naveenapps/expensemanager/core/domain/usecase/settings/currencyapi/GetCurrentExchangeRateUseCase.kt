package com.naveenapps.expensemanager.core.domain.usecase.settings.currencyapi

import com.naveenapps.expensemanager.core.model.ExchangeMethod
import com.naveenapps.expensemanager.core.model.RateLookup
import com.naveenapps.expensemanager.core.model.RatePurpose
import com.naveenapps.expensemanager.core.repository.ExchangeRateRepository
import kotlinx.coroutines.flow.first

/**
 * The rate to pre-fill a cross-currency transfer with, under the user's exchange method:
 * - API: today's fetched rate, and only for a transfer dated today — a current rate isn't the
 *   rate of a past day, so backdated transfers need a user-entered one;
 * - fixed rates: the pair's buy or sell value, for any date (it has no date of its own);
 * - disabled: nothing — the user types the rate, which is recorded on that transfer only.
 */
class GetCurrentExchangeRateUseCase(
    private val repository: ExchangeRateRepository,
) {
    suspend operator fun invoke(
        fromCode: String,
        toCode: String,
        isDatedToday: Boolean,
    ): SuggestedRate {
        if (fromCode.isBlank() || toCode.isBlank() || fromCode == toCode) {
            return SuggestedRate.None(SuggestedRate.Reason.UNAVAILABLE)
        }
        val method = repository.getMethod().first()
        if (method == ExchangeMethod.API && !isDatedToday) {
            return SuggestedRate.None(SuggestedRate.Reason.BACKDATED)
        }
        return when (val lookup = repository.getRate(fromCode, toCode, RatePurpose.TRANSFER)) {
            is RateLookup.Available -> SuggestedRate.Suggested(lookup)

            RateLookup.Disabled -> SuggestedRate.None(SuggestedRate.Reason.DISABLED)

            RateLookup.Unavailable -> SuggestedRate.None(
                if (method == ExchangeMethod.STATIC) {
                    SuggestedRate.Reason.NO_FIXED_RATE
                } else {
                    SuggestedRate.Reason.UNAVAILABLE
                },
            )
        }
    }
}

sealed interface SuggestedRate {
    data class Suggested(val rate: RateLookup.Available) : SuggestedRate

    data class None(val reason: Reason) : SuggestedRate

    enum class Reason {
        /** API method and the transfer isn't dated today. */
        BACKDATED,

        /** API method, but offline or the provider doesn't offer the pair. */
        UNAVAILABLE,

        /** Fixed-rate method, but no record exists for this pair. */
        NO_FIXED_RATE,

        /** Conversion is disabled. */
        DISABLED,
    }
}
