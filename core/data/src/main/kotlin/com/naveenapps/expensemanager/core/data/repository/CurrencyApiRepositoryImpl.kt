package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.database.dao.ExchangeRateDao
import com.naveenapps.expensemanager.core.database.entity.ExchangeRateEntity
import com.naveenapps.expensemanager.core.datastore.CurrencyApiDataStore
import com.naveenapps.expensemanager.core.model.CurrencyApiProfile
import com.naveenapps.expensemanager.core.model.ExchangeRate
import com.naveenapps.expensemanager.core.network.ExchangeRateApi
import com.naveenapps.expensemanager.core.repository.CurrencyApiRepository
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class CurrencyApiRepositoryImpl(
    private val dataStore: CurrencyApiDataStore,
    private val exchangeRateApi: ExchangeRateApi,
    private val exchangeRateDao: ExchangeRateDao,
    private val dispatchers: AppCoroutineDispatchers,
    private val clock: () -> Long = System::currentTimeMillis,
) : CurrencyApiRepository {

    /** One lock per provider+base, so concurrent screens share a single fetch instead of racing. */
    private val fetchLocks = ConcurrentHashMap<String, Mutex>()

    /** Last failed fetch per provider+base: offline, don't retry for every single amount. */
    private val lastFailedFetchAt = ConcurrentHashMap<String, Long>()

    override fun getProfile(): Flow<CurrencyApiProfile?> = dataStore.getProfile()

    override suspend fun saveProfile(profile: CurrencyApiProfile): Unit = withContext(dispatchers.io) {
        dataStore.saveProfile(profile)
        // Never mix rates from two providers: drop everything cached from the others.
        exchangeRateDao.deleteOtherProviders(providerKey(profile))
    }

    override suspend fun fetchLatestRates(
        baseCurrencyCode: String,
    ): Result<Map<String, Double>> = withContext(dispatchers.io) {
        runCatching {
            val profile = dataStore.getProfile().first()
                ?: error("No currency API profile configured")
            exchangeRateApi.getLatestRates(profile, baseCurrencyCode)
        }
    }

    override suspend fun getRate(fromCode: String, toCode: String): ExchangeRate? = withContext(dispatchers.io) {
        val profile = dataStore.getProfile().first() ?: return@withContext null
        val provider = providerKey(profile)
        val key = "$provider|$fromCode"
        val lock = fetchLocks.getOrPut(key) { Mutex() }
        lock.withLock {
            // Inside the lock: a concurrent caller may have just refreshed this base.
            val lastFetchedAt = exchangeRateDao.lastFetchedAt(provider, fromCode)
            if (lastFetchedAt != null && isFresh(lastFetchedAt)) {
                // Fresh fetch of this base: a missing pair means the provider doesn't
                // offer it — answer null without asking the network again.
                return@withLock exchangeRateDao.findRate(provider, fromCode, toCode)
                    ?.toModel()
            }

            val recentlyFailed = lastFailedFetchAt[key]
                ?.let { clock() - it < RETRY_AFTER_FAILURE_MILLIS } == true
            val refreshed = if (recentlyFailed) {
                null
            } else {
                runCatching { exchangeRateApi.getLatestRates(profile, fromCode) }
                    .onFailure { lastFailedFetchAt[key] = clock() }
                    .onSuccess { lastFailedFetchAt.remove(key) }
                    .getOrNull()
            }
            if (refreshed != null) {
                val now = clock()
                // The base->base row (always 1.0) records *when* this base was fetched,
                // even if the provider returned no rates at all.
                val rows = (refreshed + (fromCode to 1.0)).map { (targetCode, rate) ->
                    ExchangeRateEntity(provider, fromCode, targetCode, rate, now)
                }
                exchangeRateDao.replaceBase(provider, fromCode, rows)
            }
            // Refresh failed (offline, upstream error): fall back to the last cached value.
            exchangeRateDao.findRate(provider, fromCode, toCode)?.toModel()
        }
    }

    private fun isFresh(fetchedAt: Long): Boolean = clock() - fetchedAt < CACHE_TTL_MILLIS

    private fun providerKey(profile: CurrencyApiProfile): String = "${profile.preset.name}|${profile.baseUrl.trimEnd('/')}"

    private fun ExchangeRateEntity.toModel() = ExchangeRate(
        provider = provider.substringBefore('|'),
        baseCode = baseCode,
        targetCode = targetCode,
        rate = rate,
        fetchedAt = fetchedAt,
    )

    companion object {
        private const val CACHE_TTL_MILLIS = 60 * 60 * 1000L // 1 hour
        private const val RETRY_AFTER_FAILURE_MILLIS = 60 * 1000L // 1 minute
    }
}
