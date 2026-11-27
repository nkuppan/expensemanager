package com.naveenapps.expensemanager.core.foundation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * The "local as UTC" conversion used by the database (DateConverter) and the date-range store,
 * moved here from core:common (OpenSpec change make-core-datastore-multiplatform, task 1.3).
 * Runs on the JVM and the iOS simulator.
 */
class LocalAsUtcCommonTest {

    private val zones = listOf("UTC", "Asia/Kolkata", "America/New_York", "Australia/Lord_Howe").map { TimeZone.of(it) }

    /** Every hour of 2026 (UTC), so every DST change in these zones is crossed hour by hour. */
    private fun hoursOf2026(): Sequence<Long> = generateSequence(1_767_225_600_000L) { it + 3_600_000L }
        .takeWhile { it < 1_798_761_600_000L }

    @Test
    fun storedFormIsTheWallClockTimeWrittenAsUtc() {
        zones.forEach { zone ->
            hoursOf2026().forEach { millis ->
                val wallClock = Instant.fromEpochMilliseconds(millis).toLocalDateTime(zone)
                assertEquals(wallClock.toInstant(TimeZone.UTC).toEpochMilliseconds(), millis.fromLocalToUTCTimeStamp(zone), "$zone $millis")
            }
        }
    }

    @Test
    fun storedFormReadsBackToTheSameWallClockTime() {
        zones.forEach { zone ->
            hoursOf2026().forEach { millis ->
                val back = millis.fromLocalToUTCTimeStamp(zone).fromUTCToLocalTimeStamp(zone)
                val wallClock = Instant.fromEpochMilliseconds(millis).toLocalDateTime(zone)
                // Same wall-clock time; the same instant unless that time occurs twice (then the earlier one).
                assertEquals(wallClock, Instant.fromEpochMilliseconds(back).toLocalDateTime(zone), "$zone $millis")
                assertEquals(wallClock.toInstant(zone).toEpochMilliseconds(), back, "$zone $millis")
            }
        }
    }

    @Test
    fun pinnedExamples() {
        val kolkata = TimeZone.of("Asia/Kolkata")
        // 10:00 in Kolkata (04:30 UTC) is stored as 10:00 UTC.
        val tenInKolkata = LocalDateTime(2026, 1, 1, 10, 0).toInstant(kolkata).toEpochMilliseconds()
        assertEquals(LocalDateTime(2026, 1, 1, 10, 0).toInstant(TimeZone.UTC).toEpochMilliseconds(), tenInKolkata.fromLocalToUTCTimeStamp(kolkata))

        // New York, 1 Nov 2026: 01:30 happens twice; the stored 01:30 reads back as the first (EDT, 05:30 UTC).
        val newYork = TimeZone.of("America/New_York")
        val stored = LocalDateTime(2026, 11, 1, 1, 30).toInstant(TimeZone.UTC).toEpochMilliseconds()
        assertEquals(LocalDateTime(2026, 11, 1, 5, 30).toInstant(TimeZone.UTC).toEpochMilliseconds(), stored.fromUTCToLocalTimeStamp(newYork))
    }
}
