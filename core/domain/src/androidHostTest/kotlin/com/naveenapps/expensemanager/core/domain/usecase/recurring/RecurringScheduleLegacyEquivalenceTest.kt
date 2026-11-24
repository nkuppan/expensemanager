package com.naveenapps.expensemanager.core.domain.usecase.recurring

import com.google.common.truth.Truth.assertWithMessage
import com.naveenapps.expensemanager.core.model.RecurringFrequency
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import org.junit.Test

/**
 * [occurrenceDate] (kotlinx-datetime) against the java.time code it replaced (KMP groundwork C3).
 * Split from RecurringTransactionsTest when that moved to commonTest, because this comparison
 * needs the JVM (OpenSpec change make-core-domain-multiplatform, task 5.3).
 */
class RecurringScheduleLegacyEquivalenceTest {

    /** The java.time implementation of [occurrenceDate] before the move to kotlinx-datetime. */
    private fun legacyOccurrenceDate(start: java.util.Date, frequency: RecurringFrequency, index: Int, zone: ZoneId): java.util.Date {
        val base = start.toInstant().atZone(zone).toLocalDateTime()
        val n = index.toLong()
        val shifted = when (frequency) {
            RecurringFrequency.DAILY -> base.plusDays(n)
            RecurringFrequency.WEEKLY -> base.plusWeeks(n)
            RecurringFrequency.MONTHLY -> base.plusMonths(n)
            RecurringFrequency.YEARLY -> base.plusYears(n)
        }
        return java.util.Date.from(shifted.atZone(zone).toInstant())
    }

    @Test
    fun `kotlinx occurrence dates match the java time version in every zone`() {
        val zoneIds = listOf(
            "UTC",
            "Asia/Kolkata",
            "Asia/Kathmandu",
            "America/New_York",
            "America/Sao_Paulo",
            "Pacific/Auckland",
            "Australia/Lord_Howe",
            "Europe/London",
        )
        for (zoneId in zoneIds) {
            val javaZone = ZoneId.of(zoneId)
            val kotlinZone = TimeZone.of(zoneId)
            var at = LocalDateTime.of(2018, 1, 28, 0, 30).atZone(javaZone).toInstant().toEpochMilli()
            val end = LocalDateTime.of(2028, 1, 1, 0, 0).atZone(javaZone).toInstant().toEpochMilli()
            while (at < end) {
                for (frequency in RecurringFrequency.entries) {
                    for (index in listOf(0, 1, 2, 5, 13, 40)) {
                        val expected = legacyOccurrenceDate(java.util.Date(at), frequency, index, javaZone).time
                        val actual = occurrenceDate(Instant.fromEpochMilliseconds(at), frequency, index, kotlinZone).toEpochMilliseconds()
                        assertWithMessage("$zoneId start=$at $frequency #$index").that(actual).isEqualTo(expected)
                    }
                }
                // ~53 hours and an odd number of minutes: walks across month ends, DST changes and times of day.
                at += 53L * 60 * 60 * 1000 + 17L * 60 * 1000
            }
        }
    }
}
