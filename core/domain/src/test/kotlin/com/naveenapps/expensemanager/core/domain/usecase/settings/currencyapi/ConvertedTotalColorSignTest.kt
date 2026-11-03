package com.naveenapps.expensemanager.core.domain.usecase.settings.currencyapi

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.model.Amount
import org.junit.Test

class ConvertedTotalColorSignTest {

    @Test
    fun convertedValueIsUsedAsIs() {
        assertThat(ConvertedTotal(value = -12.5).colorSign()).isEqualTo(-12.5)
    }

    @Test
    fun withoutConversionAllNegativePartsAreNegative() {
        val total = ConvertedTotal(value = null, parts = listOf(Amount(-20.0), Amount(-10.0)))

        assertThat(total.colorSign()).isLessThan(0.0)
    }

    @Test
    fun withoutConversionAllPositivePartsArePositive() {
        val total = ConvertedTotal(value = null, parts = listOf(Amount(20.0), Amount(0.0)))

        assertThat(total.colorSign()).isGreaterThan(0.0)
    }

    @Test
    fun withoutConversionMixedPartsAreNeutral() {
        val total = ConvertedTotal(value = null, parts = listOf(Amount(20.0), Amount(-10.0)))

        assertThat(total.colorSign()).isEqualTo(0.0)
    }
}
