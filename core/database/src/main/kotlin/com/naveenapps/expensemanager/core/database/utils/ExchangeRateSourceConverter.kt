package com.naveenapps.expensemanager.core.database.utils

import androidx.room.TypeConverter
import com.naveenapps.expensemanager.core.model.ExchangeRateSource

object ExchangeRateSourceConverter {

    @TypeConverter
    fun ordinalToExchangeRateSource(value: Int?): ExchangeRateSource? = value?.let { ExchangeRateSource.entries[value] }

    @TypeConverter
    fun exchangeRateSourceToOrdinal(source: ExchangeRateSource?): Int? = source?.ordinal
}
