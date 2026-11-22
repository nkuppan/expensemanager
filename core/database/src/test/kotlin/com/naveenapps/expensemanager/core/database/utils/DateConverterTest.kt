package com.naveenapps.expensemanager.core.database.utils

import java.util.Date
import java.util.TimeZone
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * Pins exactly what [DateConverter] writes to and reads from the database, before the dates move
 * from java.util.Date to kotlin.time / kotlinx.datetime (KMP groundwork C1).
 *
 * The stored value is not an epoch timestamp: it is the device's local wall-clock time written as
 * if it were UTC ("floating" local time). Reading turns that wall-clock time back into an instant in
 * whatever time zone the device has at that moment. Any replacement must keep every value below
 * bit-identical, or existing rows would silently move to other days and budgets.
 *
 * Expected numbers were computed independently (Python zoneinfo, IANA tz data), not with this code.
 * Daylight-saving edges follow java.time: a wall-clock time inside a spring-forward gap moves later by
 * the gap, and one inside a fall-back overlap takes the earlier (summer) offset.
 */
class DateConverterTest {

    private lateinit var originalZone: TimeZone

    @Before
    fun saveZone() {
        originalZone = TimeZone.getDefault()
    }

    @After
    fun restoreZone() {
        TimeZone.setDefault(originalZone)
    }

    private fun inZone(zoneId: String) = TimeZone.setDefault(TimeZone.getTimeZone(zoneId))

    private fun store(epochMillis: Long): Long? = DateConverter.dateToTimestamp(Date(epochMillis))

    private fun read(stored: Long): Long? = DateConverter.fromTimestamp(stored)?.time

    // --- writing: local wall-clock time, labelled UTC -------------------------------------------

    @Test
    fun utcStoresTheInstantUnchanged() {
        inZone("UTC")
        // 2026-03-15 10:30 UTC
        assertEquals(1773570600000L, store(1773570600000L))
    }

    @Test
    fun halfHourOffsetStoresLocalWallClock() {
        inZone("Asia/Kolkata")
        // 2026-03-15 10:30 IST (05:00 UTC) is stored as 10:30 "UTC"
        assertEquals(1773570600000L, store(1773550800000L))
    }

    @Test
    fun negativeOffsetKeepsTheLocalDateWhenUtcIsAlreadyTomorrow() {
        inZone("America/New_York")
        // 2026-01-31 23:45 EST (2026-02-01 04:45 UTC) stays on 31 January
        assertEquals(1769903100000L, store(1769921100000L))
    }

    @Test
    fun quarterHourOffsetAtTheLastMillisecondOfTheYear() {
        inZone("Asia/Kathmandu")
        // 2026-12-31 23:59:59.999 NPT
        assertEquals(1798761599999L, store(1798740899999L))
    }

    @Test
    fun summerTimeAcrossTheDateLineStoresLocalMidnight() {
        inZone("Pacific/Auckland")
        // 2026-01-01 00:00 NZDT (UTC+13, still 2025-12-31 in UTC)
        assertEquals(1767225600000L, store(1767178800000L))
    }

    @Test
    fun millisecondsArePreserved() {
        inZone("America/Los_Angeles")
        // 2026-07-04 18:05:07.123 PDT
        assertEquals(1783188307123L, store(1783213507123L))
    }

    // --- reading: the wall-clock time placed in the current zone --------------------------------

    @Test
    fun readingReversesWritingOnOrdinaryTimes() {
        inZone("Asia/Kolkata")
        assertEquals(1773550800000L, read(1773570600000L))
        inZone("Asia/Kathmandu")
        assertEquals(1798740899999L, read(1798761599999L))
        inZone("Pacific/Auckland")
        assertEquals(1767178800000L, read(1767225600000L))
        inZone("America/Los_Angeles")
        assertEquals(1783213507123L, read(1783188307123L))
    }

    @Test
    fun aRowKeepsItsWallClockTimeAfterTheDeviceChangesZone() {
        inZone("Asia/Kolkata")
        val stored = store(1773550800000L)!! // 10:30 in Kolkata
        inZone("America/New_York")
        // Read back as 10:30 in New York, not as the same instant (00:30 EDT)
        assertEquals(1773585000000L, read(stored))
    }

    @Test
    fun aTimeInTheSpringForwardGapMovesLaterByTheGap() {
        inZone("America/New_York")
        // 2026-03-08 02:30 doesn't exist in New York; it reads as 03:30 EDT (07:30 UTC)
        assertEquals(1772955000000L, read(1772937000000L))
    }

    @Test
    fun aTimeInTheFallBackOverlapReadsAsTheEarlierOccurrence() {
        inZone("America/New_York")
        // 2026-11-01 01:30 happens twice; it reads as the first one, 01:30 EDT (05:30 UTC)
        assertEquals(1793511000000L, read(1793496600000L))
    }

    @Test
    fun theSecondOccurrenceOfAnOverlapTimeComesBackAnHourEarlier() {
        inZone("America/New_York")
        // 01:30 EST (06:30 UTC), the second 01:30 of 2026-11-01, is stored as 01:30 ...
        val stored = store(1793514600000L)!!
        assertEquals(1793496600000L, stored)
        // ... and reads back as the first 01:30 (EDT). Existing behaviour, kept on purpose.
        assertEquals(1793511000000L, read(stored))
    }

    @Test
    fun nullStaysNull() {
        assertNull(DateConverter.dateToTimestamp(null))
        assertNull(DateConverter.fromTimestamp(null))
    }
}
