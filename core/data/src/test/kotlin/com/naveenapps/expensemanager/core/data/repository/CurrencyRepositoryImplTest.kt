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
import com.naveenapps.expensemanager.core.model.CurrencyPosition
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
        numberFormatSettingRepository = numberFormatSettingRepository,
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
                position = CurrencyPosition.PREFIX,
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
                position = CurrencyPosition.PREFIX,
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
                position = CurrencyPosition.PREFIX,
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
                position = CurrencyPosition.PREFIX,
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
                position = CurrencyPosition.SUFFIX,
            ),
        )
        val formattedAmount = repository.getFormattedCurrency(passedAmount)
        Truth.assertThat(formattedAmount).isNotNull()
        Truth.assertThat(formattedAmount.amount).isEqualTo(passedAmount.amount)
        Truth.assertThat(formattedAmount.amountString).isEqualTo("-1,200,000,000.44€")
    }

    // region word / right-to-left symbols (ليرة): position and minus sign

    private val lira = "ليرة"
    private val ltrMark = "\u200E"

    private fun formatLira(value: Double, position: CurrencyPosition, symbol: String = lira): String? = repository.getFormattedCurrency(
        amount.copy(
            amount = value,
            currency = defaultCurrency.copy(symbol = symbol, position = position),
        ),
    ).amountString

    @Test
    fun `Given arabic symbol in prefix when formatting then symbol stays before the number`() = runTest {
        formatTypeFlow.value = NumberFormatType.WITH_COMMA_SEPARATOR
        advanceUntilIdle()

        Truth.assertThat(formatLira(10585.0, CurrencyPosition.PREFIX))
            .isEqualTo("$ltrMark$lira$ltrMark 10,585")
    }

    @Test
    fun `Given arabic symbol in suffix when formatting then symbol stays after the number`() = runTest {
        formatTypeFlow.value = NumberFormatType.WITH_COMMA_SEPARATOR
        advanceUntilIdle()

        Truth.assertThat(formatLira(10585.0, CurrencyPosition.SUFFIX))
            .isEqualTo("${ltrMark}10,585 $lira$ltrMark")
    }

    @Test
    fun `Given arabic symbol in prefix and negative amount then minus sits on the number not the symbol`() = runTest {
        formatTypeFlow.value = NumberFormatType.WITH_COMMA_SEPARATOR
        advanceUntilIdle()

        Truth.assertThat(formatLira(-208.0, CurrencyPosition.PREFIX))
            .isEqualTo("$ltrMark$lira$ltrMark -208")
    }

    @Test
    fun `Given arabic symbol in suffix and negative amount then minus leads the number`() = runTest {
        formatTypeFlow.value = NumberFormatType.WITH_COMMA_SEPARATOR
        advanceUntilIdle()

        Truth.assertThat(formatLira(-208.0, CurrencyPosition.SUFFIX))
            .isEqualTo("$ltrMark-208 $lira$ltrMark")
    }

    @Test
    fun `Given latin word symbol in prefix and negative amount then minus sits on the number`() = runTest {
        formatTypeFlow.value = NumberFormatType.WITH_COMMA_SEPARATOR
        advanceUntilIdle()

        Truth.assertThat(formatLira(-208.0, CurrencyPosition.PREFIX, symbol = "ALL"))
            .isEqualTo("ALL -208")
    }

    // endregion
}
