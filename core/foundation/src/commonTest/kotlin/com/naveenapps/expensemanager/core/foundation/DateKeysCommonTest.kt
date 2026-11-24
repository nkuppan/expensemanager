package com.naveenapps.expensemanager.core.foundation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

/**
 * Stored keys and period bounds, with fixed expected values (worked out independently from the
 * IANA time-zone data), so the same assertions run on the Android host and the iOS simulator
 * (OpenSpec change add-kmp-foundation-module, task 3.1). The old java.util versions are compared
 * separately in core:common's DateKeysTest, which can only run on the JVM.
 */
class DateKeysCommonTest {

    private val kolkata = TimeZone.of("Asia/Kolkata")
    private val newYork = TimeZone.of("America/New_York")

    @Test
    fun monthKeyIsEnglishAndPadded() {
        assertEquals("October 2026", LocalDate(2026, 10, 15).toMonthAndYearKey())
        assertEquals("January 2026", LocalDate(2026, 1, 1).toMonthAndYearKey())
        assertEquals("December 0999", LocalDate(999, 12, 31).toMonthAndYearKey())
    }

    @Test
    fun monthKeyOfAnInstantUsesTheGivenZone() {
        // 31 Oct 2026 20:00 UTC is already 1 November in India.
        val lateOctoberUtc = Instant.parse("2026-10-31T20:00:00Z")
        assertEquals("October 2026", lateOctoberUtc.toMonthAndYearKey(TimeZone.UTC))
        assertEquals("November 2026", lateOctoberUtc.toMonthAndYearKey(kolkata))
    }

    @Test
    fun monthKeyParsesBack() {
        assertEquals(LocalDate(2026, 10, 1), "October 2026".monthAndYearKeyToLocalDate())
        assertEquals(LocalDate(2026, 10, 1), "october 2026".monthAndYearKeyToLocalDate())
        assertEquals(LocalDate(2026, 10, 1), "Oct 2026".monthAndYearKeyToLocalDate())
        assertEquals(LocalDate(2026, 10, 1), "  October   2026 ".monthAndYearKeyToLocalDate())
    }

    @Test
    fun malformedMonthKeysAreRejected() {
        listOf("", "October", "2026", "Octember 2026", "October 2026 x", "Oc 2026", "October 20a6", "October -2026")
            .forEach { assertNull(it.monthAndYearKeyToLocalDate(), "'$it'") }
    }

    @Test
    fun yearKeyIsPaddedAndParsesBack() {
        assertEquals("2026", LocalDate(2026, 6, 1).toYearKey())
        assertEquals("0999", LocalDate(999, 6, 1).toYearKey())
        assertEquals("2026", Instant.parse("2026-12-31T20:00:00Z").toYearKey(TimeZone.UTC))
        assertEquals("2027", Instant.parse("2026-12-31T20:00:00Z").toYearKey(kolkata))
        assertEquals(LocalDate(2026, 1, 1), "2026".yearKeyToLocalDate())
        listOf("", "20 26", "2026a", "-2026").forEach { assertNull(it.yearKeyToLocalDate(), "'$it'") }
    }

    @Test
    fun dayKeyIsDayMonthYear() {
        assertEquals("05/03/2026", LocalDate(2026, 3, 5).toDayKey())
        assertEquals("31/12/2026", LocalDate(2026, 12, 31).toDayKey())
    }

    @Test
    fun localDateOfAnInstantUsesTheGivenZone() {
        val instant = Instant.parse("2026-11-22T20:00:00Z")
        assertEquals(LocalDate(2026, 11, 22), instant.toLocalDateIn(TimeZone.UTC))
        assertEquals(LocalDate(2026, 11, 23), instant.toLocalDateIn(kolkata))
    }

    @Test
    fun monthBoundsAreTheFirstAndLastMillisecond() {
        val october = LocalDate(2026, 10, 15)
        assertEquals(1_790_793_000_000, october.startOfMonthMillis(kolkata))
        assertEquals(1_793_471_399_999, october.endOfMonthMillis(kolkata))
        // A daylight-saving change inside the month: starts at EST (UTC-5), ends at EDT (UTC-4).
        val march = LocalDate(2026, 3, 20)
        assertEquals(1_772_341_200_000, march.startOfMonthMillis(newYork))
        assertEquals(1_775_015_999_999, march.endOfMonthMillis(newYork))
        // Leap February.
        assertEquals(1_835_461_799_999, LocalDate(2028, 2, 10).endOfMonthMillis(kolkata))
    }

    @Test
    fun yearBoundsAreTheFirstAndLastMillisecond() {
        val day = LocalDate(2026, 7, 4)
        assertEquals(1_767_225_600_000, day.startOfYearMillis(TimeZone.UTC))
        assertEquals(1_798_761_599_999, day.endOfYearMillis(TimeZone.UTC))
    }
}
