package com.naveenapps.expensemanager.feature.dashboard

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.model.DashboardSection
import com.naveenapps.expensemanager.core.model.DashboardSection.ACCOUNTS
import com.naveenapps.expensemanager.core.model.DashboardSection.BUDGETS
import com.naveenapps.expensemanager.core.model.DashboardSection.CATEGORIES
import com.naveenapps.expensemanager.core.model.DashboardSection.SUMMARY
import com.naveenapps.expensemanager.core.model.DashboardSection.TRANSACTIONS
import com.naveenapps.expensemanager.core.model.DashboardSection.UPCOMING
import org.junit.Test

class DashboardSectionOrderTest {

    @Test
    fun `nothing saved gives the default order`() {
        assertThat(DashboardSection.fromStored(null)).isEqualTo(DashboardSection.DEFAULT_ORDER)
        assertThat(DashboardSection.fromStored(emptyList())).isEqualTo(DashboardSection.DEFAULT_ORDER)
    }

    @Test
    fun `a saved order is kept exactly`() {
        val saved = listOf(UPCOMING, TRANSACTIONS, SUMMARY, BUDGETS, CATEGORIES, ACCOUNTS)

        assertThat(DashboardSection.fromStored(saved.map { it.name })).isEqualTo(saved)
    }

    @Test
    fun `unknown and duplicate names are dropped and missing sections are added at the end`() {
        // Saved by an older version that had no Upcoming card, plus a section since removed.
        val stored = listOf("BUDGETS", "OLD_SECTION", "SUMMARY", "BUDGETS")

        assertThat(DashboardSection.fromStored(stored))
            .containsExactly(BUDGETS, SUMMARY, ACCOUNTS, CATEGORIES, TRANSACTIONS, UPCOMING)
            .inOrder()
    }
}
