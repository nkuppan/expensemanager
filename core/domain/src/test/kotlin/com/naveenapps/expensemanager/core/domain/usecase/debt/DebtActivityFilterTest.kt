package com.naveenapps.expensemanager.core.domain.usecase.debt

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.model.DebtEntry
import com.naveenapps.expensemanager.core.model.DebtEntryKind
import com.naveenapps.expensemanager.core.model.TransactionType
import java.util.Date
import org.junit.Test

class DebtActivityFilterTest {

    private fun entry(id: String, accountId: String?, date: Long) = DebtEntry(
        id = id,
        personId = "ravi",
        kind = DebtEntryKind.LENT,
        amountMilli = 1_000,
        accountId = accountId,
        date = Date(date),
        dueDate = null,
        note = "",
        createdOn = Date(date),
        updatedOn = Date(date),
    )

    private val entries = listOf(
        entry("cash-early", "cash", 1_000),
        entry("cash-late", "cash", 9_000),
        entry("bank", "bank", 5_000),
        entry("no-account", null, 5_000),
    )

    private val noFilter = DebtActivityFilter(emptyList(), emptyList(), emptyList(), range = null)

    @Test
    fun onlyEntriesThatMovedMoneyInAnAccountAreListed() {
        assertThat(filterDebtActivity(entries, noFilter).map { it.id })
            .containsExactly("cash-early", "cash-late", "bank")
    }

    @Test
    fun followsTheAccountFilterAndPeriod() {
        val filter = noFilter.copy(accounts = listOf("cash"), range = 0L..5_000L)

        assertThat(filterDebtActivity(entries, filter).map { it.id }).containsExactly("cash-early")
    }

    @Test
    fun hiddenWhileFilteringByCategoryOrType() {
        assertThat(filterDebtActivity(entries, noFilter.copy(categories = listOf("food")))).isEmpty()
        assertThat(filterDebtActivity(entries, noFilter.copy(transactionTypes = listOf(TransactionType.EXPENSE))))
            .isEmpty()
    }
}
