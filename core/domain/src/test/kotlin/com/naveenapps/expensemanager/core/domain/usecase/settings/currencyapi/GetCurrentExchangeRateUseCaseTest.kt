package com.naveenapps.expensemanager.core.domain.usecase.settings.currencyapi

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.model.ExchangeMethod
import com.naveenapps.expensemanager.core.model.ExchangeRateSource
import com.naveenapps.expensemanager.core.model.RateLookup
import com.naveenapps.expensemanager.core.model.RatePurpose
import com.naveenapps.expensemanager.core.repository.ExchangeRateRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class GetCurrentExchangeRateUseCaseTest : BaseCoroutineTest() {

    private val repository: ExchangeRateRepository = mock()
    private val useCase = GetCurrentExchangeRateUseCase(repository)

    private suspend fun given(method: ExchangeMethod, lookup: RateLookup) {
        whenever(repository.getMethod()).thenReturn(flowOf(method))
        whenever(repository.getRate("USD", "SYP", RatePurpose.TRANSFER)).thenReturn(lookup)
    }

    @Test
    fun apiSuggestsTheFetchedRateForATransferDatedToday() = runTest {
        val fetched = RateLookup.Available(139.0, ExchangeRateSource.FETCHED, "BUY_SELL_QUOTES", 42L)
        given(ExchangeMethod.API, fetched)

        assertThat(useCase("USD", "SYP", isDatedToday = true))
            .isEqualTo(SuggestedRate.Suggested(fetched))
    }

    @Test
    fun apiNeverFetchesForABackdatedTransfer() = runTest {
        given(ExchangeMethod.API, RateLookup.Available(139.0, ExchangeRateSource.FETCHED))

        assertThat(useCase("USD", "SYP", isDatedToday = false))
            .isEqualTo(SuggestedRate.None(SuggestedRate.Reason.BACKDATED))
        verify(repository, never()).getRate(any(), any(), any())
    }

    @Test
    fun fixedRateIsSuggestedForAnyDate() = runTest {
        val fixed = RateLookup.Available(138.0, ExchangeRateSource.STATIC)
        given(ExchangeMethod.STATIC, fixed)

        assertThat(useCase("USD", "SYP", isDatedToday = false))
            .isEqualTo(SuggestedRate.Suggested(fixed))
    }

    @Test
    fun fixedMethodWithoutThePairAsksForARate() = runTest {
        given(ExchangeMethod.STATIC, RateLookup.Unavailable)

        assertThat(useCase("USD", "SYP", isDatedToday = true))
            .isEqualTo(SuggestedRate.None(SuggestedRate.Reason.NO_FIXED_RATE))
    }

    @Test
    fun disabledSuggestsNothing() = runTest {
        given(ExchangeMethod.DISABLED, RateLookup.Disabled)

        assertThat(useCase("USD", "SYP", isDatedToday = true))
            .isEqualTo(SuggestedRate.None(SuggestedRate.Reason.DISABLED))
    }
}
