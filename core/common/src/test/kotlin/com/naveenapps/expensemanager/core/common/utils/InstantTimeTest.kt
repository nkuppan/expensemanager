package com.naveenapps.expensemanager.core.common.utils

import com.naveenapps.expensemanager.core.foundation.hourAndMinute
import com.naveenapps.expensemanager.core.foundation.nextTimeOfDay
import com.naveenapps.expensemanager.core.foundation.nextWeekdayAt
import com.naveenapps.expensemanager.core.foundation.withDateOf
import com.naveenapps.expensemanager.core.foundation.withTime
import java.util.Calendar
import java.util.TimeZone as JavaTimeZone
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * [InstantTime] functions against the java.util.Calendar code they replaced (KMP groundwork C5).
 *
 * Dates are stored as local wall-clock time (see DateConverter), so what must not change is the
 * *local* date and time a function produces; that is checked in every zone. The exact instant is
 * also checked in zones without daylight saving. In the repeated hour when clocks go back,
 * Calendar picked the second occurrence and kotlinx-datetime picks the first: same wall-clock
 * time, so the same stored value.
 */
class InstantTimeTest {

    private lateinit var originalZone: JavaTimeZone

    @Before
    fun save() {
        originalZone = JavaTimeZone.getDefault()
    }

    @After
    fun restore() {
        JavaTimeZone.setDefault(originalZone)
    }

    private val noDst = listOf("UTC", "Asia/Kolkata", "Asia/Kathmandu")
    private val withDst = listOf(
        "America/New_York",
        "America/Sao_Paulo",
        "Europe/London",
        "Pacific/Auckland",
        "Australia/Lord_Howe",
        "Asia/Beirut",
    )

    /** Every ~97 minutes from 2018 to 2028 in [zoneId], with the JVM default zone set to it (Calendar uses it). */
    private fun sweep(zoneId: String, block: (Long, TimeZone, Int) -> Unit) {
        JavaTimeZone.setDefault(JavaTimeZone.getTimeZone(zoneId))
        val zone = TimeZone.of(zoneId)
        var at = LocalDateTime(2018, 1, 1, 0, 7).toInstant(zone).toEpochMilliseconds()
        val end = LocalDateTime(2028, 1, 1, 0, 0).toInstant(zone).toEpochMilliseconds()
        var step = 0
        while (at < end) {
            block(at, zone, step++)
            at += 97L * 60_000 + 1_234
        }
    }

    private fun local(millis: Long, zone: TimeZone) = Instant.fromEpochMilliseconds(millis).toLocalDateTime(zone)

    private fun assertSame(what: String, zoneId: String, zone: TimeZone, old: Long, new: Instant) {
        assertEquals("$what in $zoneId (local time)", local(old, zone), new.toLocalDateTime(zone))
        if (zoneId in noDst) assertEquals("$what in $zoneId (instant)", old, new.toEpochMilliseconds())
    }

    private fun calendarAt(millis: Long) = Calendar.getInstance().apply { timeInMillis = millis }

    @Test
    fun hourAndMinuteMatchCalendar() {
        (noDst + withDst).forEach { zoneId ->
            sweep(zoneId) { at, zone, _ ->
                val calendar = calendarAt(at)
                val expected = calendar.get(Calendar.HOUR_OF_DAY) to calendar.get(Calendar.MINUTE)
                assertEquals("$zoneId $at", expected, Instant.fromEpochMilliseconds(at).hourAndMinute(zone))
            }
        }
    }

