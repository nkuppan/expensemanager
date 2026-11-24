package com.naveenapps.expensemanager.core.domain.usecase.settings.currency

import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.CurrencyRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SaveCurrencyUseCaseTest : BaseCoroutineTest() {

    private val currency = Currency(
        symbol = "$",
        name = "US Dollars",
    )

    private val currencyRepository: CurrencyRepository = mock()

    private val saveCurrencyUseCase = SaveCurrencyUseCase(currencyRepository)

    @Test
    fun whenValidCurrencyReturnSuccess() = runTest {
        whenever(currencyRepository.saveCurrency(currency)).thenReturn(true)

        val response = saveCurrencyUseCase.invoke(currency)

        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Success::class.java)
        Truth.assertThat((response as Resource.Success).data).isTrue()
    }

    @Test
    fun currencyNameIsInvalidReturnError() = runTest {
        val response = saveCurrencyUseCase.invoke(currency.copy(symbol = ""))

        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
        val exception = (response as Resource.Error).exception
        Truth.assertThat(exception).isNotNull()
        Truth.assertThat(exception.message).isNotEmpty()
    }

    @Test
    fun currencySymbolIsInvalidReturnError() = runTest {
        val response = saveCurrencyUseCase.invoke(currency.copy(name = ""))

        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
        val exception = (response as Resource.Error).exception
        Truth.assertThat(exception).isNotNull()
        Truth.assertThat(exception.message).isNotEmpty()
    }
}
