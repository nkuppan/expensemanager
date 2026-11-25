package com.naveenapps.expensemanager.core.common.utils

import java.util.Date
import java.util.TimeZone as JavaTimeZone
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/** Expected epoch values were computed independently (Python zoneinfo, IANA tz data). */
class DateBridgesTest {

    private lateinit var originalZone: JavaTimeZone

    @Before
    fun saveZone() {
        originalZone = JavaTimeZone.getDefault()
    }

    @After
    fun restoreZone() {
        JavaTimeZone.setDefault(originalZone)
    }

    private fun inZone(zoneId: String) = JavaTimeZone.setDefault(JavaTimeZone.getTimeZone(zoneId))

    @Test
    fun instantRoundTripKeepsEveryMillisecond() {
        val date = Date(1773550845678L)
        assertEquals(1773550845678L, date.toKotlinInstant().toEpochMilliseconds())
        assertEquals(date, date.toKotlinInstant().toJavaDate())
    }

    @Test
    fun localDateTimeFollowsTheDeviceZone() {
        inZone("Asia/Kolkata")
        // 2026-03-15 10:30:45.678 IST
        val local = Date(1773550845678L).toKotlinLocalDateTime()
        assertEquals(LocalDateTime(2026, 3, 15, 10, 30, 45, 678_000_000), local)
        assertEquals(1773550845678L, local.toJavaDate().time)
    }

    @Test
    fun localDateUsesTheDeviceZoneNotUtc() {
        inZone("America/New_York")
        // 2026-01-31 23:45 EST is already 1 February in UTC
        val date = Date(1769921100000L)
        assertEquals(LocalDate(2026, 1, 31), date.toKotlinLocalDate())
        assertEquals(LocalDate(2026, 2, 1), date.toKotlinLocalDate(TimeZone.UTC))
    }

    @Test
    fun localDateBecomesStartOfThatDay() {
        inZone("Asia/Kolkata")
        assertEquals(1773513000000L, LocalDate(2026, 3, 15).toJavaDate().time)
    }

    @Test
    fun aDayWithoutMidnightStartsAtTheFirstRealMoment() {
        inZone("America/Sao_Paulo")
        // On 2018-11-04 clocks jumped from 00:00 to 01:00, so the day began at 01:00 -02:00
        assertEquals(1541300400000L, LocalDate(2018, 11, 4).toJavaDate().time)
    }

    @Test
    fun explicitZoneOverridesTheDeviceZone() {
        inZone("UTC")
        val kolkata = TimeZone.of("Asia/Kolkata")
        assertEquals(1773513000000L, LocalDate(2026, 3, 15).toJavaDate(kolkata).time)
        assertEquals(Instant.fromEpochMilliseconds(1773513000000L), LocalDate(2026, 3, 15).toJavaDate(kolkata).toKotlinInstant())
    }
}
