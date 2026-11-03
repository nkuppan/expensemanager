package com.naveenapps.expensemanager.core.repository

import com.naveenapps.expensemanager.core.model.ExchangeMethod
import com.naveenapps.expensemanager.core.model.RateLookup
import com.naveenapps.expensemanager.core.model.RatePurpose
import com.naveenapps.expensemanager.core.model.StaticExchangeRate
import kotlinx.coroutines.flow.Flow

/**
 * The single source of conversion rates. Which rate it answers with depends on the user's
 * [ExchangeMethod]: the configured API, their own fixed pairs, or no conversion at all.
 */
interface ExchangeRateRepository {

    /**
     * The method in use. Until the user picks one it is [ExchangeMethod.API] when an API
     * profile was already configured, otherwise [ExchangeMethod.DISABLED].
     */
    fun getMethod(): Flow<ExchangeMethod>

    /** False until the user has picked a method themselves. */
    suspend fun isMethodChosen(): Boolean

    suspend fun setMethod(method: ExchangeMethod)

    fun getStaticRates(): Flow<List<StaticExchangeRate>>

    /** Saves [rate] as the one record of its pair, replacing it in either direction. */
    suspend fun saveStaticRate(rate: StaticExchangeRate)

    suspend fun deleteStaticRate(baseCode: String, quoteCode: String)

    /** Emits whenever anything that changes a conversion result changes. */
    fun observeConfiguration(): Flow<Unit>

    /** The rate from [fromCode] to [toCode] for [purpose] under the current method. */
    suspend fun getRate(fromCode: String, toCode: String, purpose: RatePurpose): RateLookup
}
