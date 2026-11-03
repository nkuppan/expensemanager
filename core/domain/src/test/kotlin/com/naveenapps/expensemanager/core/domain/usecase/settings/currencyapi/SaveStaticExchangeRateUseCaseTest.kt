package com.naveenapps.expensemanager.core.domain.usecase.settings.currencyapi

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.StaticExchangeRate
import com.naveenapps.expensemanager.core.repository.ExchangeRateRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class SaveStaticExchangeRateUseCaseTest : BaseCoroutineTest() {

    private val repository: ExchangeRateRepository = mock()
    private val useCase = SaveStaticExchangeRateUseCase(repository)

    private val usdSyp = StaticExchangeRate("USD", "SYP", buy = 138.0, sell = 140.0)

    @Test
    fun validPairIsSaved() = runTest {
        assertThat(useCase(usdSyp)).isInstanceOf(Resource.Success::class.java)
        verify(repository).saveStaticRate(usdSyp)
    }

    @Test
    fun sameCurrencyTwiceIsRejected() = runTest {
        assertThat(useCase(usdSyp.copy(quoteCode = "USD"))).isInstanceOf(Resource.Error::class.java)
        verify(repository, never()).saveStaticRate(any())
    }

    @Test
    fun nonPositiveRatesAreRejected() = runTest {
        assertThat(useCase(usdSyp.copy(buy = 0.0))).isInstanceOf(Resource.Error::class.java)
        assertThat(useCase(usdSyp.copy(sell = -1.0))).isInstanceOf(Resource.Error::class.java)
        verify(repository, never()).saveStaticRate(any())
    }

    @Test
    fun editingToADifferentPairRemovesTheOldRecord() = runTest {
        useCase(usdSyp.copy(baseCode = "EUR"), replacing = usdSyp)

        verify(repository).deleteStaticRate("USD", "SYP")
    }

    @Test
    fun editingTheSamePairReversedOnlyReplacesIt() = runTest {
        useCase(StaticExchangeRate("SYP", "USD", 0.007, 0.0072), replacing = usdSyp)

        verify(repository, never()).deleteStaticRate(any(), any())
    }
}
