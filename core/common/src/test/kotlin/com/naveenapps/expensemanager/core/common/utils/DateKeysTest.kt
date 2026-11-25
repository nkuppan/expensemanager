package com.naveenapps.expensemanager.core.common.utils

import java.util.Date
import java.util.Locale
import java.util.TimeZone as JavaTimeZone
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * The kotlinx-datetime keys in DateKeys.kt must produce exactly what the SimpleDateFormat
 * versions in DateUtils.kt produce, because these strings are stored (`Budget.selectedMonth`) or
 * matched against stored ones. Both run side by side here over thousands of instants, in time
 * zones chosen to break date maths (+14, -11, half-hour and 45-minute offsets, half-hour DST,
 * a day without midnight) and default locales with other digits and calendars (Arabic,
 * Persian, Thai Buddhist, Japanese imperial). Delete the old functions only after this is green.
 */
class DateKeysTest {

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

    private val zones = listOf(
        "UTC", "Asia/Kolkata", "Asia/Kathmandu", "America/New_York", "America/Sao_Paulo",
        "Pacific/Auckland", "Pacific/Kiritimati", "Pacific/Pago_Pago", "Europe/London", "Australia/Lord_Howe",
    )

    private val locales = listOf(
        Locale.US,
        Locale.GERMANY,
        Locale.forLanguageTag("ar-EG"),
        Locale.forLanguageTag("hi-IN"),
        Locale.forLanguageTag("fa-IR"),
        Locale.forLanguageTag("th-TH-u-ca-buddhist"),
        Locale.forLanguageTag("ja-JP-u-ca-japanese"),
        Locale.SIMPLIFIED_CHINESE,
    )

    /** Every ~37 hours from 2018 to 2032, plus the millisecond either side of every month start. */
    private fun samples(zone: TimeZone): List<Instant> {
        val start = LocalDate(2018, 1, 1).atStartOfDayIn(zone).toEpochMilliseconds()
        val end = LocalDate(2033, 1, 1).atStartOfDayIn(zone).toEpochMilliseconds()
        val step = 37L * 60 * 60 * 1000 + 1234
        val regular = generateSequence(start) { it + step }.takeWhile { it < end }.map { Instant.fromEpochMilliseconds(it) }
        val edges = (2018..2032).flatMap { year ->
            (1..12).flatMap { month ->
                val first = LocalDate(year, month, 1).atStartOfDayIn(zone).toEpochMilliseconds()
                listOf(first - 1, first, first + 1).map { Instant.fromEpochMilliseconds(it) }
            }
        }
        return regular.toList() + edges
    }

    private fun forEachZoneAndLocale(block: (zoneId: String, TimeZone) -> Unit) {
        for (zoneId in zones) {
            for (locale in locales) {
                JavaTimeZone.setDefault(JavaTimeZone.getTimeZone(zoneId))
                Locale.setDefault(locale)
                block("$zoneId/${locale.toLanguageTag()}", TimeZone.of(zoneId))
            }
        }
    }

    @Test
    fun formattedKeysMatchTheOldFormatters() = forEachZoneAndLocale { where, zone ->
        for (instant in samples(zone)) {
            val old = Date(instant.toEpochMilliseconds())
            val day = instant.toLocalDateIn()
            assertEquals("month key $where $instant", old.toMonthAndYearKey(), day.toMonthAndYearKey())
            assertEquals("year key $where $instant", old.toYear(), day.toYearKey())
            assertEquals("day key $where $instant", old.toCompleteDateWithDate(), day.toDayKey())
        }
    }

    @Test
    fun periodBoundsMatchTheOldFunctions() = forEachZoneAndLocale { where, zone ->
        for (instant in samples(zone)) {
            val old = Date(instant.toEpochMilliseconds())
            val day = instant.toLocalDateIn()
            assertEquals("start of month $where $instant", old.getStartOfTheMonth(), day.startOfMonthMillis())
            assertEquals("end of month $where $instant", old.getEndOfTheMonth(), day.endOfMonthMillis())
            assertEquals("start of year $where $instant", old.getStartOfTheYear(), day.startOfYearMillis())
            assertEquals("end of year $where $instant", old.getEndOfTheYear(), day.endOfYearMillis())
        }
    }

    @Test
    fun parsedKeysMatchTheOldParsers() = forEachZoneAndLocale { where, _ ->
        for (year in listOf(2018, 2024, 2026, 2032)) {
            for (month in 1..12) {
                val key = LocalDate(year, month, 1).toMonthAndYearKey()
                val old = key.fromMonthAndYearKey()?.toKotlinLocalDate()
                assertEquals("parse $key $where", old, key.monthAndYearKeyToLocalDate())
                assertEquals("round trip $key", LocalDate(year, month, 1), key.monthAndYearKeyToLocalDate())
            }
            val yearKey = LocalDate(year, 6, 15).toYearKey()
            assertEquals("parse $yearKey $where", yearKey.fromYear()?.toKotlinLocalDate(), yearKey.yearKeyToLocalDate())
        }
    }

    @Test
    fun invalidKeysAreRejected() {
        for (text in listOf("garbage", "", "  ", "2025", "March", "Marchx 2026", "March 20x6", "13 2026")) {
            assertNull("month key '$text'", text.monthAndYearKeyToLocalDate())
        }
        for (text in listOf("garbage", "", "20x5", "March 2026")) {
            assertNull("year key '$text'", text.yearKeyToLocalDate())
        }
    }
}
