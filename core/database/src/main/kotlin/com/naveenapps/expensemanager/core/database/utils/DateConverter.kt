package com.naveenapps.expensemanager.core.database.utils

import androidx.room.TypeConverter
import com.naveenapps.expensemanager.core.common.utils.fromLocalToUTCTimeStamp
import com.naveenapps.expensemanager.core.common.utils.fromUTCToLocalDate
import java.util.Date

object DateConverter {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.fromUTCToLocalDate()
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.fromLocalToUTCTimeStamp()
    }
}
