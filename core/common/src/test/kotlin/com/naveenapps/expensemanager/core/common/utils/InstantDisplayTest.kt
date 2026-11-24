package com.naveenapps.expensemanager.core.common.utils

import com.naveenapps.expensemanager.core.foundation.monthAndYearKeyToLocalDate
import com.naveenapps.expensemanager.core.foundation.toMonthAndYearKey
import com.naveenapps.expensemanager.core.foundation.toYearKey
import com.naveenapps.expensemanager.core.foundation.yearKeyToLocalDate
import java.util.Date
import java.util.Locale
import java.util.TimeZone as JavaTimeZone
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * The Instant overloads in InstantDisplay.kt and DateKeys.kt give exactly what the java.util.Date
 * versions gave, in every zone and app language (KMP groundwork C5). Screens, exports and stored
 * keys switched from one to the other, so any difference would be a visible change.
 */
class InstantDisplayTest {

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

    private val noDst = listOf("UTC", "Asia/Kolkata", "Asia/Kathmandu")
    private val zones = noDst + listOf(
        "America/New_York",
        "America/Sao_Paulo",
        "Europe/London",
        "Pacific/Auckland",
        "Australia/Lord_Howe",
    )
    private val locales = listOf(
        Locale.US,
        Locale.GERMANY,
        Locale.SIMPLIFIED_CHINESE,
        Locale.forLanguageTag("ar-EG"),
        Locale.forLanguageTag("hi-IN"),
        Locale.forLanguageTag("fa-IR"),
        Locale.forLanguageTag("th-TH-u-ca-buddhist"),
        Locale.forLanguageTag("ja-JP-u-ca-japanese"),
    )

    private fun inEveryZoneAndLocale(block: (zoneId: String, locale: Locale) -> Unit) {
        zones.forEach { zoneId ->
            locales.forEach { locale ->
                JavaTimeZone.setDefault(JavaTimeZone.getTimeZone(zoneId))
                Locale.setDefault(locale)
                block(zoneId, locale)
            }
        }
    }

    /** Every ~37 hours from 2018 to 2030, so every month start/end and time of day comes round. */
    private fun samples(): Sequence<Long> = generateSequence(1_514_764_800_000L) { it + 37L * 3_600_000 + 61_000 }
        .takeWhile { it < 1_893_456_000_000L }

    @Test
    fun formattedTextMatchesTheDateVersions() {
        inEveryZoneAndLocale { zoneId, locale ->
            samples().forEach { millis ->
                val date = Date(millis)
                val instant = Instant.fromEpochMilliseconds(millis)
                val where = "$zoneId $locale $millis"
                assertEquals(where, date.toDate(), instant.toDate())
                assertEquals(where, date.toDateAndMonth(), instant.toDateAndMonth())
                assertEquals(where, date.toCompleteDate(), instant.toCompleteDate())
                assertEquals(where, date.toCompleteDateWithDate(), instant.toCompleteDateWithDate())
                assertEquals(where, date.toMonthAndYear(), instant.toMonthAndYear())
                assertEquals(where, date.toMonthYear(), instant.toMonthYear())
                assertEquals(where, date.toMonth(), instant.toMonth())
                assertEquals(where, date.toYear(), instant.toYear())
                assertEquals(where, date.toYearInt(), instant.toYearInt())
                assertEquals(where, date.toDay(), instant.toDay())
                assertEquals(where, date.toTimeAndMinutes(), instant.toTimeAndMinutes())
                assertEquals(where, date.toTimeAndMinutesWithAMPM(), instant.toTimeAndMinutesWithAMPM())
            }
        }
    }

    @Test
    fun periodBoundsMatchTheDateVersions() {
        inEveryZoneAndLocale { zoneId, locale ->
            samples().forEach { millis ->
                val date = Date(millis)
                val instant = Instant.fromEpochMilliseconds(millis)
                val where = "$zoneId $locale $millis"
                assertEquals(where, date.getStartOfTheMonth(), instant.getStartOfTheMonth())
                assertEquals(where, date.getEndOfTheMonth(), instant.getEndOfTheMonth())
                assertEquals(where, date.getStartOfTheYear(), instant.getStartOfTheYear())
                assertEquals(where, date.getEndOfTheYear(), instant.getEndOfTheYear())
            }
        }
    }

    /** Budget.selectedMonth and yearly budget keys: stored, then matched, so they must not change. */
    @Test
    fun storedKeysMatchTheDateVersions() {
        inEveryZoneAndLocale { zoneId, locale ->
            samples().forEach { millis ->
                val date = Date(millis)
                val instant = Instant.fromEpochMilliseconds(millis)
                val where = "$zoneId $locale $millis"
                assertEquals(where, date.toMonthAndYearKey(), instant.toMonthAndYearKey())
                assertEquals(where, date.toYear(), instant.toYearKey())
            }
        }
    }

    /** The date picker's UTC-midnight millis → the start of that day in the device zone. */
    @Test
    fun datePickerStartOfDayMatchesTheDateVersion() {
        zones.forEach { zoneId ->
            JavaTimeZone.setDefault(JavaTimeZone.getTimeZone(zoneId))
            var utcMidnight = 1_514_764_800_000L
            while (utcMidnight < 1_893_456_000_000L) {
                assertEquals(
                    "$zoneId $utcMidnight",
                    utcMidnight.toExactStartOfTheDay().time,
                    utcMidnight.toExactStartOfTheDayInstant().toEpochMilliseconds(),
                )
                utcMidnight += 86_400_000L
            }
        }
    }

    /**
     * The budget editor's month and year pickers, and loading a budget's saved key, now build
     * the start of the month or year from kotlinx-datetime instead of parsing text with
     * SimpleDateFormat. Same local time everywhere; same instant where there's no daylight saving.
     */
    @Test
    fun budgetPeriodStartsMatchTheOldParsers() {
        zones.forEach { zoneId ->
            locales.forEach { locale ->
                JavaTimeZone.setDefault(JavaTimeZone.getTimeZone(zoneId))
                Locale.setDefault(locale)
                val zone = TimeZone.of(zoneId)
                for (year in 2015..2032) {
                    val yearKey = year.toString()
                    val yearWhere = "$zoneId $locale year $year"
                    assertSameStart(yearWhere, zoneId, zone, yearKey.fromYear()!!, yearKey.yearKeyToLocalDate()!!)
                    for (month in 1..12) {
                        val where = "$zoneId $locale $year-$month"
                        val first = LocalDate(year, month, 1)
                        val picked = "$month-$year".fromShortMonthAndYearToDate()!!
                        assertSameStart(where, zoneId, zone, picked, first)
                        val key = first.toMonthAndYearKey()
                        val loaded = key.fromMonthAndYearKey()!!
                        assertSameStart("$where key", zoneId, zone, loaded, key.monthAndYearKeyToLocalDate()!!)
                    }
                }
            }
        }
    }

    private fun assertSameStart(where: String, zoneId: String, zone: TimeZone, old: Date, newDay: LocalDate) {
        val new = newDay.atStartOfDayIn(zone)
        val oldLocal = Instant.fromEpochMilliseconds(old.time).toLocalDateTime(zone)
        assertEquals("$where (local time)", oldLocal, new.toLocalDateTime(zone))
        if (zoneId in noDst) assertEquals("$where (instant)", old.time, new.toEpochMilliseconds())
    }
}
