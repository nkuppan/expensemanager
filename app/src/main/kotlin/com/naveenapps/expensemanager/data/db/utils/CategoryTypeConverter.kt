package com.naveenapps.expensemanager.data.db.utils

import androidx.room.TypeConverter
import com.naveenapps.expensemanager.domain.model.CategoryType


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