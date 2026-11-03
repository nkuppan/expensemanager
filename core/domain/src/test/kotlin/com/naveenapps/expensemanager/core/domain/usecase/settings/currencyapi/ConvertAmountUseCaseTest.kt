package com.naveenapps.expensemanager.core.domain.usecase.settings.currencyapi

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.FormatAmountInCurrencyUseCase
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.ExchangeRateSource
import com.naveenapps.expensemanager.core.model.RateLookup
import com.naveenapps.expensemanager.core.model.RatePurpose
import com.naveenapps.expensemanager.core.repository.ExchangeRateRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ConvertAmountUseCaseTest : BaseCoroutineTest() {

    private val repository: ExchangeRateRepository = mock()
    private val formatAmountInCurrencyUseCase: FormatAmountInCurrencyUseCase = mock()
    private val useCase = ConvertAmountUseCase(repository, formatAmountInCurrencyUseCase)

    override fun onCreate() {
        super.onCreate()
        runBlocking {
            whenever(formatAmountInCurrencyUseCase.currencyFor(any())).thenAnswer {
                val code = it.arguments[0] as String
                Currency(symbol = code, name = code, code = code)
            }
        }
    }

    private suspend fun rateIs(lookup: RateLookup) {
        whenever(repository.getRate("SYP", "USD", RatePurpose.VIEW)).thenReturn(lookup)
    }

    private val mixed = listOf(200.0 to "SYP", 5.0 to "USD")

    @Test
    fun convertedTotalIsMarkedAsConverted() = runTest {
        rateIs(RateLookup.Available(0.01, ExchangeRateSource.FETCHED, "BUY_SELL_QUOTES"))

        val total = useCase.total(mixed, "USD")

        assertThat(total.value).isEqualTo(7.0)
        assertThat(total.usedConversion).isTrue()
        assertThat(total.usedFixedRates).isFalse()
    }

    @Test
    fun totalConvertedWithFixedRatesSaysSo() = runTest {
        rateIs(RateLookup.Available(0.01, ExchangeRateSource.STATIC))

        val total = useCase.total(mixed, "USD")

        assertThat(total.value).isEqualTo(7.0)
        assertThat(total.usedFixedRates).isTrue()
    }

    @Test
    fun totalWithoutARateIsNotMarkedAsConverted() = runTest {
        rateIs(RateLookup.Unavailable)

        val total = useCase.total(mixed, "USD")

        assertThat(total.value).isNull()
        assertThat(total.missingPairs).containsExactly("SYP→USD")
        assertThat(total.usedConversion).isFalse()
        assertThat(total.conversionDisabled).isFalse()
    }

    @Test
    fun sameCurrencyTotalIsNotMarkedAsConverted() = runTest {
        val total = useCase.total(listOf(2.0 to "USD", 5.0 to "USD"), "USD")

        assertThat(total.value).isEqualTo(7.0)
        assertThat(total.usedConversion).isFalse()
    }

    @Test
    fun disabledConversionKeepsEachCurrencyApart() = runTest {
        rateIs(RateLookup.Disabled)

        val total = useCase.total(mixed, "USD")

        assertThat(total.value).isNull()
        assertThat(total.conversionDisabled).isTrue()
        assertThat(total.missingPairs).isEmpty()
        assertThat(total.usedConversion).isFalse()
        assertThat(total.parts.map { it.currency?.code to it.amount })
            .containsExactly("USD" to 5.0, "SYP" to 200.0).inOrder()
    }

    @Test
    fun disabledConversionStillTotalsTheDisplayCurrencyAlone() = runTest {
        rateIs(RateLookup.Disabled)

        val total = useCase.total(listOf(2.0 to "USD", 5.0 to "USD"), "USD")

        assertThat(total.value).isEqualTo(7.0)
    }

    @Test
    fun disabledConversionNeverConvertsASingleAmount() = runTest {
        rateIs(RateLookup.Disabled)

        assertThat(useCase(200.0, "SYP", "USD")).isNull()
    }

    @Test
    fun subtractingDisabledTotalsWorksPerCurrency() = runTest {
        rateIs(RateLookup.Disabled)
        val income = useCase.total(listOf(500.0 to "USD"), "USD")
        val expense = useCase.total(listOf(20.0 to "USD", 208.0 to "SYP"), "USD")

        val balance = income - expense

        assertThat(balance.value).isNull()
        assertThat(balance.conversionDisabled).isTrue()
        assertThat(balance.parts.map { it.currency?.code to it.amount })
            .containsExactly("USD" to 480.0, "SYP" to -208.0).inOrder()
    }

    @Test
    fun addingTotalsWithAMissingRateStaysUnavailable() = runTest {
        rateIs(RateLookup.Unavailable)
        val known = useCase.total(listOf(5.0 to "USD"), "USD")
        val unknown = useCase.total(listOf(200.0 to "SYP"), "USD")

        val sum = listOf(known, unknown).sum()

        assertThat(sum.value).isNull()
        assertThat(sum.missingPairs).containsExactly("SYP→USD")
    }
}
