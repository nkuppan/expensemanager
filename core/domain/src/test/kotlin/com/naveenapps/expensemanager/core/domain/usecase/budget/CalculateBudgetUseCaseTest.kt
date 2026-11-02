package com.naveenapps.expensemanager.core.domain.usecase.budget

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.FormatAmountInCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currencyapi.ConvertAmountUseCase
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.ExchangeRateSource
import com.naveenapps.expensemanager.core.model.RateLookup
import com.naveenapps.expensemanager.core.model.RatePurpose
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.repository.CurrencyRepository
import com.naveenapps.expensemanager.core.repository.ExchangeRateRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import com.naveenapps.expensemanager.core.testing.FAKE_BUDGET
import com.naveenapps.expensemanager.core.testing.FAKE_EXPENSE_TRANSACTION
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class CalculateBudgetUseCaseTest : BaseCoroutineTest() {

    private val getBudgetTransactionsUseCase: GetBudgetTransactionsUseCase = mock()
    private val exchangeRateRepository: ExchangeRateRepository = mock()
    private val formatAmountInCurrencyUseCase: FormatAmountInCurrencyUseCase = mock()
    private val currencyRepository: CurrencyRepository = mock()

    private val useCase = CalculateBudgetUseCase(
        getBudgetTransactionsUseCase = getBudgetTransactionsUseCase,
        convertAmountUseCase = ConvertAmountUseCase(
            exchangeRateRepository,
            formatAmountInCurrencyUseCase,
        ),
        formatAmountInCurrencyUseCase = formatAmountInCurrencyUseCase,
        getFormattedAmountUseCase = GetFormattedAmountUseCase(currencyRepository),
    )

    private val budget = FAKE_BUDGET.copy(amount = 1000.0, currencyCode = "SYP")

    private fun expense(id: String, amount: Double, code: String) = FAKE_EXPENSE_TRANSACTION.copy(
        id = id,
        type = TransactionType.EXPENSE,
        amount = Amount(amount),
        fromCurrencyCode = code,
        toCurrencyCode = code,
    )

    override fun onCreate() {
        super.onCreate()
        runBlocking {
            whenever(formatAmountInCurrencyUseCase.currencyFor(any())).thenAnswer {
                val code = it.arguments[0] as String
                Currency(symbol = code, name = code, code = code)
            }
            whenever(formatAmountInCurrencyUseCase.invoke(any(), any())).thenAnswer {
                Amount(it.arguments[0] as Double)
            }
            whenever(getBudgetTransactionsUseCase(any())).thenReturn(
                Resource.Success(
                    listOf(expense("a", 200.0, "SYP"), expense("b", 5.0, "USD")),
                ),
            )
        }
        whenever(currencyRepository.getFormattedCurrency(any())).thenAnswer {
            val amount = it.arguments[0] as Amount
            amount.copy(amountString = "${amount.amount} ${amount.currency?.code}")
        }
    }

    @Test
    fun disabledConversionCountsOnlyTheBudgetsOwnCurrency() = runTest {
        whenever(exchangeRateRepository.getRate("USD", "SYP", RatePurpose.VIEW))
            .thenReturn(RateLookup.Disabled)

        val result = useCase(budget)

        assertThat(result.transactionAmount.amount).isEqualTo(200.0)
        assertThat(result.percent).isEqualTo(20f)
        assertThat(result.otherCurrenciesExcluded).isTrue()
        assertThat(result.convertedAtCurrentRates).isFalse()
    }

    @Test
    fun otherCurrenciesAreConvertedIntoTheBudgetsCurrency() = runTest {
        whenever(exchangeRateRepository.getRate("USD", "SYP", RatePurpose.VIEW))
            .thenReturn(RateLookup.Available(100.0, ExchangeRateSource.STATIC))

        val result = useCase(budget)

        assertThat(result.transactionAmount.amount).isEqualTo(700.0)
        assertThat(result.percent).isEqualTo(70f)
        assertThat(result.otherCurrenciesExcluded).isFalse()
        assertThat(result.convertedAtCurrentRates).isTrue()
    }

    @Test
    fun aMissingRateLeavesSpendingUnavailable() = runTest {
        whenever(exchangeRateRepository.getRate("USD", "SYP", RatePurpose.VIEW))
            .thenReturn(RateLookup.Unavailable)

        val result = useCase(budget)

        assertThat(result.transactionAmount.isAvailable).isFalse()
        assertThat(result.percent).isEqualTo(0f)
        assertThat(result.otherCurrenciesExcluded).isFalse()
    }
}
