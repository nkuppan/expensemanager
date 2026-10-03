package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.minusRespectiveFrame
import com.naveenapps.expensemanager.core.model.DateRangeType
import kotlinx.datetime.TimeZone
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * The two date windows compared on Analysis, in epoch millis (start inclusive, end exclusive,
 * same as the filter ranges).
 *
 * @property isToDate true when the selected period is still in progress, so both windows stop at
 *   the same point (e.g. Oct 1–3 vs Sep 1–3) instead of comparing a partial period with a full one.
 */
data class ComparisonWindows(
    val type: DateRangeType,
    val currentStart: Long,
    val currentEnd: Long,
    val previousStart: Long,
    val previousEnd: Long,
    val isToDate: Boolean,
)

/**
 * Previous period for [type]/[range]: yesterday, last week, last calendar month or last year
 * (shifted with the same rules as the filter's back arrow), or, for a custom range, a window of
 * the same length right before it. Null for "All" and for periods that haven't started yet.
 */
@OptIn(ExperimentalTime::class)
fun comparisonWindows(
    type: DateRangeType,
    range: List<Long>,
    now: Long,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): ComparisonWindows? {
    if (type == DateRangeType.ALL || range.size < 2) return null
    val start = range[0]
    val end = range[1]
    if (end <= start || now <= start) return null

    val previousStart: Long
    val previousEnd: Long
    if (type == DateRangeType.CUSTOM) {
        previousStart = start - (end - start)
        previousEnd = start
    } else {
        previousStart = Instant.fromEpochMilliseconds(start).minusRespectiveFrame(type, timeZone).toEpochMilliseconds()
        previousEnd = Instant.fromEpochMilliseconds(end).minusRespectiveFrame(type, timeZone).toEpochMilliseconds()
    }

    val inProgress = now < end
    return if (inProgress) {
        val elapsed = now - start
        ComparisonWindows(
            type = type,
            currentStart = start,
            currentEnd = now,
            previousStart = previousStart,
            previousEnd = minOf(previousEnd, previousStart + elapsed),
            isToDate = true,
        )
    } else {
        ComparisonWindows(type, start, end, previousStart, previousEnd, isToDate = false)
    }
}
