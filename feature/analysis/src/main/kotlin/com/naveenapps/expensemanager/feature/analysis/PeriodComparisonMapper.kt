package com.naveenapps.expensemanager.feature.analysis

import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.PeriodComparison
import com.naveenapps.expensemanager.core.model.changeRatio
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

/** Null when neither period has any income or expense (nothing worth showing). */
internal fun PeriodComparison.toUi(
    zone: ZoneId = ZoneId.systemDefault(),
    locale: Locale = Locale.getDefault(),
    format: (Double) -> String,
): PeriodComparisonUi? {
    if (currentExpense == 0.0 && previousExpense == 0.0 && currentIncome == 0.0 && previousIncome == 0.0) {
        return null
    }
    val withYear = type == DateRangeType.THIS_YEAR || type == DateRangeType.CUSTOM
    return PeriodComparisonUi(
        titleResId = when (type) {
            DateRangeType.TODAY -> R.string.compare_title_day
            DateRangeType.THIS_WEEK -> R.string.compare_title_week
            DateRangeType.THIS_MONTH -> R.string.compare_title_month
            DateRangeType.THIS_YEAR -> R.string.compare_title_year
            else -> R.string.compare_title_custom
        },
        periodsLabel = "${rangeLabel(currentStart, currentEnd, zone, locale, withYear)} vs " +
            rangeLabel(previousStart, previousEnd, zone, locale, withYear),
        expense = ComparisonMetricUi(format(currentExpense), format(previousExpense), changeRatio(currentExpense, previousExpense)),
        income = ComparisonMetricUi(format(currentIncome), format(previousIncome), changeRatio(currentIncome, previousIncome)),
        categoryChanges = categoryChanges.map {
            val increase = it.difference > 0
            CategoryChangeUi(
                category = it.category,
                current = format(it.current),
                previous = format(it.previous),
                difference = (if (increase) "+" else "−") + format(abs(it.difference)),
                isIncrease = increase,
            )
        },
    )
}

/** "1–3 Oct", "3 Oct" or, across months, "28 Sep – 3 Oct". [endExclusive] is the window end. */
private fun rangeLabel(start: Long, endExclusive: Long, zone: ZoneId, locale: Locale, withYear: Boolean): String {
    val first = Instant.ofEpochMilli(start).atZone(zone).toLocalDate()
    val last = Instant.ofEpochMilli(endExclusive - 1).atZone(zone).toLocalDate().coerceAtLeast(first)
    val full = DateTimeFormatter.ofPattern(if (withYear) "d MMM yyyy" else "d MMM", locale)
    return when {
        first == last -> full.format(first)
        first.month == last.month && first.year == last.year ->
            "${first.dayOfMonth}–${full.format(last)}"
        else -> "${short(first, locale, withYear && first.year != last.year)} – ${full.format(last)}"
    }
}

private fun short(date: LocalDate, locale: Locale, withYear: Boolean): String =
    DateTimeFormatter.ofPattern(if (withYear) "d MMM yyyy" else "d MMM", locale).format(date)
