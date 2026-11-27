package com.naveenapps.expensemanager.core.database.utils

import androidx.room.TypeConverter
import com.naveenapps.expensemanager.core.foundation.fromLocalToUTCTimeStamp
import com.naveenapps.expensemanager.core.foundation.fromUTCToLocalTimeStamp
import kotlin.time.Instant

/**
 * Dates are stored as "floating" local time: the device's wall-clock time written as if it were
 * UTC, and read back in the device's *current* zone (pinned by `DateConverterTest`). The column
 * values are exactly what the java.util.Date version wrote, so moving to Instant needed no
 * migration (KMP groundwork C4).
 */
object DateConverter {

    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? = value?.let { Instant.fromEpochMilliseconds(it.fromUTCToLocalTimeStamp()) }

    @TypeConverter
    fun dateToTimestamp(date: Instant?): Long? = date?.toEpochMilliseconds()?.fromLocalToUTCTimeStamp()
}
