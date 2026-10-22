package com.naveenapps.expensemanager.core.data.utils

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.common.utils.fromCompleteDate
import com.naveenapps.expensemanager.core.common.utils.toCompleteDateWithDate
import com.naveenapps.expensemanager.core.common.utils.toMonthAndYear
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.junit.Test

/** Lives in core:data because core:common has no unit-test setup. */
class DateUtilsThreadSafetyTest {

    private fun day(year: Int, month: Int, day: Int): Date = Calendar.getInstance().apply {
        clear()
        set(year, month - 1, day)
    }.time

    @Test
    fun formattingAndParsingFromManyThreadsRoundTrips() {
        val dates = (1..28).map { day(2026, (it % 12) + 1, it) }
        val pool = Executors.newFixedThreadPool(8)
        try {
            val results = pool.invokeAll(
                (1..8).map {
                    Callable {
                        repeat(500) {
                            dates.forEach { date ->
                                val text = date.toCompleteDateWithDate()
                                check(text.fromCompleteDate() == date) { "Round trip failed for $text" }
                            }
                        }
                    }
                },
            )
            results.forEach { it.get(30, TimeUnit.SECONDS) }
        } finally {
            pool.shutdownNow()
        }
    }

    @Test
    fun localizedFormatsFollowTheCurrentDefaultLocale() {
        val original = Locale.getDefault()
        try {
            val date = day(2026, 3, 15)
            Locale.setDefault(Locale.US)
            assertThat(date.toMonthAndYear()).isEqualTo("March 2026")
            Locale.setDefault(Locale.GERMANY)
            assertThat(date.toMonthAndYear()).isEqualTo("März 2026")
        } finally {
            Locale.setDefault(original)
        }
    }
}
