package com.nkuppan.expensemanager.data.db.utils

import androidx.room.TypeConverter
import java.util.*



class DateConverter {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
