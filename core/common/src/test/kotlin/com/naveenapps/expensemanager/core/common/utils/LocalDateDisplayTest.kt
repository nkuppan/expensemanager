package com.naveenapps.expensemanager.core.common.utils

import java.util.Locale
import java.util.TimeZone as JavaTimeZone
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * `LocalDate.toMonthAndYear()` (the chart's month label since core:domain returns dates) gives the
 * same text as `Instant.toMonthAndYear()` did for every instant of that month, in every zone and
 * app language (OpenSpec change make-core-domain-multiplatform, task 3.2).
 */
class LocalDateDisplayTest {

    private lateinit var originalZone: JavaTimeZone
    private lateinit var originalLocale: Locale

    @Before
    fun save() {
        originalZone = JavaTimeZone.getDefault()
        originalLocale = Locale.getDefault()
    }

    @After
    fun restore() {
        JavaTimeZone.setDefault(originalZone)
        Locale.setDefault(originalLocale)
    }

    private val zones = listOf("UTC", "Asia/Kolkata", "America/New_York", "America/Sao_Paulo", "Pacific/Chatham")
    private val locales = listOf(
        Locale.US,
        Locale.GERMANY,
        Locale.forLanguageTag("ar-EG"),
        Locale.forLanguageTag("th-TH-u-ca-buddhist"),
    )

    @Test
    fun monthLabelMatchesTheInstantVersionForTheWholeMonth() {
        zones.forEach { zoneId ->
            locales.forEach { locale ->
                JavaTimeZone.setDefault(JavaTimeZone.getTimeZone(zoneId))
                Locale.setDefault(locale)
                val zone = TimeZone.of(zoneId)
                for (year in 1990..2040) {
                    for (month in 1..12) {
                        val first = LocalDate(year, month, 1)
                        val last = first.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
                        val firstInstant = first.atStartOfDayIn(zone)
                        val lastInstant = LocalDateTime(last, LocalTime(23, 59, 59, 999_000_000)).toInstant(zone)
                        val where = "$zoneId $locale $first"
                        assertEquals(where, firstInstant.toMonthAndYear(), first.toMonthAndYear())
                        assertEquals(where, lastInstant.toMonthAndYear(), last.toMonthAndYear())
                        assertEquals(where, first.toMonthAndYear(), last.toMonthAndYear())
                    }
                }
            }
        }
    }
}
