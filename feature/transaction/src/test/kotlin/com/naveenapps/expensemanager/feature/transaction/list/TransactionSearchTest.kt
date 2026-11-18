package com.naveenapps.expensemanager.feature.transaction.list

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.testing.FAKE_CATEGORY
import com.naveenapps.expensemanager.core.testing.FAKE_EXPENSE_TRANSACTION
import org.junit.Test

/** Search on the transaction list matches the category name the user sees, not only the stored one. */
class TransactionSearchTest {

    // A built-in category: stored in English, shown translated (here, Hindi).
    private val food = FAKE_CATEGORY.copy(name = "Food")
    private val transaction = FAKE_EXPENSE_TRANSACTION.copy(notes = "", category = food)
    private val hindiName: (Category) -> String = { if (it.name == "Food") "खाना" else it.name }

    @Test
    fun matchesTheTranslatedCategoryNameShown() {
        assertThat(transaction.matches("खाना", hindiName)).isTrue()
    }

    @Test
    fun stillMatchesTheStoredEnglishName() {
        assertThat(transaction.matches("foo", hindiName)).isTrue()
    }

    @Test
    fun withoutATranslationOnlyTheStoredNameMatches() {
        assertThat(transaction.matches("खाना")).isFalse()
        assertThat(transaction.matches("food")).isTrue()
    }

    @Test
    fun unrelatedTextDoesNotMatch() {
        assertThat(transaction.matches("किराया", hindiName)).isFalse()
    }
}
