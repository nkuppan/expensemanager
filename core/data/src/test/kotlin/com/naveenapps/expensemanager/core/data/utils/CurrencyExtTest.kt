package com.naveenapps.expensemanager.core.data.utils

import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.data.repository.defaultCurrency
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.Locale

class CurrencyExtTest : BaseCoroutineTest() {

    @Test
    fun getCurrencyWithDifferentLocale() = runTest {
        val amount = 0.0
        val formattedAmount = getCurrency(
            defaultCurrency,
            amount,
            Locale.GERMANY
        )

        Truth.assertThat(formattedAmount).isNotNull()
        Truth.assertThat(formattedAmount).isEqualTo("0,0$")
    }

    @Test
    fun getCurrencyWithDefaultLocale() = runTest {
        val amount = 0.0
        val formattedAmount = getCurrency(
            defaultCurrency,
            amount,
            Locale.getDefault()
        )

        Truth.assertThat(formattedAmount).isNotNull()
        Truth.assertThat(formattedAmount).isEqualTo("0.0$")
    }
}