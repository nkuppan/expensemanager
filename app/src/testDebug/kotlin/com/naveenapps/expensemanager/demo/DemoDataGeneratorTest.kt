package com.naveenapps.expensemanager.demo

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.model.TransactionType
import java.util.Calendar
import java.util.Date
import org.junit.Test

class DemoDataGeneratorTest {

    private fun date(year: Int, month: Int, day: Int, hour: Int = 12): Date = Calendar.getInstance().apply {
        set(year, month, day, hour, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

    @Test
    fun `nothing is dated in the future`() {
        val today = date(2026, Calendar.OCTOBER, 14, hour = 10)
        val plan = DemoDataGenerator.generate(today = today, months = 3)
        assertThat(plan.transactions.maxOf { it.date }).isAtMost(today)
    }

    @Test
    fun `covers the requested months and is sorted`() {
        val today = date(2026, Calendar.OCTOBER, 20)
        val plan = DemoDataGenerator.generate(today = today, months = 3)
        val months = plan.transactions.map { Calendar.getInstance().apply { time = it.date }.get(Calendar.MONTH) }.toSet()
        assertThat(months).containsExactly(Calendar.AUGUST, Calendar.SEPTEMBER, Calendar.OCTOBER)
        assertThat(plan.transactions.map { it.date }).isInOrder()
        assertThat(plan.budgets).hasSize(9)
    }

    @Test
    fun `same input gives the same data`() {
        val today = date(2026, Calendar.OCTOBER, 20)
        assertThat(DemoDataGenerator.generate(today)).isEqualTo(DemoDataGenerator.generate(today))
    }

    @Test
    fun `bank and cash never go negative`() {
        listOf(1, 9, 20, 28).forEach { day ->
            val plan = DemoDataGenerator.generate(today = date(2026, Calendar.OCTOBER, day), months = 3)
            val balance = plan.accounts.associate { it.id to it.openingUsd }.toMutableMap()
            plan.transactions.forEach { t ->
                when (t.type) {
                    TransactionType.INCOME -> balance[t.fromAccountId] = balance.getValue(t.fromAccountId) + t.amountUsd

                    TransactionType.EXPENSE -> balance[t.fromAccountId] = balance.getValue(t.fromAccountId) - t.amountUsd

                    TransactionType.TRANSFER -> {
                        balance[t.fromAccountId] = balance.getValue(t.fromAccountId) - t.amountUsd
                        balance[t.toAccountId!!] = balance.getValue(t.toAccountId!!) + t.amountUsd
                    }
                }
                assertThat(balance.getValue(DemoAccountId.BANK)).isAtLeast(0.0)
                assertThat(balance.getValue(DemoAccountId.CASH)).isAtLeast(0.0)
                assertThat(balance.getValue(DemoAccountId.CARD)).isAtLeast(-3_000.0)
            }
        }
    }

    @Test
    fun `each repeating item becomes exactly one recurring rule`() {
        val plan = DemoDataGenerator.generate(today = date(2026, Calendar.OCTOBER, 20), months = 3)
        // salary, internet, streaming, gym
        assertThat(plan.transactions.count { it.repeat != null }).isEqualTo(4)
    }

    @Test
    fun `currency amounts are rounded to sensible steps`() {
        assertThat(DemoCurrency.convert(54.37, "USD", 2)).isEqualTo(54.37)
        assertThat(DemoCurrency.convert(54.37, "JPY", 0)).isEqualTo(8_160.0)
        assertThat(DemoCurrency.convert(54.37, "INR", 2)).isEqualTo(4_570.0)
        assertThat(DemoCurrency.convert(54.37, "IDR", 0)).isEqualTo(870_000.0)
        assertThat(DemoCurrency.convert(54.37, "XYZ", 2)).isEqualTo(54.37)
    }
}
