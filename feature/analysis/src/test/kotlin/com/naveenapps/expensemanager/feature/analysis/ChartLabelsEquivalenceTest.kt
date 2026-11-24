package com.naveenapps.expensemanager.feature.analysis

import com.naveenapps.expensemanager.core.common.utils.toMonthAndYear
import com.naveenapps.expensemanager.core.domain.usecase.transaction.chartBucketStart
import com.naveenapps.expensemanager.core.foundation.toDayKey
import com.naveenapps.expensemanager.core.foundation.toLocalDateIn
import com.naveenapps.expensemanager.core.foundation.toYearKey
import com.naveenapps.expensemanager.core.model.GroupType
import java.util.Locale
import java.util.TimeZone as JavaTimeZone
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * The chart now groups by bucket start date (core:domain) and labels in the UI. Labels and
 * grouping must match the old string-keyed code exactly (OpenSpec change
 * make-core-domain-multiplatform, design D2, task 3.4).
 */
class ChartLabelsEquivalenceTest {

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

    /** The bucket key GetChartDataUseCase used before (its private `groupValue`), kept as the reference. */
    private fun oldKey(groupType: GroupType, at: Instant): String = when (groupType) {
        GroupType.YEAR -> at.toLocalDateIn().toYearKey()
        GroupType.MONTH -> at.toMonthAndYear()
        GroupType.DATE -> at.toLocalDateIn().toDayKey()
    }

    private val zones = listOf("UTC", "Asia/Kolkata", "America/New_York", "America/Sao_Paulo", "Pacific/Chatham")
    private val locales = listOf(Locale.US, Locale.GERMANY)

    /** Every hour of 2019–2027 (so every DST change is crossed hour by hour). */
    private fun samples(): Sequence<Instant> = generateSequence(1_546_300_800_000L) { it + 3_600_000L }
        .takeWhile { it < 1_830_297_600_000L }
        .map { Instant.fromEpochMilliseconds(it) }

    @Test
    fun labelsAndGroupingMatchTheOldStringKeys() {
        zones.forEach { zoneId ->
            locales.forEach { locale ->
                JavaTimeZone.setDefault(JavaTimeZone.getTimeZone(zoneId))
                Locale.setDefault(locale)
                GroupType.entries.forEach { groupType ->
                    val bucketForKey = HashMap<String, LocalDate>()
                    val keyForBucket = HashMap<LocalDate, String>()
                    samples().forEach { at ->
                        val where = "$zoneId $locale $groupType $at"
                        val old = oldKey(groupType, at)
                        val bucket = chartBucketStart(groupType, at)
                        assertEquals(where, old, chartLabel(groupType, bucket))
                        // Same bucket exactly when the old code gave the same key.
                        assertEquals(where, bucket, bucketForKey.getOrPut(old) { bucket })
                        assertEquals(where, old, keyForBucket.getOrPut(bucket) { old })
                    }
                }
            }
        }
    }
}
