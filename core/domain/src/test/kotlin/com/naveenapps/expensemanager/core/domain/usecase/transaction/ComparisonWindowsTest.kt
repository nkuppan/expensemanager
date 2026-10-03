package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.model.DateRangeType
import kotlinx.datetime.TimeZone
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

class ComparisonWindowsTest {

    private val utc = TimeZone.UTC

    private fun millis(y: Int, m: Int, d: Int, h: Int = 0) =
        LocalDateTime.of(y, m, d, h, 0).toInstant(ZoneOffset.UTC).toEpochMilli()

    @Test
    fun `month in progress is compared with the same days of last month`() {
        val range = listOf(millis(2026, 10, 1), millis(2026, 11, 1))
        val now = millis(2026, 10, 3, 12)

        val windows = comparisonWindows(DateRangeType.THIS_MONTH, range, now, utc)!!

        assertThat(windows.isToDate).isTrue()
        assertThat(windows.currentEnd).isEqualTo(now)
        assertThat(windows.previousStart).isEqualTo(millis(2026, 9, 1))
        assertThat(windows.previousEnd).isEqualTo(millis(2026, 9, 3, 12))
    }

    @Test
    fun `finished month is compared with the whole previous calendar month`() {
        val range = listOf(millis(2026, 3, 1), millis(2026, 4, 1))
        val now = millis(2026, 10, 3)

        val windows = comparisonWindows(DateRangeType.THIS_MONTH, range, now, utc)!!

        assertThat(windows.isToDate).isFalse()
        assertThat(windows.previousStart).isEqualTo(millis(2026, 2, 1))
        assertThat(windows.previousEnd).isEqualTo(millis(2026, 3, 1))
    }

    @Test
    fun `to-date window never runs past the end of a shorter previous month`() {
        // 31 March 20:00 vs February (28 days): capped at 1 March.
        val range = listOf(millis(2026, 3, 1), millis(2026, 4, 1))
        val now = millis(2026, 3, 31, 20)

        val windows = comparisonWindows(DateRangeType.THIS_MONTH, range, now, utc)!!

        assertThat(windows.previousEnd).isEqualTo(millis(2026, 3, 1))
    }

    @Test
    fun `custom range is compared with the same length right before it`() {
        val range = listOf(millis(2026, 6, 10), millis(2026, 6, 20))
        val now = millis(2026, 10, 3)

        val windows = comparisonWindows(DateRangeType.CUSTOM, range, now, utc)!!

        assertThat(windows.previousStart).isEqualTo(millis(2026, 5, 31))
        assertThat(windows.previousEnd).isEqualTo(millis(2026, 6, 10))
    }

    @Test
    fun `all time and future periods have nothing to compare`() {
        val now = millis(2026, 10, 3)
        assertThat(comparisonWindows(DateRangeType.ALL, listOf(0L, now), now, utc)).isNull()
        assertThat(
            comparisonWindows(DateRangeType.THIS_MONTH, listOf(millis(2026, 11, 1), millis(2026, 12, 1)), now, utc),
        ).isNull()
    }
}
