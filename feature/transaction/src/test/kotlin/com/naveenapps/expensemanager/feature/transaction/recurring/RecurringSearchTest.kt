package com.naveenapps.expensemanager.feature.transaction.recurring

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.RecurringFrequency
import com.naveenapps.expensemanager.core.model.RecurringTransaction
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.model.TransactionType
import java.util.Date
import org.junit.Test

class RecurringSearchTest {

    private val item = RecurringItemUi(
        rule = RecurringTransaction(
            id = "r",
            notes = "Netflix",
            categoryId = "c",
            fromAccountId = "a",
            toAccountId = null,
            type = TransactionType.EXPENSE,
            amount = 1200.0,
            frequency = RecurringFrequency.MONTHLY,
            startDate = Date(),
            nextOccurrence = 1,
            nextDueDate = Date(),
            createdOn = Date(),
            updatedOn = Date(),
            category = Category(
                id = "c",
                name = "Entertainment",
                type = CategoryType.EXPENSE,
                storedIcon = StoredIcon(name = "ic", backgroundColor = "#000000"),
                createdOn = Date(),
                updatedOn = Date(),
            ),
            fromAccount = Account(
                id = "a",
                name = "Credit Card",
                type = AccountType.CREDIT,
                storedIcon = StoredIcon(name = "ic", backgroundColor = "#000000"),
                createdOn = Date(),
                updatedOn = Date(),
            ),
        ),
        amount = "$1,200.00",
    )

    @Test
    fun `blank query matches everything`() {
        assertThat(item.matches("  ")).isTrue()
    }

    @Test
    fun `matches notes category account and amount case-insensitively`() {
        assertThat(item.matches("netfl")).isTrue()
        assertThat(item.matches("ENTERTAIN")).isTrue()
        assertThat(item.matches("credit")).isTrue()
        assertThat(item.matches("1,200")).isTrue()
        assertThat(item.matches("1200")).isTrue()
    }

    @Test
    fun `does not match unrelated text`() {
        assertThat(item.matches("groceries")).isFalse()
    }

    // A built-in category is shown translated (e.g. in Hindi) but stores an English name.
    private val translatedName: (Category) -> String = { if (it.name == "Entertainment") "मनोरंजन" else it.name }

    @Test
    fun matchesTheTranslatedCategoryNameShown() {
        assertThat(item.matches("मनोरंजन", translatedName)).isTrue()
    }

    @Test
    fun stillMatchesTheStoredEnglishNameWhenTranslated() {
        assertThat(item.matches("entertain", translatedName)).isTrue()
    }

    @Test
    fun translatedNameDoesNotMatchUnrelatedText() {
        assertThat(item.matches("किराया", translatedName)).isFalse()
    }
}
