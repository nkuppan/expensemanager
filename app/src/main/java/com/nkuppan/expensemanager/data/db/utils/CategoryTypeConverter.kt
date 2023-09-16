package com.nkuppan.expensemanager.data.db.utils

import androidx.room.TypeConverter
import com.nkuppan.expensemanager.domain.model.CategoryType


object CategoryTypeConverter {

    @TypeConverter
    fun ordinalToCategoryType(value: Int?): CategoryType? {
        return value?.let { CategoryType.values()[value] }
    }

    @TypeConverter
    fun categoryTypeToOrdinal(categoryType: CategoryType?): Int? {
        return categoryType?.ordinal
    }
}