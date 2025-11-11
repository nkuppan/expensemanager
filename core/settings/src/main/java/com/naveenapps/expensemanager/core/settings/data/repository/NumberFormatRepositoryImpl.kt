package com.naveenapps.expensemanager.core.settings.data.repository

import com.naveenapps.expensemanager.core.settings.domain.model.NumberFormatType
import com.naveenapps.expensemanager.core.settings.domain.repository.NumberFormatRepository
import com.naveenapps.expensemanager.core.settings.domain.repository.NumberFormatSettingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.text.NumberFormat
import java.text.ParseException
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class NumberFormatRepositoryImpl(
    private val locale: Locale = Locale.getDefault(),
    numberFormatSettingRepository: NumberFormatSettingRepository,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
) : NumberFormatRepository {

    private var numberFormatType: NumberFormatType = NumberFormatType.WITHOUT_ANY_SEPARATOR

    init {
        numberFormatSettingRepository.getNumberFormatType().onEach {
            numberFormatType = it
        }.launchIn(coroutineScope)
    }

    private fun getFormatter(): NumberFormat {
        return when (numberFormatType) {
            NumberFormatType.WITHOUT_ANY_SEPARATOR -> {
                ApplicationNumberFormatter.getNumberFormatWithoutGrouping(locale)
            }

            NumberFormatType.WITH_COMMA_SEPARATOR -> {
                ApplicationNumberFormatter.getNumberFormatWithGrouping(locale)
            }
        }
    }

    /**
     * Converts a Double or numeric string into localized display format
     * Example: 1234.5 → "1.234,5" (DE) or "1234.5" (US, no grouping)
     */
    override fun formatForDisplay(value: Double): String {
        return getFormatter().format(value)
    }

    /**
     * Converts localized string into a plain string with '.' as decimal
     * Example: "1.234,5" (DE) → "1234.5"
     */
    override fun formatForEditing(localizedValue: String): String {
        val parsed = parseToDouble(localizedValue)
        return parsed?.toString() ?: localizedValue
    }

    /**
     * Converts localized string into a plain string with '.' as decimal
     * Example: "1.234,5" (DE) → "1234.5"
     */
    override fun formatForEditing(value: Double): String {
        return value.toString()
    }

    /**
     * Parses localized string to Double
     * Example: "1.234,5" (DE) → 1234.5
     */
    override fun parseToDouble(localizedValue: String): Double? {
        val formatter = getFormatter()
        return try {
            formatter.parse(localizedValue)?.toDouble()
        } catch (e: ParseException) {
            null
        }
    }
}


object ApplicationNumberFormatter {

    private val normalNumberFormatCache = ConcurrentHashMap<Locale, NumberFormat>()
    private val defaultNumberFormatCache = ConcurrentHashMap<Locale, NumberFormat>()

    fun getNumberFormatWithGrouping(locale: Locale): NumberFormat {
        return normalNumberFormatCache.computeIfAbsent(locale) {
            NumberFormat.getNumberInstance(locale).apply {
                isGroupingUsed = true
                maximumFractionDigits = 1
                minimumFractionDigits = 0
            }
        }
    }

    fun getNumberFormatWithoutGrouping(locale: Locale): NumberFormat {
        return defaultNumberFormatCache.computeIfAbsent(locale) {
            NumberFormat.getNumberInstance(locale).apply {
                isGroupingUsed = false
                maximumFractionDigits = 1
                minimumFractionDigits = 1
            }
        }
    }
}