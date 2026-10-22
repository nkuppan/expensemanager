package com.naveenapps.expensemanager.core.data.repository.export

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CsvSafeTextTest {

    @Test
    fun formulaLikeTextIsNeutralised() {
        assertThat("=HYPERLINK(\"http://x\")".toCsvSafeText()).isEqualTo("'=HYPERLINK(\"http://x\")")
        assertThat("+1".toCsvSafeText()).isEqualTo("'+1")
        assertThat("-2".toCsvSafeText()).isEqualTo("'-2")
        assertThat("@SUM(A1)".toCsvSafeText()).isEqualTo("'@SUM(A1)")
        assertThat("\tx".toCsvSafeText()).isEqualTo("'\tx")
    }

    @Test
    fun ordinaryTextIsUnchanged() {
        assertThat("Groceries, weekly".toCsvSafeText()).isEqualTo("Groceries, weekly")
        assertThat("".toCsvSafeText()).isEqualTo("")
    }
}
