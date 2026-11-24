package com.naveenapps.expensemanager.core.foundation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone

/**
 * Date/time edits and scheduling with fixed expected values (worked out independently from the
 * IANA time-zone data), run on the Android host and the iOS simulator (OpenSpec change
 * add-kmp-foundation-module, task 3.2). The comparison with the old java.util.Calendar code is
 * core:common's InstantTimeTest.
 */
class InstantTimeCommonTest {

    private val kolkata = TimeZone.of("Asia/Kolkata")
    private val newYork = TimeZone.of("America/New_York")

    private fun at(millis: Long) = Instant.fromEpochMilliseconds(millis)

    /** 23 Nov 2026, 10:15:30.123 in India. */
    private val morning = at(1_795_409_130_123)

    @Test
    fun hourAndMinuteAreLocal() {
        assertEquals(10 to 15, morning.hourAndMinute(kolkata))
        assertEquals(4 to 45, morning.hourAndMinute(TimeZone.UTC))
    }

    @Test
    fun withTimeKeepsOrResetsSeconds() {
        assertEquals(at(1_795_437_330_123), morning.withTime(18, 5, resetSeconds = false, zone = kolkata))
        assertEquals(at(1_795_437_300_000), morning.withTime(18, 5, zone = kolkata))
    }

    @Test
    fun withDateOfKeepsTheTimeOfDay() {
        val christmasEarly = at(1_798_147_800_000) // 25 Dec 2026, 03:00 in India
        assertEquals(at(1_798_173_930_123), morning.withDateOf(christmasEarly, kolkata))
    }

    @Test
    fun nextTimeOfDayIsLaterTodayOrTomorrow() {
        val tenAm = at(1_795_408_200_000) // 23 Nov 2026, 10:00 in India
        assertEquals(at(1_795_437_000_000), tenAm.nextTimeOfDay(18, 0, kolkata), "later today")
        assertEquals(at(1_795_491_000_000), tenAm.nextTimeOfDay(9, 0, kolkata), "already passed: tomorrow")
        assertEquals(at(1_795_494_600_000), tenAm.nextTimeOfDay(10, 0, kolkata), "exactly now: tomorrow")
    }

    @Test
    fun nextTimeOfDaySkipsForwardOverTheDaylightSavingGap() {
        // 7 Mar 2026, 14:00 EST. 02:00 doesn't exist on 8 March: the clocks jump to 03:00 EDT.
        val saturday = at(1_772_910_000_000)
        assertEquals(at(1_772_953_200_000), saturday.nextTimeOfDay(2, 0, newYork))
    }

    @Test
    fun nextWeekdayAtIsThisSundayOrNext() {
        val sundayBefore = at(1_795_350_600_000) // Sun 22 Nov 2026, 18:00 in India
        val sundayAfter = at(1_795_357_800_000) // Sun 22 Nov 2026, 20:00
        val wednesday = at(1_795_588_200_000) // Wed 25 Nov 2026, 12:00
        val thisSundayEvening = at(1_795_354_200_000) // Sun 22 Nov 2026, 19:00
        val nextSundayEvening = at(1_795_959_000_000) // Sun 29 Nov 2026, 19:00
        assertEquals(thisSundayEvening, sundayBefore.nextWeekdayAt(DayOfWeek.SUNDAY, 19, kolkata))
        assertEquals(nextSundayEvening, sundayAfter.nextWeekdayAt(DayOfWeek.SUNDAY, 19, kolkata))
        assertEquals(nextSundayEvening, wednesday.nextWeekdayAt(DayOfWeek.SUNDAY, 19, kolkata))
        assertEquals(nextSundayEvening, thisSundayEvening.nextWeekdayAt(DayOfWeek.SUNDAY, 19, kolkata), "exactly then")
    }
}
