package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.database.dao.StaticExchangeRateDao
import com.naveenapps.expensemanager.core.database.entity.StaticExchangeRateEntity
import com.naveenapps.expensemanager.core.datastore.ExchangeMethodDataStore
import com.naveenapps.expensemanager.core.model.ExchangeMethod
import com.naveenapps.expensemanager.core.model.ExchangeRateSource
import com.naveenapps.expensemanager.core.model.RateLookup
import com.naveenapps.expensemanager.core.model.RatePurpose
import com.naveenapps.expensemanager.core.model.StaticExchangeRate
import com.naveenapps.expensemanager.core.repository.CurrencyApiRepository
import com.naveenapps.expensemanager.core.repository.ExchangeRateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ExchangeRateRepositoryImpl(
    private val methodDataStore: ExchangeMethodDataStore,
    private val staticExchangeRateDao: StaticExchangeRateDao,
    private val currencyApiRepository: CurrencyApiRepository,
    private val dispatchers: AppCoroutineDispatchers,
    private val clock: () -> Long = System::currentTimeMillis,
) : ExchangeRateRepository {

    override fun getMethod(): Flow<ExchangeMethod> = combine(
        methodDataStore.getMethod(),
        currencyApiRepository.getProfile(),
    ) { chosen, profile ->
        // Installs that configured a provider before methods existed keep converting with it.
        chosen ?: if (profile != null) ExchangeMethod.API else ExchangeMethod.DISABLED
    }

    override suspend fun isMethodChosen(): Boolean = withContext(dispatchers.io) {
        methodDataStore.getMethod().first() != null
    }

    override suspend fun setMethod(method: ExchangeMethod): Unit = withContext(dispatchers.io) {
        methodDataStore.setMethod(method)
    }

    override fun getStaticRates(): Flow<List<StaticExchangeRate>> = staticExchangeRateDao.getAll().map { rates -> rates.map { it.toModel() } }

    override suspend fun saveStaticRate(rate: StaticExchangeRate): Unit = withContext(dispatchers.io) {
        staticExchangeRateDao.replacePair(
            StaticExchangeRateEntity(
                baseCode = rate.baseCode,
                quoteCode = rate.quoteCode,
                buy = rate.buy,
                sell = rate.sell,
                updatedOn = clock(),
            ),
        )
    }

    override suspend fun deleteStaticRate(baseCode: String, quoteCode: String): Unit = withContext(dispatchers.io) {
        staticExchangeRateDao.deletePair(baseCode, quoteCode)
    }

    override fun observeConfiguration(): Flow<Unit> = combine(
        getMethod(),
        getStaticRates(),
        currencyApiRepository.getProfile(),
    ) { _, _, _ -> }

    override suspend fun getRate(
        fromCode: String,
        toCode: String,
        purpose: RatePurpose,
    ): RateLookup = withContext(dispatchers.io) {
        when (getMethod().first()) {
            ExchangeMethod.DISABLED -> RateLookup.Disabled

            ExchangeMethod.STATIC -> staticRate(fromCode, toCode, purpose)

            ExchangeMethod.API -> currencyApiRepository.getRate(fromCode, toCode)?.let {
                RateLookup.Available(
                    rate = it.rate,
                    source = ExchangeRateSource.FETCHED,
                    provider = it.provider,
                    capturedAt = it.fetchedAt,
                )
            } ?: RateLookup.Unavailable
        }
    }

    /**
     * For a record "1 base: buy / sell quote": handing over the base gets [StaticExchangeRate.buy]
     * quote each, and buying one base costs [StaticExchangeRate.sell] quote. Views that only
     * value money use the midpoint in both directions.
     */
    private suspend fun staticRate(
        fromCode: String,
        toCode: String,
        purpose: RatePurpose,
    ): RateLookup {
        val pair = staticExchangeRateDao.findPair(fromCode, toCode)?.toModel()
            ?: return RateLookup.Unavailable
        val baseToQuote = pair.baseCode == fromCode
        val perBase = when (purpose) {
            RatePurpose.VIEW -> pair.mid
            RatePurpose.TRANSFER -> if (baseToQuote) pair.buy else pair.sell
        }
        if (!perBase.isFinite() || perBase <= 0.0) return RateLookup.Unavailable
        return RateLookup.Available(
            rate = if (baseToQuote) perBase else 1 / perBase,
            source = ExchangeRateSource.STATIC,
            capturedAt = clock(),
        )
    }

    private fun StaticExchangeRateEntity.toModel() = StaticExchangeRate(
        baseCode = baseCode,
        quoteCode = quoteCode,
        buy = buy,
        sell = sell,
        updatedOn = updatedOn,
    )
}
