package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.model.ExchangeRateSnapshot
import com.naveenapps.expensemanager.core.model.ExchangeRateSource
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.repository.AccountRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import com.naveenapps.expensemanager.core.testing.FAKE_ACCOUNT
import com.naveenapps.expensemanager.core.testing.FAKE_EXPENSE_TRANSACTION
import com.naveenapps.expensemanager.core.testing.FAKE_SECOND_ACCOUNT
import java.util.Date
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class CheckTransactionValidationUseCaseTest : BaseCoroutineTest() {

    private val accountRepository: AccountRepository = mock()
    private val useCase = CheckTransactionValidationUseCase(accountRepository)

    private val usd = FAKE_ACCOUNT.copy(id = "usd", currencyCode = "USD")
    private val syp = FAKE_SECOND_ACCOUNT.copy(id = "syp", currencyCode = "SYP")

    private val transfer = FAKE_EXPENSE_TRANSACTION.copy(
        type = TransactionType.TRANSFER,
        fromAccountId = "usd",
        toAccountId = "syp",
        fromCurrencyCode = "USD",
        toCurrencyCode = "SYP",
        createdOn = Date(),
    )

    private fun snapshot(
        source: ExchangeRateSource = ExchangeRateSource.USER,
        base: String = "USD",
        quote: String = "SYP",
        rate: Double = 137.5,
        capturedAt: Long = System.currentTimeMillis(),
    ) = ExchangeRateSnapshot("s1", source, "BUY_SELL_QUOTES", base, quote, rate, capturedAt)

    override fun onCreate() {
        super.onCreate()
        kotlinx.coroutines.runBlocking {
            whenever(accountRepository.findAccount("usd")).thenReturn(Resource.Success(usd))
            whenever(accountRepository.findAccount("syp")).thenReturn(Resource.Success(syp))
        }
    }

    private suspend fun assertRejected(transaction: com.naveenapps.expensemanager.core.model.Transaction) {
        assertThat(useCase(transaction)).isInstanceOf(Resource.Error::class.java)
    }

    @Test
    fun crossCurrencyTransferWithMatchingSnapshotIsValid() = runTest {
        assertThat(useCase(transfer.copy(rateSnapshot = snapshot())))
            .isInstanceOf(Resource.Success::class.java)
    }

    @Test
    fun crossCurrencyTransferWithoutRateIsRejected() = runTest {
        assertRejected(transfer)
    }

    @Test
    fun snapshotForTheWrongPairIsRejected() = runTest {
        assertRejected(transfer.copy(rateSnapshot = snapshot(base = "EUR")))
    }

    @Test
    fun nonPositiveRateIsRejected() = runTest {
        assertRejected(transfer.copy(rateSnapshot = snapshot(rate = 0.0)))
    }

    @Test
    fun currencyNotMatchingTheAccountIsRejected() = runTest {
        assertRejected(transfer.copy(fromCurrencyCode = "EUR", rateSnapshot = snapshot(base = "EUR")))
    }

    @Test
    fun fetchedRateOnABackdatedTransferIsRejected() = runTest {
        val lastWeek = Date(System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000)
        assertRejected(
            transfer.copy(
                createdOn = lastWeek,
                rateSnapshot = snapshot(source = ExchangeRateSource.FETCHED),
            ),
        )
    }

    @Test
    fun fixedRateOnABackdatedTransferIsValid() = runTest {
        val lastWeek = Date(System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000)
        assertThat(
            useCase(
                transfer.copy(
                    createdOn = lastWeek,
                    rateSnapshot = snapshot(source = ExchangeRateSource.STATIC),
                ),
            ),
        ).isInstanceOf(Resource.Success::class.java)
    }

    @Test
    fun userRateOnABackdatedTransferIsValid() = runTest {
        val lastWeek = Date(System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000)
        assertThat(useCase(transfer.copy(createdOn = lastWeek, rateSnapshot = snapshot())))
            .isInstanceOf(Resource.Success::class.java)
    }

    @Test
    fun sameCurrencyTransactionWithASnapshotIsRejected() = runTest {
        val expense = FAKE_EXPENSE_TRANSACTION.copy(
            fromAccountId = "usd",
            fromCurrencyCode = "USD",
            toCurrencyCode = "USD",
            rateSnapshot = snapshot(quote = "USD"),
        )
        assertRejected(expense)
    }

    @Test
    fun archivedAccountIsRejected() = runTest {
        whenever(accountRepository.findAccount("usd"))
            .thenReturn(Resource.Success(usd.copy(isArchived = true)))
        assertRejected(transfer.copy(rateSnapshot = snapshot()))
    }
}
