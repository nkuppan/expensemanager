package com.naveenapps.expensemanager.core.designsystem.ui.components

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SignedAmountTextTest {

    private val ltrMark = "‎"
    private val lira = "ليرة"

    @Test
    fun `no sign leaves the amount unchanged`() {
        assertThat(signedAmountText("", "\$100")).isEqualTo("\$100")
    }

    @Test
    fun `sign symbol in prefix keeps the sign in front`() {
        assertThat(signedAmountText("−", "\$100")).isEqualTo("−\$100")
    }

    @Test
    fun `sign symbol in suffix keeps the sign in front`() {
        assertThat(signedAmountText("+", "1,200€")).isEqualTo("+1,200€")
    }

    @Test
    fun `word symbol in prefix puts the sign on the number`() {
        assertThat(signedAmountText("−", "$ltrMark$lira$ltrMark 208"))
            .isEqualTo("$ltrMark$lira$ltrMark −208")
    }

    @Test
    fun `word symbol in suffix puts the sign before the number`() {
        assertThat(signedAmountText("−", "${ltrMark}208 $lira$ltrMark"))
            .isEqualTo("−${ltrMark}208 $lira$ltrMark")
    }

    @Test
    fun `text without digits just gets the sign in front`() {
        assertThat(signedAmountText("−", "")).isEqualTo("−")
    }
}
