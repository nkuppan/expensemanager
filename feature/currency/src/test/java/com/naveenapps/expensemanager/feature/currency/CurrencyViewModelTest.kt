package com.naveenapps.expensemanager.feature.currency

import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetDefaultCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.SaveCurrencyUseCase
import com.naveenapps.expensemanager.core.model.Country
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.CurrencyPosition
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.repository.CurrencyRepository
import com.naveenapps.expensemanager.core.settings.domain.model.NumberFormatType
import com.naveenapps.expensemanager.core.settings.domain.repository.NumberFormatSettingRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CurrencyViewModelTest : BaseCoroutineTest() {

    private val usd = Currency(
        name = "US Dollars",
        symbol = "$",
        code = "USD",
        position = CurrencyPosition.SUFFIX,
    )

    private val currencyRepository: CurrencyRepository = mock()
    private val numberFormatSettingRepository: NumberFormatSettingRepository = mock()
    private val appComposeNavigator: AppComposeNavigator = mock()

    private lateinit var viewModel: CurrencyViewModel

    override fun onCreate() {
        whenever(currencyRepository.getDefaultCurrency()).thenReturn(usd)
        whenever(currencyRepository.getSelectedCurrency()).thenReturn(MutableStateFlow(usd))
        whenever(numberFormatSettingRepository.getNumberFormatType())
            .thenReturn(MutableStateFlow(NumberFormatType.WITHOUT_ANY_SEPARATOR))

        viewModel = CurrencyViewModel(
            GetDefaultCurrencyUseCase(currencyRepository),
            GetCurrencyUseCase(currencyRepository),
            SaveCurrencyUseCase(currencyRepository),
            numberFormatSettingRepository,
            appComposeNavigator,
        )
    }

    @Test
    fun selectingCurrencyShouldUpdateCodeAndKeepPosition() = runTest {
        whenever(currencyRepository.saveCurrency(any())).thenReturn(true)
        val rupee = Currency(name = "Indian Rupee", symbol = "₹", code = "INR")

        viewModel.processAction(
            CurrencyAction.SelectCurrency(
                Country(
                    name = "India",
                    countryCode = "IN",
                    currencyCode = "INR",
                    currency = rupee,
                ),
            ),
        )
        advanceUntilIdle()

        val selected = viewModel.state.value.currency
        Truth.assertThat(selected.code).isEqualTo("INR")
        Truth.assertThat(selected.symbol).isEqualTo("₹")
        Truth.assertThat(selected.name).isEqualTo("Indian Rupee")
        Truth.assertThat(selected.position).isEqualTo(CurrencyPosition.SUFFIX)

        val captor = argumentCaptor<Currency>()
        verify(currencyRepository, atLeastOnce()).saveCurrency(captor.capture())
        Truth.assertThat(captor.lastValue.code).isEqualTo("INR")
    }
}
