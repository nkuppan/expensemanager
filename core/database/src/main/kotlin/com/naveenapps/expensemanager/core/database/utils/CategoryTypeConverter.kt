package com.naveenapps.expensemanager.core.database.utils

import androidx.room.TypeConverter
import com.naveenapps.expensemanager.core.model.CategoryType

object CategoryTypeConverter {

    @TypeConverter
    fun ordinalToCategoryType(value: Int?): CategoryType? = value?.let { CategoryType.entries[value] }

    @TypeConverter
    fun categoryTypeToOrdinal(categoryType: CategoryType?): Int? = categoryType?.ordinal
}