    /** The add-transaction time picker: hour and minute replaced, seconds kept. */
    @Test
    fun withTimeKeepingSecondsMatchesTheOldTimePicker() {
        (noDst + withDst).forEach { zoneId ->
            sweep(zoneId) { at, zone, step ->
                val hour = step * 7 % 24
                val minute = step * 13 % 60
                val old = calendarAt(at).apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                }.timeInMillis
                val new = Instant.fromEpochMilliseconds(at).withTime(hour, minute, resetSeconds = false, zone = zone)
                assertSame("time picker $at → $hour:$minute", zoneId, zone, old, new)
            }
        }
    }

    /** The recurring rule's time picker: seconds reset, value untouched when the time didn't change. */
    @Test
    fun withTimeResettingSecondsMatchesTheOldRecurringEdit() {
        (noDst + withDst).forEach { zoneId ->
            sweep(zoneId) { at, zone, step ->
                val hour = step * 5 % 24
                val minute = step * 11 % 60
                val calendar = calendarAt(at)
                val unchanged = calendar.get(Calendar.HOUR_OF_DAY) == hour && calendar.get(Calendar.MINUTE) == minute
                val old = if (unchanged) {
                    at
                } else {
                    calendar.apply {
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                }
                val instant = Instant.fromEpochMilliseconds(at)
                val unchangedNow = instant.hourAndMinute(zone) == (hour to minute)
                val new = if (unchangedNow) instant else instant.withTime(hour, minute, zone = zone)
                assertSame("recurring time $at → $hour:$minute", zoneId, zone, old, new)
            }
        }
    }

    /** The recurring rule's date picker: the picked day, this instant's time of day. */
    @Test
    fun withDateOfMatchesTheOldRecurringEdit() {
        (noDst + withDst).forEach { zoneId ->
            sweep(zoneId) { at, zone, step ->
                val day = at + (step % 401 - 200) * 86_400_000L
                val source = calendarAt(at)
                val target = calendarAt(day)
                val old = (source.clone() as Calendar).apply {
                    set(target.get(Calendar.YEAR), target.get(Calendar.MONTH), target.get(Calendar.DAY_OF_MONTH))
                }.timeInMillis
                val new = Instant.fromEpochMilliseconds(at).withDateOf(Instant.fromEpochMilliseconds(day), zone)
                assertSame("date picker $at → $day", zoneId, zone, old, new)
            }
        }
    }

    /** Where there's no daylight saving, the old Calendar scheduling gave exactly these times. */
    @Test
    fun nextTimeOfDayMatchesCalendarWithoutDaylightSaving() {
        noDst.forEach { zoneId ->
            sweep(zoneId) { at, zone, step ->
                val hour = step % 24
                val old = calendarAt(at).apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    if (timeInMillis <= at) add(Calendar.DATE, 1)
                }.timeInMillis
                val new = Instant.fromEpochMilliseconds(at).nextTimeOfDay(hour, 0, zone)
                assertEquals("$zoneId $at → $hour:00", old, new.toEpochMilliseconds())
            }
        }
    }

    /**
     * Everywhere: the result is after now, within a day, and is the chosen local time (or, when
     * the clocks skip it that night, the first moment after the gap). Calendar was an hour off
     * on those nights, so this checks the rule rather than the old output.
     */
    @Test
    fun nextTimeOfDayIsTheNextChosenLocalTime() {
        (noDst + withDst).forEach { zoneId ->
            sweep(zoneId) { at, zone, step ->
                val hour = step % 24
                val minute = step * 7 % 60
                val now = Instant.fromEpochMilliseconds(at)
                val next = now.nextTimeOfDay(hour, minute, zone)
                assertTrue("$zoneId $now: $next isn't after now", next > now)
                assertTrue("$zoneId $now: $next is more than a day away", next - now <= 1.days + 1.hours)
                // It is that day's hour:minute (shifted past the gap if the clocks skip it).
                val date = next.toLocalDateTime(zone).date
                val expected = LocalDateTime(date, LocalTime(hour, minute)).toInstant(zone)
                assertEquals("$zoneId $now → $hour:$minute", expected, next)
                // And no earlier one qualifies: the previous day's is not after now.
                val previous = LocalDateTime(date.minus(1, DateTimeUnit.DAY), LocalTime(hour, minute)).toInstant(zone)
                assertTrue("$zoneId $now: skipped $previous", previous <= now)
            }
        }
    }

    @Test
    fun nextWeekdayAtIsTheComingSundayEvening() {
        (noDst + withDst).forEach { zoneId ->
            sweep(zoneId) { at, zone, _ ->
                val now = Instant.fromEpochMilliseconds(at)
                val next = now.nextWeekdayAt(DayOfWeek.SUNDAY, 19, zone)
                val local = next.toLocalDateTime(zone)
                assertEquals("$zoneId $now", DayOfWeek.SUNDAY, local.dayOfWeek)
                assertEquals("$zoneId $now", 19 to 0, local.hour to local.minute)
                assertTrue("$zoneId $now: $next isn't after now", next > now)
                // 7 days, plus the extra hour in the week the clocks go back.
                assertTrue("$zoneId $now: $next isn't the coming Sunday", next - now <= 7.days + 1.hours)
            }
        }
    }

    /** UpcomingRecurringWorker stores LocalDate.toEpochDays() where it stored java.time's toEpochDay(). */
    @Test
    fun epochDaysMatchJavaTime() {
        var day = java.time.LocalDate.of(2000, 1, 1)
        while (day.year < 2040) {
            val kotlinDay = kotlinx.datetime.LocalDate(day.year, day.monthValue, day.dayOfMonth)
            assertEquals(day.toString(), day.toEpochDay(), kotlinDay.toEpochDays().toLong())
            day = day.plusDays(3)
        }
    }
}
