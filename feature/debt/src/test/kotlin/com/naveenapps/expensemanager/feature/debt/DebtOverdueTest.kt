package com.naveenapps.expensemanager.feature.debt

import com.google.common.truth.Truth.assertWithMessage
import java.util.Calendar
import java.util.TimeZone as JavaTimeZone
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.After
import org.junit.Before
import org.junit.Test

/** [isOverdue] on Instants against the java.util.Calendar version it replaced (KMP groundwork C5). */
class DebtOverdueTest {

    private lateinit var originalZone: JavaTimeZone

    @Before
    fun save() {
        originalZone = JavaTimeZone.getDefault()
    }

    @After
    fun restore() {
        JavaTimeZone.setDefault(originalZone)
    }

    /** The previous implementation: due before 00:00 today (Calendar, device zone). */
    private fun legacyIsOverdue(dueDate: Long, now: Long): Boolean {
        val startOfToday = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return dueDate < startOfToday
    }

    @Test
    fun noDueDateIsNeverOverdue() {
        assertWithMessage("null due date").that(isOverdue(null)).isFalse()
    }

    @Test
    fun dueYesterdayIsOverdueAndDueTodayIsNot() {
        val zone = TimeZone.of("Asia/Kolkata")
        val now = LocalDateTime(2026, 11, 23, 10, 0).toInstant(zone)
        assertWithMessage("yesterday 23:59")
            .that(isOverdue(LocalDateTime(2026, 11, 22, 23, 59).toInstant(zone), now, zone)).isTrue()
        assertWithMessage("today 00:00")
            .that(isOverdue(LocalDateTime(2026, 11, 23, 0, 0).toInstant(zone), now, zone)).isFalse()
        assertWithMessage("tomorrow")
            .that(isOverdue(LocalDateTime(2026, 11, 24, 9, 0).toInstant(zone), now, zone)).isFalse()
    }

    /**
     * Every ~2.3 hours over ten years, with a due date up to 3 days either side, in zones with
     * and without daylight saving (including Lord Howe's half-hour change and São Paulo's
     * midnight change): identical answers.
     */
    @Test
    fun matchesTheCalendarVersionInEveryZone() {
        listOf(
            "UTC",
            "Asia/Kolkata",
            "Asia/Kathmandu",
            "America/New_York",
            "America/Sao_Paulo",
            "Europe/London",
            "Pacific/Auckland",
            "Australia/Lord_Howe",
        ).forEach { zoneId ->
            JavaTimeZone.setDefault(JavaTimeZone.getTimeZone(zoneId))
            val zone = TimeZone.of(zoneId)
            var now = LocalDateTime(2018, 1, 1, 0, 5).toInstant(zone).toEpochMilliseconds()
            val end = LocalDateTime(2028, 1, 1, 0, 0).toInstant(zone).toEpochMilliseconds()
            var step = 0L
            while (now < end) {
                val due = now + (step % 145 - 72) * 3_600_000L + step % 60 * 60_000L
                assertWithMessage("$zoneId now=$now due=$due")
                    .that(isOverdue(Instant.fromEpochMilliseconds(due), Instant.fromEpochMilliseconds(now), zone))
                    .isEqualTo(legacyIsOverdue(due, now))
                now += 8_317_000L
                step++
            }
        }
    }
}
