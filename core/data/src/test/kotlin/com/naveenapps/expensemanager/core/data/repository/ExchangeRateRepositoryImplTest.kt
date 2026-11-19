package com.naveenapps.expensemanager.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.database.ExpenseManagerDatabase
import com.naveenapps.expensemanager.core.datastore.ExchangeMethodDataStore
import com.naveenapps.expensemanager.core.model.CurrencyApiProfile
import com.naveenapps.expensemanager.core.model.ExchangeMethod
import com.naveenapps.expensemanager.core.model.ExchangeRate
import com.naveenapps.expensemanager.core.model.ExchangeRateSource
import com.naveenapps.expensemanager.core.model.RateLookup
import com.naveenapps.expensemanager.core.model.RatePurpose
import com.naveenapps.expensemanager.core.model.StaticExchangeRate
import com.naveenapps.expensemanager.core.repository.CurrencyApiRepository
import com.naveenapps.expensemanager.core.repository.ExchangeRateRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class ExchangeRateRepositoryImplTest : BaseCoroutineTest() {

    private val testContext: Context = ApplicationProvider.getApplicationContext()
    private val testDataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
        scope = TestScope(testCoroutineDispatcher.dispatcher),
        produceFile = {
            // A fresh file per test: the chosen method must not leak between tests.
            testContext.preferencesDataStoreFile("exchange_method_${UUID.randomUUID()}")
        },
    )

    private val profile = MutableStateFlow<CurrencyApiProfile?>(null)
    private val currencyApiRepository: CurrencyApiRepository = mock()

    private lateinit var database: ExpenseManagerDatabase
    private lateinit var repository: ExchangeRateRepository

    override fun onCreate() {
        super.onCreate()
        database = Room.inMemoryDatabaseBuilder(
            testContext,
            ExpenseManagerDatabase::class.java,
        ).allowMainThreadQueries().build()
        whenever(currencyApiRepository.getProfile()).thenReturn(profile)
        repository = ExchangeRateRepositoryImpl(
            methodDataStore = ExchangeMethodDataStore(testDataStore),
            staticExchangeRateDao = database.staticExchangeRateDao(),
            currencyApiRepository = currencyApiRepository,
            dispatchers = AppCoroutineDispatchers(
                testCoroutineDispatcher.dispatcher,
                testCoroutineDispatcher.dispatcher,
                testCoroutineDispatcher.dispatcher,
            ),
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        database.close()
    }

    private val usdSyp = StaticExchangeRate("USD", "SYP", buy = 138.0, sell = 140.0)

    private suspend fun rate(from: String, to: String, purpose: RatePurpose) = (repository.getRate(from, to, purpose) as RateLookup.Available).rate

    @Test
    fun methodDefaultsToDisabledWithoutAProvider() = runTest {
        assertThat(repository.getMethod().first()).isEqualTo(ExchangeMethod.DISABLED)
        assertThat(repository.isMethodChosen()).isFalse()
    }

    @Test
    fun methodDefaultsToApiWhenAProviderWasAlreadyConfigured() = runTest {
        profile.value = CurrencyApiProfile()

        assertThat(repository.getMethod().first()).isEqualTo(ExchangeMethod.API)
    }

    @Test
    fun chosenMethodWinsOverTheDefault() = runTest {
        profile.value = CurrencyApiProfile()

        repository.setMethod(ExchangeMethod.DISABLED)

        assertThat(repository.getMethod().first()).isEqualTo(ExchangeMethod.DISABLED)
        assertThat(repository.isMethodChosen()).isTrue()
    }

    @Test
    fun disabledAnswersDisabledAndNeverAsksTheApi() = runTest {
        repository.setMethod(ExchangeMethod.DISABLED)

        assertThat(repository.getRate("USD", "SYP", RatePurpose.VIEW))
            .isEqualTo(RateLookup.Disabled)
        verify(currencyApiRepository, never()).getRate(any(), any())
    }

    @Test
    fun apiAnswersWithTheFetchedRate() = runTest {
        repository.setMethod(ExchangeMethod.API)
        whenever(currencyApiRepository.getRate("USD", "SYP"))
            .thenReturn(ExchangeRate("BUY_SELL_QUOTES", "USD", "SYP", 139.0, 42L))

        assertThat(repository.getRate("USD", "SYP", RatePurpose.TRANSFER)).isEqualTo(
            RateLookup.Available(139.0, ExchangeRateSource.FETCHED, "BUY_SELL_QUOTES", 42L),
        )
    }

    @Test
    fun apiWithoutARateIsUnavailable() = runTest {
        repository.setMethod(ExchangeMethod.API)

        assertThat(repository.getRate("USD", "SYP", RatePurpose.VIEW))
            .isEqualTo(RateLookup.Unavailable)
    }

    @Test
    fun fixedRateUsesBuyWhenHandingOverTheBaseCurrency() = runTest {
        repository.setMethod(ExchangeMethod.STATIC)
        repository.saveStaticRate(usdSyp)

        assertThat(rate("USD", "SYP", RatePurpose.TRANSFER)).isEqualTo(138.0)
    }

    @Test
    fun fixedRateUsesSellWhenBuyingTheBaseCurrency() = runTest {
        repository.setMethod(ExchangeMethod.STATIC)
        repository.saveStaticRate(usdSyp)

        assertThat(rate("SYP", "USD", RatePurpose.TRANSFER)).isWithin(1e-12).of(1 / 140.0)
    }

    @Test
    fun fixedRateUsesTheMidpointForViewsInBothDirections() = runTest {
        repository.setMethod(ExchangeMethod.STATIC)
        repository.saveStaticRate(usdSyp)

        assertThat(rate("USD", "SYP", RatePurpose.VIEW)).isEqualTo(139.0)
        assertThat(rate("SYP", "USD", RatePurpose.VIEW)).isWithin(1e-12).of(1 / 139.0)
    }

    @Test
    fun fixedRateIsMarkedAsStatic() = runTest {
        repository.setMethod(ExchangeMethod.STATIC)
        repository.saveStaticRate(usdSyp)

        val lookup = repository.getRate("USD", "SYP", RatePurpose.VIEW) as RateLookup.Available
        assertThat(lookup.source).isEqualTo(ExchangeRateSource.STATIC)
        assertThat(lookup.provider).isNull()
    }

    @Test
    fun fixedMethodWithoutThePairIsUnavailable() = runTest {
        repository.setMethod(ExchangeMethod.STATIC)
        repository.saveStaticRate(usdSyp)

        assertThat(repository.getRate("EUR", "SYP", RatePurpose.VIEW))
            .isEqualTo(RateLookup.Unavailable)
    }

    @Test
    fun aPairHasOnlyOneRecordWhicheverWayRoundItIsSaved() = runTest {
        repository.saveStaticRate(usdSyp)
        repository.saveStaticRate(StaticExchangeRate("SYP", "USD", buy = 0.007, sell = 0.0072))
        repository.saveStaticRate(StaticExchangeRate("EUR", "SYP", buy = 150.0, sell = 152.0))

        val rates = repository.getStaticRates().first()
        assertThat(rates.map { it.baseCode to it.quoteCode })
            .containsExactly("SYP" to "USD", "EUR" to "SYP")
    }

    @Test
    fun deletingAPairRemovesItInEitherDirection() = runTest {
        repository.saveStaticRate(usdSyp)

        repository.deleteStaticRate("SYP", "USD")

        assertThat(repository.getStaticRates().first()).isEmpty()
    }
}
