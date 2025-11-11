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
import com.naveenapps.expensemanager.core.model.TextPosition
import com.naveenapps.expensemanager.core.repository.CurrencyRepository
import com.naveenapps.expensemanager.core.settings.data.repository.NumberFormatRepositoryImpl
import com.naveenapps.expensemanager.core.settings.domain.model.NumberFormatType
import com.naveenapps.expensemanager.core.settings.domain.repository.NumberFormatSettingRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class CurrencyRepositoryImplTest : BaseCoroutineTest() {

    private val amount = Amount(
        amount = 100.0,
        amountString = null,
        currency = null,
    )

    private val testContext: Context = ApplicationProvider.getApplicationContext()
    private val testDataStore: DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = TestScope(testCoroutineDispatcher.dispatcher),
            produceFile = {
                testContext.preferencesDataStoreFile("TEST_DATASTORE_NAME")
            },
        )


    val formatTypeFlow = MutableStateFlow(NumberFormatType.WITHOUT_ANY_SEPARATOR)

    private val numberFormatSettingRepository = mock<NumberFormatSettingRepository> {
        whenever(it.getNumberFormatType()).thenReturn(formatTypeFlow)
    }

    private val numberFormatRepository = NumberFormatRepositoryImpl(
        coroutineScope = CoroutineScope(testCoroutineDispatcher.dispatcher),
        numberFormatSettingRepository = numberFormatSettingRepository
    )

    private val repository: CurrencyRepository = CurrencyRepositoryImpl(
        dataStore = CurrencyDataStore(dataStore = testDataStore),
        numberFormatRepository = numberFormatRepository,
        dispatchers = AppCoroutineDispatchers(
            testCoroutineDispatcher.dispatcher,
            testCoroutineDispatcher.dispatcher,
            testCoroutineDispatcher.dispatcher,
        ),
    )

    @Test
    fun `Given default currency when getting a default currency will return the same`() = runTest {
        val currency = repository.getDefaultCurrency()
        Truth.assertThat(currency).isNotNull()
        Truth.assertThat(currency).isEqualTo(defaultCurrency)
    }

    @Test
    fun `Given no selected currency when getting a selected currency will return the default currency`() = runTest {
        repository.getSelectedCurrency().test {
            val currency = awaitItem()
            Truth.assertThat(currency).isNotNull()
            Truth.assertThat(currency).isEqualTo(defaultCurrency)
        }
    }

    @Test
    fun `Given new currency and saving when reading the selected currency will return the saved currency`() = runTest {
        repository.getSelectedCurrency().test {
            val currency = awaitItem()
            Truth.assertThat(currency).isNotNull()
            Truth.assertThat(currency).isEqualTo(defaultCurrency)

            val updatingCurrency = defaultCurrency.copy(
                symbol = "€",
            )

            repository.saveCurrency(updatingCurrency)

            val newCurrency = awaitItem()
            Truth.assertThat(newCurrency).isNotNull()
            Truth.assertThat(newCurrency).isEqualTo(updatingCurrency)
        }
    }

    @Test
    fun `Given default currency when reading a formatted will return the amount with default currency`() = runTest {
        val formattedAmount = repository.getFormattedCurrency(amount)
        Truth.assertThat(formattedAmount).isNotNull()
        Truth.assertThat(formattedAmount.amount).isEqualTo(amount.amount)
        Truth.assertThat(formattedAmount.amountString).isEqualTo("100.0$")
    }

    @Test
    fun `Given new currency when reading a formatted will return the amount with new currency`() = runTest {
        val passedAmount = amount.copy(amount = 120.0, currency = defaultCurrency.copy(symbol = "€"))
        val formattedAmount = repository.getFormattedCurrency(passedAmount)
        Truth.assertThat(formattedAmount).isNotNull()
        Truth.assertThat(formattedAmount.amount).isEqualTo(passedAmount.amount)
        Truth.assertThat(formattedAmount.amountString).isEqualTo("120.0€")
    }

    @Test
    fun `Given new currency and prefix when reading a formatted will return the amount with new currency in prefix`() = runTest {
        val passedAmount = amount.copy(
            amount = 120.0,
            currency = defaultCurrency.copy(
                symbol = "€",
                position = TextPosition.PREFIX,
            ),
        )
        val formattedAmount = repository.getFormattedCurrency(passedAmount)
        Truth.assertThat(formattedAmount).isNotNull()
        Truth.assertThat(formattedAmount.amount).isEqualTo(passedAmount.amount)
        Truth.assertThat(formattedAmount.amountString).isEqualTo("€120.0")
    }

    @Test
    fun `Given new currency and prefix and number format with grouping when reading a formatted will return the amount with new currency in prefix and number grouping`() = runTest {

        formatTypeFlow.value = NumberFormatType.WITH_COMMA_SEPARATOR
        advanceUntilIdle()

        val passedAmount = amount.copy(
            amount = 1200.0,
            currency = defaultCurrency.copy(
                symbol = "€",
                position = TextPosition.PREFIX,
            ),
        )
        val formattedAmount = repository.getFormattedCurrency(passedAmount)
        Truth.assertThat(formattedAmount).isNotNull()
        Truth.assertThat(formattedAmount.amount).isEqualTo(passedAmount.amount)
        Truth.assertThat(formattedAmount.amountString).isEqualTo("€1,200")
    }

    @Test
    fun `Given new currency and prefix and number format and negative number with grouping when reading a formatted will return the amount with new currency in prefix and number grouping`() = runTest {

        formatTypeFlow.value = NumberFormatType.WITH_COMMA_SEPARATOR
        advanceUntilIdle()

        val passedAmount = amount.copy(
            amount = -1200.0,
            currency = defaultCurrency.copy(
                symbol = "€",
                position = TextPosition.PREFIX,
            ),
        )
        val formattedAmount = repository.getFormattedCurrency(passedAmount)
        Truth.assertThat(formattedAmount).isNotNull()
        Truth.assertThat(formattedAmount.amount).isEqualTo(passedAmount.amount)
        Truth.assertThat(formattedAmount.amountString).isEqualTo("-€1,200")
    }

    @Test
    fun `Given new currency and prefix and number format and big negative number with grouping when reading a formatted will return the amount with new currency in prefix and number grouping`() = runTest {

        formatTypeFlow.value = NumberFormatType.WITH_COMMA_SEPARATOR
        advanceUntilIdle()

        val passedAmount = amount.copy(
            amount = -1200000000.0,
            currency = defaultCurrency.copy(
                symbol = "€",
                position = TextPosition.PREFIX,
            ),
        )
        val formattedAmount = repository.getFormattedCurrency(passedAmount)
        Truth.assertThat(formattedAmount).isNotNull()
        Truth.assertThat(formattedAmount.amount).isEqualTo(passedAmount.amount)
        Truth.assertThat(formattedAmount.amountString).isEqualTo("-€1,200,000,000")
    }

    @Test
    fun `Given new currency and suffix and number format and big negative number with grouping when reading a formatted will return the amount with new currency in suffix and number grouping`() = runTest {

        formatTypeFlow.value = NumberFormatType.WITH_COMMA_SEPARATOR
        advanceUntilIdle()
        
        val passedAmount = amount.copy(
            amount = -1200000000.44,
            currency = defaultCurrency.copy(
                symbol = "€",
                position = TextPosition.SUFFIX,
            ),
        )
        val formattedAmount = repository.getFormattedCurrency(passedAmount)
        Truth.assertThat(formattedAmount).isNotNull()
        Truth.assertThat(formattedAmount.amount).isEqualTo(passedAmount.amount)
        Truth.assertThat(formattedAmount.amountString).isEqualTo("-1,200,000,000.4€")
    }
}
