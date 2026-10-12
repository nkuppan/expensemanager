package com.naveenapps.expensemanager.core.data.repository

import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.settings.data.repository.NumberFormatRepositoryImpl
import com.naveenapps.expensemanager.core.settings.domain.model.NumberFormatType
import com.naveenapps.expensemanager.core.settings.domain.repository.NumberFormatSettingRepository
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test

class NumberFormatRepositoryImplTest {

    private class FakeNumberFormatSettingRepository(type: NumberFormatType) : NumberFormatSettingRepository {
        val flow = MutableStateFlow(type)
        override fun getNumberFormatType(): Flow<NumberFormatType> = flow
        override suspend fun saveNumberFormatType(numberFormatType: NumberFormatType) {
            flow.value = numberFormatType
        }
    }

    private fun repository(
        locale: Locale,
        type: NumberFormatType = NumberFormatType.WITHOUT_ANY_SEPARATOR,
    ) = NumberFormatRepositoryImpl(
        locale = locale,
        numberFormatSettingRepository = FakeNumberFormatSettingRepository(type),
        coroutineScope = CoroutineScope(Dispatchers.Unconfined),
    )

    private val locales = listOf(Locale.US, Locale.GERMANY, Locale.FRANCE, Locale.forLanguageTag("hi-IN"), Locale.forLanguageTag("ar"))
    private val values = listOf(0.0, 12.5, 12.75, 1234.5, 99999.99, 10_000_000.0, 123_456_789.12, -42.1)

    @Test
    fun editingValueRoundTripsForAllLocalesAndFormatTypes() {
        locales.forEach { locale ->
            NumberFormatType.entries.forEach { type ->
                val repository = repository(locale, type)
                values.forEach { value ->
                    val edited = repository.formatForEditing(value)
                    Truth.assertWithMessage("$locale/$type: '$edited'")
                        .that(repository.parseToDouble(edited))
                        .isWithin(0.0001)
                        .of(value)
                }
            }
        }
    }

    @Test
    fun editingFormatNeverUsesScientificNotation() {
        Truth.assertThat(repository(Locale.US).formatForEditing(10_000_000.0)).isEqualTo("10000000")
        Truth.assertThat(repository(Locale.US).formatForEditing(12.5)).isEqualTo("12.5")
        Truth.assertThat(repository(Locale.GERMANY).formatForEditing(12.5)).isEqualTo("12,5")
    }

    @Test
    fun parsesDotDecimalInCommaDecimalLocale() {
        // Previously "12.5" in German was read as 12 (or 125 with grouping enabled).
        Truth.assertThat(repository(Locale.GERMANY).parseToDouble("12.5")).isEqualTo(12.5)
        Truth.assertThat(
            repository(Locale.GERMANY, NumberFormatType.WITH_COMMA_SEPARATOR).parseToDouble("12.5"),
        ).isEqualTo(12.5)
    }

    @Test
    fun parsesLocalizedAndGroupedValues() {
        Truth.assertThat(repository(Locale.GERMANY).parseToDouble("12,5")).isEqualTo(12.5)
        Truth.assertThat(repository(Locale.GERMANY).parseToDouble("1.234,5")).isEqualTo(1234.5)
        Truth.assertThat(repository(Locale.US).parseToDouble("1,234.5")).isEqualTo(1234.5)
        Truth.assertThat(repository(Locale.US).parseToDouble(" 42 ")).isEqualTo(42.0)
    }

    @Test
    fun rejectsInvalidInput() {
        val repository = repository(Locale.US)
        Truth.assertThat(repository.parseToDouble("")).isNull()
        Truth.assertThat(repository.parseToDouble("abc")).isNull()
        Truth.assertThat(repository.parseToDouble("12abc")).isNull()
    }

    @Test
    fun displayShowsTwoDecimals() {
        Truth.assertThat(repository(Locale.US).formatForDisplay(12.75)).isEqualTo("12.75")
        Truth.assertThat(
            repository(Locale.US, NumberFormatType.WITH_COMMA_SEPARATOR).formatForDisplay(1234.56),
        ).isEqualTo("1,234.56")
    }
}
