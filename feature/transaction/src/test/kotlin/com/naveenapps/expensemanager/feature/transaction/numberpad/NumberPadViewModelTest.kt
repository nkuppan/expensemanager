package com.naveenapps.expensemanager.feature.transaction.numberpad

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NumberPadViewModelTest : BaseCoroutineTest() {

    private val viewModel = NumberPadViewModel()

    private fun kotlinx.coroutines.test.TestScope.type(vararg keys: String) {
        keys.forEach { viewModel.appendString(it) }
        advanceUntilIdle()
    }

    @Test
    fun repeatedDigits_from11To99_areAllowed() = runTest {
        for (digit in 1..9) {
            viewModel.clearAmount()
            type("$digit", "$digit")
            assertThat(viewModel.calculatedAmountString.value).isEqualTo("$digit$digit")
        }
    }

    @Test
    fun repeatedDigits_moreThanTwice_areAllowed() = runTest {
        type("1", "1", "1")
        assertThat(viewModel.calculatedAmountString.value).isEqualTo("111")
    }

    @Test
    fun leadingZero_doesNotAcceptMoreZeros() = runTest {
        type("0", "0", "00")
        assertThat(viewModel.calculatedAmountString.value).isEqualTo("0")
    }

    @Test
    fun leadingZero_isReplacedByNextDigit() = runTest {
        type("0", "5")
        assertThat(viewModel.calculatedAmountString.value).isEqualTo("5")
    }

    @Test
    fun zerosAfterNonZeroDigit_areAllowed() = runTest {
        type("1", "0", "00")
        assertThat(viewModel.calculatedAmountString.value).isEqualTo("1000")
    }

    @Test
    fun zeroAfterDecimal_isAllowed() = runTest {
        type("0", ".", "0", "5")
        assertThat(viewModel.calculatedAmountString.value).isEqualTo("0.05")
    }

    @Test
    fun repeatedDigits_afterOperator_areAllowed() = runTest {
        type("1", "+", "2", "2")
        assertThat(viewModel.calculatedAmountString.value).isEqualTo("1+22")
        assertThat(viewModel.calculatedAmount.value).isEqualTo(String.format("%.2f", 23.0))
    }

    @Test
    fun backspaceOnLastDigit_resetsAmountToZero() = runTest {
        type("1")
        assertThat(viewModel.calculatedAmount.value).isEqualTo(String.format("%.2f", 1.0))

        type("")
        assertThat(viewModel.calculatedAmountString.value).isEmpty()
        assertThat(viewModel.calculatedAmount.value).isEqualTo("0")
    }

    @Test
    fun backspaceAllDigits_resetsAmountToZero() = runTest {
        type("2", "5", "", "")
        assertThat(viewModel.calculatedAmountString.value).isEmpty()
        assertThat(viewModel.calculatedAmount.value).isEqualTo("0")
    }

    @Test
    fun backspaceOneDigit_recalculatesAmount() = runTest {
        type("2", "5", "")
        assertThat(viewModel.calculatedAmountString.value).isEqualTo("2")
        assertThat(viewModel.calculatedAmount.value).isEqualTo(String.format("%.2f", 2.0))
    }

    @Test
    fun typingAfterBackspacingEverything_startsFresh() = runTest {
        type("1", "", "7")
        assertThat(viewModel.calculatedAmountString.value).isEqualTo("7")
        assertThat(viewModel.calculatedAmount.value).isEqualTo(String.format("%.2f", 7.0))
    }

    @Test
    fun backspaceOnEmptyPad_keepsZero() = runTest {
        type("")
        assertThat(viewModel.calculatedAmountString.value).isEmpty()
        assertThat(viewModel.calculatedAmount.value).isEqualTo("0")
    }
}
