package com.naveenapps.expensemanager.core.common.utils

import com.google.common.truth.Truth
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Test

private const val ONE_DAY_MILLISECONDS = 86400000L
private val AUSTRALIA_TIME_ZONE = TimeZone.of("Australia/Hobart")
private val BRAZIL_TIME_ZONE = TimeZone.of("Brazil/East")
private val DEFAULT_TIME = TimeZone.currentSystemDefault()

class DateUtilsTest {

    @Test
    fun whenExactDateShouldAlwaysReturnThatDateStarting() {
        Truth.assertThat(
            "10/01/2024".fromCompleteDate().time.toExactStartOfTheDay().toCompleteDateWithDate()
        ).isEqualTo("10/01/2024")
    }

    @Test
    fun testingTimeZonShift() {
        val now = Clock.System.now()
        val actual = now.toLocalDateTime(AUSTRALIA_TIME_ZONE).date
        val expected = now.toLocalDateTime(BRAZIL_TIME_ZONE).date
        Truth.assertThat(actual).isNotEqualTo(expected)
    }

    @Test
    fun checkTodayRangeDifferenceAndSizeWithDefaultTimeZone() {
        val ranges = getTodayRange()
        Truth.assertThat(ranges).isNotEmpty()
        Truth.assertThat(ranges).hasSize(2)
        Truth.assertThat(ranges[1] - ranges[0]).isEqualTo(ONE_DAY_MILLISECONDS)
    }

    @Test
    fun checkThisWeekRangeDifferenceAndSizeWithDefaultTimeZone() {
        val ranges = getThisWeekRange()
        Truth.assertThat(ranges).isNotEmpty()
        Truth.assertThat(ranges).hasSize(2)
        Truth.assertThat(ranges[1] - ranges[0]).isEqualTo(7 * ONE_DAY_MILLISECONDS)
    }

    @Test
    fun checkThisMonthRangeDifferenceAndSizeWithDefaultTimeZone() {
        val ranges = getThisMonthRange()
        Truth.assertThat(ranges).isNotEmpty()
        Truth.assertThat(ranges).hasSize(2)
        val maxDays =
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).month.maxLength()
        Truth.assertThat(ranges[1] - ranges[0]).isEqualTo(maxDays.toLong() * ONE_DAY_MILLISECONDS)
    }

    @Test
    fun checkThisMonthRangeDifferenceAndSizeWithBrazilTimeZone() {
        val ranges = getThisMonthRange(BRAZIL_TIME_ZONE)
        Truth.assertThat(ranges).isNotEmpty()
        Truth.assertThat(ranges).hasSize(2)
        val maxDays = Clock.System.now().toLocalDateTime(BRAZIL_TIME_ZONE).month.maxLength()
        Truth.assertThat(ranges[1] - ranges[0]).isEqualTo(maxDays.toLong() * ONE_DAY_MILLISECONDS)
    }

    @Test
    fun checkThisYearRangeDifferenceAndSizeWithBrazilTimeZone() {
        val ranges = getThisYearRange(DEFAULT_TIME)
        Truth.assertThat(ranges).isNotEmpty()
        Truth.assertThat(ranges).hasSize(2)
        val year = Clock.System.now().toLocalDateTime(DEFAULT_TIME).year
        Truth.assertThat(ranges[1] - ranges[0])
            .isEqualTo(year.getNumberOfDays() * ONE_DAY_MILLISECONDS)
    }
}

fun Int.isLeapYear(): Boolean {
    return (this % 4 == 0 && this % 100 != 0) || (this % 400 == 0)
}

fun Int.getNumberOfDays(): Int {
    return if (this.isLeapYear()) {
        366
    } else {
        365
    }
}