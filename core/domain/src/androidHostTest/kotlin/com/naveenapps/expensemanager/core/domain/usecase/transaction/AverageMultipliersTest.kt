package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.google.common.truth.Truth.assertWithMessage
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.repository.CalendarWeeksRepository
import java.util.Calendar
import java.util.Locale
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import org.junit.After
import org.junit.Test

/** [averageMultipliers] must give exactly what the old `Calendar.getActualMaximum` code gave (KMP groundwork C3). */
class AverageMultipliersTest {

    private val originalZone = java.util.TimeZone.getDefault()
    private val originalLocale = Locale.getDefault()

    @After
    fun restore() {
        java.util.TimeZone.setDefault(originalZone)
        Locale.setDefault(originalLocale)
    }

    /** Same as the Android implementation. */
    private val weeks = object : CalendarWeeksRepository {
        override fun weeksInMonthOf(at: Instant) = calendarAt(at.toEpochMilliseconds()).getActualMaximum(Calendar.WEEK_OF_MONTH)
        override fun weeksInYearOf(at: Instant) = calendarAt(at.toEpochMilliseconds()).getActualMaximum(Calendar.WEEK_OF_YEAR)
    }

    private fun calendarAt(millis: Long) = Calendar.getInstance().apply { timeInMillis = millis }

    /** The body of `GetAverageDataUseCase` before the change. */
    private fun legacy(type: DateRangeType, millis: Long): Triple<Double, Double, Double> {
        val calendar = calendarAt(millis)
        return when (type) {
            DateRangeType.TODAY -> Triple(
                1.0,
                calendar.getActualMaximum(Calendar.DAY_OF_WEEK).toDouble(),
                calendar.getActualMaximum(Calendar.DAY_OF_MONTH).toDouble(),
            )

            DateRangeType.THIS_WEEK -> Triple(
                1.0 / calendar.getActualMaximum(Calendar.DAY_OF_WEEK),
                1.0,
                calendar.getActualMaximum(Calendar.WEEK_OF_MONTH).toDouble(),
            )

            DateRangeType.THIS_MONTH -> Triple(
                1.0 / calendar.getActualMaximum(Calendar.DAY_OF_MONTH),
                1.0 / calendar.getActualMaximum(Calendar.WEEK_OF_MONTH),
                1.0,
            )

            DateRangeType.THIS_YEAR, DateRangeType.CUSTOM, DateRangeType.ALL -> Triple(
                1.0 / calendar.getActualMaximum(Calendar.DAY_OF_YEAR),
                1.0 / calendar.getActualMaximum(Calendar.WEEK_OF_YEAR),
                1.0 / 12,
            )
        }
    }

    @Test
    fun multipliersMatchTheCalendarVersion() {
        val zones = listOf("UTC", "Asia/Kolkata", "America/New_York", "Pacific/Auckland", "Pacific/Kiritimati", "Pacific/Pago_Pago")
        val locales = listOf(Locale.US, Locale.GERMANY, Locale.forLanguageTag("ar-EG"), Locale.forLanguageTag("hi-IN"))
        for (zoneId in zones) {
            for (locale in locales) {
                java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone(zoneId))
                Locale.setDefault(locale)
                var millis = 1_514_764_800_000L // 2018-01-01T00:00Z
                val end = 1_830_297_600_000L // 2028-01-01T00:00Z
                while (millis < end) {
                    for (type in DateRangeType.entries) {
                        val expected = legacy(type, millis)
                        val actual = averageMultipliers(type, Instant.fromEpochMilliseconds(millis), weeks, TimeZone.of(zoneId))
                        assertWithMessage("$zoneId $locale $type at $millis")
                            .that(Triple(actual.perDay, actual.perWeek, actual.perMonth))
                            .isEqualTo(expected)
                    }
                    millis += 61L * 60 * 60 * 1000 + 7L * 60 * 1000 // ~61 h: crosses every month and year boundary
                }
            }
        }
    }
}
