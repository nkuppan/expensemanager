package com.naveenapps.expensemanager.core.settings.data.repository

import com.naveenapps.expensemanager.core.settings.domain.model.NumberFormatType
import com.naveenapps.expensemanager.core.settings.domain.repository.NumberFormatRepository
import com.naveenapps.expensemanager.core.settings.domain.repository.NumberFormatSettingRepository
import java.text.NumberFormat
import java.text.ParsePosition
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class NumberFormatRepositoryImpl(
    private val locale: Locale = Locale.getDefault(),
    numberFormatSettingRepository: NumberFormatSettingRepository,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob()),
) : NumberFormatRepository {

    private var numberFormatType: NumberFormatType = NumberFormatType.WITHOUT_ANY_SEPARATOR

    init {
        numberFormatSettingRepository.getNumberFormatType().onEach {
            numberFormatType = it
        }.launchIn(coroutineScope)
    }

    private fun getFormatter(): NumberFormat = when (numberFormatType) {
        NumberFormatType.WITHOUT_ANY_SEPARATOR -> {
            ApplicationNumberFormatter.getNumberFormatWithoutGrouping(locale)
        }

        NumberFormatType.WITH_COMMA_SEPARATOR -> {
            ApplicationNumberFormatter.getNumberFormatWithGrouping(locale)
        }
    }

    /**
     * Converts a Double or numeric string into localized display format
     * Example: 1234.5 → "1.234,5" (DE) or "1234.5" (US, no grouping)
     */
    override fun formatForDisplay(value: Double): String = getFormatter().cloneFormat().format(value)

    /**
     * Normalises a user-entered, possibly localized string into the editing format
     * produced by [formatForEditing] (Double overload).
     */
    override fun formatForEditing(localizedValue: String): String {
        val parsed = parseToDouble(localizedValue)
        return parsed?.let { formatForEditing(it) } ?: localizedValue
    }

    /**
     * Formats a value for an editable text field: localized decimal separator, no grouping,
     * up to 2 decimals and never scientific notation, so [parseToDouble] reads back exactly
     * the same value. Example: 12.5 → "12.5" (US) / "12,5" (DE); 1.0E7 → "10000000".
     */
    override fun formatForEditing(value: Double): String {
        val formatter = ApplicationNumberFormatter.getEditingNumberFormat(locale).cloneFormat()
        return formatter.format(value)
    }

    /**
     * Parses a user-entered amount. The whole string must be consumed — a partial parse
     * (e.g. "12.5" read as 12 by a German formatter) is rejected rather than silently
     * truncating the amount. Tries, in order:
     * 1. the locale's decimal format without grouping ("12,5" in DE, "12.5" in US),
     * 2. a plain '.' decimal ("12.5" typed on a keypad in any locale),
     * 3. the locale's grouped format ("1.234,5" in DE, "1,234.5" in US).
     */
    override fun parseToDouble(localizedValue: String): Double? {
        val text = localizedValue.trim()
        if (text.isEmpty()) return null

        parseFully(ApplicationNumberFormatter.getEditingNumberFormat(locale), text)?.let { return it }
        text.toDoubleOrNull()?.takeIf { it.isFinite() && PLAIN_NUMBER.matches(text) }?.let { return it }
        return parseFully(ApplicationNumberFormatter.getNumberFormatWithGrouping(locale), text)
    }

    private fun parseFully(format: NumberFormat, text: String): Double? {
        val position = ParsePosition(0)
        val result = format.cloneFormat().parse(text, position) ?: return null
        return if (position.index == text.length && position.errorIndex == -1) result.toDouble() else null
    }

    // NumberFormat is not thread-safe and the instances below are shared app-wide.
    private fun NumberFormat.cloneFormat(): NumberFormat = clone() as NumberFormat

    private companion object {
        val PLAIN_NUMBER = Regex("^[+-]?\\d*\\.?\\d+$")
    }
}

object ApplicationNumberFormatter {

    private val normalNumberFormatCache = ConcurrentHashMap<Locale, NumberFormat>()
    private val defaultNumberFormatCache = ConcurrentHashMap<Locale, NumberFormat>()

    fun getNumberFormatWithGrouping(locale: Locale): NumberFormat = normalNumberFormatCache.computeIfAbsent(locale) {
        NumberFormat.getNumberInstance(locale).apply {
            isGroupingUsed = true
            maximumFractionDigits = MAX_FRACTION_DIGITS
            minimumFractionDigits = 0
        }
    }

    fun getNumberFormatWithoutGrouping(locale: Locale): NumberFormat = defaultNumberFormatCache.computeIfAbsent(locale) {
        NumberFormat.getNumberInstance(locale).apply {
            isGroupingUsed = false
            maximumFractionDigits = MAX_FRACTION_DIGITS
            minimumFractionDigits = 1
        }
    }

    private val editingNumberFormatCache = ConcurrentHashMap<Locale, NumberFormat>()

    fun getEditingNumberFormat(locale: Locale): NumberFormat = editingNumberFormatCache.computeIfAbsent(locale) {
        NumberFormat.getNumberInstance(locale).apply {
            isGroupingUsed = false
            maximumFractionDigits = MAX_FRACTION_DIGITS
            minimumFractionDigits = 0
        }
    }

    // Amounts were previously shown with a single decimal (12.75 → "12.8"). Two decimals
    // matches how amounts are entered and stored.
    private const val MAX_FRACTION_DIGITS = 2
}
