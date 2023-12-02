package com.naveenapps.expensemanager.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.datastore.CurrencyDataStore
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.TextFormat
import com.naveenapps.expensemanager.core.model.TextPosition
import com.naveenapps.expensemanager.core.repository.CurrencyRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CurrencyRepositoryImplTest : BaseCoroutineTest() {

    private val amount = Amount(
        amount = 100.0,
        amountString = null,
        currency = null
    )

    private val testContext: Context = ApplicationProvider.getApplicationContext()
    private val testCoroutineScope = TestScope(testCoroutineDispatcher.dispatcher)
    private val testDataStore: DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = testCoroutineScope,
            produceFile = {
                testContext.preferencesDataStoreFile("TEST_DATASTORE_NAME")
            }
        )

    private val repository: CurrencyRepository = CurrencyRepositoryImpl(
        CurrencyDataStore(testDataStore),
        AppCoroutineDispatchers(
            testCoroutineDispatcher.dispatcher,
            testCoroutineDispatcher.dispatcher,
            testCoroutineDispatcher.dispatcher
        )
    )

    @Test
    fun getDefaultCurrencyShouldReturnDefaultCurrency() = runTest {
        val currency = repository.getDefaultCurrency()
        Truth.assertThat(currency).isNotNull()
        Truth.assertThat(currency).isEqualTo(defaultCurrency)
    }

    @Test
    fun getSelectedCurrencyShouldReturnDefaultCurrency() = runTest {
        repository.getSelectedCurrency().test {
            val currency = awaitItem()
            Truth.assertThat(currency).isNotNull()
            Truth.assertThat(currency).isEqualTo(defaultCurrency)
        }
    }

    @Test
    fun getSelectedCurrencyShouldReturnAfterUpdate() = runTest {
        repository.getSelectedCurrency().test {
            val currency = awaitItem()
            Truth.assertThat(currency).isNotNull()
            Truth.assertThat(currency).isEqualTo(defaultCurrency)

            val updatingCurrency = defaultCurrency.copy(
                symbol = "€"
            )

            repository.saveCurrency(updatingCurrency)

            val newCurrency = awaitItem()
            Truth.assertThat(newCurrency).isNotNull()
            Truth.assertThat(newCurrency).isEqualTo(updatingCurrency)
        }
    }

    @Test
    fun getFormattedAmountWithoutCurrencyShouldReturnAmountWithDefaultCurrency() = runTest {
        val formattedAmount = repository.getFormattedCurrency(amount)
        Truth.assertThat(formattedAmount).isNotNull()
        Truth.assertThat(formattedAmount.amount).isEqualTo(amount.amount)
        Truth.assertThat(formattedAmount.amountString).isEqualTo("100.0$")
    }

    @Test
    fun getFormattedAmountWithCurrencyShouldReturnAmountWithPassedCurrency() = runTest {
        val passedAmount = amount.copy(amount = 120.0, currency = defaultCurrency.copy(symbol = "€"))
        val formattedAmount = repository.getFormattedCurrency(passedAmount)
        Truth.assertThat(formattedAmount).isNotNull()
        Truth.assertThat(formattedAmount.amount).isEqualTo(passedAmount.amount)
        Truth.assertThat(formattedAmount.amountString).isEqualTo("120.0€")
    }

    @Test
    fun getFormattedAmountWithPrefix() = runTest {
        val passedAmount = amount.copy(
            amount = 120.0,
            currency = defaultCurrency.copy(
                symbol = "€",
                position = TextPosition.PREFIX
            )
        )
        val formattedAmount = repository.getFormattedCurrency(passedAmount)
        Truth.assertThat(formattedAmount).isNotNull()
        Truth.assertThat(formattedAmount.amount).isEqualTo(passedAmount.amount)
        Truth.assertThat(formattedAmount.amountString).isEqualTo("€120.0")
    }

    @Test
    fun getFormattedAmountWithPrefixAndNumberFormatOne() = runTest {
        val passedAmount = amount.copy(
            amount = 1200.0,
            currency = defaultCurrency.copy(
                symbol = "€",
                position = TextPosition.PREFIX,
                format = TextFormat.NUMBER_FORMAT,
            )
        )
        val formattedAmount = repository.getFormattedCurrency(passedAmount)
        Truth.assertThat(formattedAmount).isNotNull()
        Truth.assertThat(formattedAmount.amount).isEqualTo(passedAmount.amount)
        Truth.assertThat(formattedAmount.amountString).isEqualTo("€1,200")
    }

    @Test
    fun getFormattedAmountWithNegativePrefixAndNumberFormatOne() = runTest {
        val passedAmount = amount.copy(
            amount = -1200.0,
            currency = defaultCurrency.copy(
                symbol = "€",
                position = TextPosition.PREFIX,
                format = TextFormat.NUMBER_FORMAT,
            )
        )
        val formattedAmount = repository.getFormattedCurrency(passedAmount)
        Truth.assertThat(formattedAmount).isNotNull()
        Truth.assertThat(formattedAmount.amount).isEqualTo(passedAmount.amount)
        Truth.assertThat(formattedAmount.amountString).isEqualTo("-€1,200")
    }

    @Test
    fun getFormattedAmountWithBigNumberAndTextFormatOne() = runTest {
        val passedAmount = amount.copy(
            amount = -1200000000.0,
            currency = defaultCurrency.copy(
                symbol = "€",
                position = TextPosition.PREFIX,
                format = TextFormat.NUMBER_FORMAT,
            )
        )
        val formattedAmount = repository.getFormattedCurrency(passedAmount)
        Truth.assertThat(formattedAmount).isNotNull()
        Truth.assertThat(formattedAmount.amount).isEqualTo(passedAmount.amount)
        Truth.assertThat(formattedAmount.amountString).isEqualTo("-€1,200,000,000")
    }

    @Test
    fun getFormattedAmountWithBigNumberAndSuffixTextFormatOne() = runTest {
        val passedAmount = amount.copy(
            amount = -1200000000.44,
            currency = defaultCurrency.copy(
                symbol = "€",
                position = TextPosition.SUFFIX,
                format = TextFormat.NUMBER_FORMAT,
            )
        )
        val formattedAmount = repository.getFormattedCurrency(passedAmount)
        Truth.assertThat(formattedAmount).isNotNull()
        Truth.assertThat(formattedAmount.amount).isEqualTo(passedAmount.amount)
        Truth.assertThat(formattedAmount.amountString).isEqualTo("-1,200,000,000.4€")
    }
}